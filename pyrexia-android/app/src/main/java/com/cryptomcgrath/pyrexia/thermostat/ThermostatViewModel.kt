package com.cryptomcgrath.pyrexia.thermostat

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.cryptomcgrath.pyrexia.statlist.StatListEvent
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.EventQueue
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

internal class ThermostatViewModel(pyDevice: PyDevice, id: Int): ViewModel() {

    class Factory(private val pyDevice: PyDevice,
                  private val id: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThermostatViewModel(pyDevice, id) as T
        }
    }

    internal val store = RxStore.create(thermostatReducerFun)
    internal val dispatcher = Dispatcher.create(store)
    internal val eventQueue = EventQueue.create()

    private val pyrexiaService = PyrexiaService(pyDevice)
    private val disposables = CompositeDisposable()

    val backgroundColor = ObservableInt(R.color.black)
    val showError = ObservableBoolean(false)

    private val current get() = store.state.current

    init {
        dispatcher.getEventBus().subscribeBy(
            onNext = {
                when (it) {
                    is ThermostatEvent.Init -> {
                        refreshData()
                        subscribeToStateChanges()
                        setupAutoRefresh()
                    }
                    is StatListEvent.OnClickIncreaseTemp -> {
                        increaseTemp()
                    }
                    is StatListEvent.OnClickDecreaseTemp -> {
                        decreaseTemp()
                    }
                }
                eventQueue.post(it)
            },
            onError = {
                // ignore
            }
        ).addTo(disposables)
        dispatcher.post(ThermostatEvent.Init(id))
    }

    private fun subscribeToStateChanges() {
        store.stateStream.subscribeBy(
            onNext = {
                updateUi(it)
            },
            onError = {
                // ignore
            }
        ).addTo(disposables)
    }

    private fun updateUi(state: ThermostatState) {
        showError.set(
            state.connectionError != null
        )
        state.current?.let {
            backgroundColor.set(
                when {
                    !it.program.enabled -> R.color.grey42
                    it.control.controlOn && it.program.mode == Program.Mode.HEAT -> R.color.heating
                    it.control.controlOn && it.program.mode == Program.Mode.COOL -> R.color.cooling
                    else -> R.color.cobalt
                }
            )
        }
    }

    private fun refreshData() {
        pyrexiaService.getStatList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    dispatcher.post(ThermostatEvent.NewStatList(it))
                },
                onError = {
                    dispatcher.post(ThermostatEvent.ConnectionError(it))
                }
            ).addTo(disposables)
    }

    private fun setupAutoRefresh() {
        Observable.interval(AUTO_REFRESH_INTERVAL, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    Log.d(TAG, "refreshing data")
                    refreshData()
                },
                onError = {
                    // ignore
                }
            ).addTo(disposables)
    }

    fun enableStat(id: Int) {
        pyrexiaService.statEnable(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    refreshData()
                },
                onError = {
                    dispatcher.post(ThermostatEvent.ConnectionError(it))
                }
            ).addTo(disposables)
    }

    fun disableStat(id: Int) {
        pyrexiaService.statDisable(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    refreshData()
                },
                onError = {
                    dispatcher.post(ThermostatEvent.ConnectionError(it))
                }
            ).addTo(disposables)
    }

    private fun increaseTemp() {
        current?.let {
            if (it.program.enabled) {
                pyrexiaService.statIncrease(it.program.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onComplete = {
                            refreshData()
                        },
                        onError = {
                            dispatcher.post(ThermostatEvent.ConnectionError(it))
                        }
                    ).addTo(disposables)
            }

        }
    }

    private fun decreaseTemp() {
        current?.let {
            if (it.program.enabled) {
                pyrexiaService.statDecrease(it.program.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onComplete = {
                            refreshData()
                        },
                        onError = {
                            dispatcher.post(ThermostatEvent.ConnectionError(it))
                        }
                    ).addTo(disposables)
            }
        }
    }

    fun onClickConnectionError() {
        store.state.connectionError?.let {
            eventQueue.post(UiEvent.ServiceError(it))
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    sealed class UiEvent: Event {
        data class ServiceError(val throwable: Throwable): UiEvent()
        data class StatEnable(val id: Int, val enable: Boolean): UiEvent()
    }
}

const val AUTO_REFRESH_INTERVAL = 15L
const val TAG="ThermostatViewModel"

fun String.sentenceCase(): String {
    return this.lowercase(Locale.getDefault()).replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}
