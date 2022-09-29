package com.cryptomcgrath.pyrexia.thermostat

import android.util.Log
import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.cryptomcgrath.pyrexia.statlist.StatListViewModel
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
import java.util.concurrent.TimeUnit

internal class ThermostatViewModel(private val id: Int): ViewModel() {

    class Factory(private val id: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThermostatViewModel(id) as T
        }
    }

    private val store = RxStore.create(thermostatReducerFun)
    private val dispatcher = Dispatcher.create(store)
    internal val eventQueue = EventQueue.create()

    private val pyrexiaService = PyrexiaService()
    private val disposables = CompositeDisposable()

    val name = ObservableField<String>("----")
    val setPointText = ObservableField<String>("---")
    val sensorValue = ObservableField<String>("---")
    val modeText = ObservableField<String>("----")
    val isEnabled = ObservableBoolean(false)
    val background = ObservableInt(R.color.light_blue)
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
                }
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
            name.set(it.program.name)
            setPointText.set(String.format("%3d°", it.program.setPoint.toInt()))
            sensorValue.set(String.format("%3d°", it.sensor.value.toInt()))
            modeText.set(it.program.mode.name)
            isEnabled.set(it.program.enabled)
            background.set(
                when {
                    !it.program.enabled -> R.color.light_grey
                    it.control.controlOn && it.program.mode == Program.Mode.HEAT -> R.color.heating
                    it.control.controlOn && it.program.mode == Program.Mode.COOL -> R.color.cooling
                    else -> R.color.light_blue
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

    fun onEnabledChanged(buttonView: CompoundButton, isChecked: Boolean) {
        isEnabled.set(isChecked)
        // POST /stat/:id/enable
    }

    fun onClickIncrease() {
        current?.let {
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
                )
        }
    }

    fun onClickDecrease() {
        current?.let {
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
                )
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
    }
}

//const val BASE_URL = "http://bigred.ddns.net:8000/"
const val BASE_URL = "http://100.96.4.79:8000/"

const val AUTO_REFRESH_INTERVAL = 15L
const val TAG="ThermostatViewModel"
