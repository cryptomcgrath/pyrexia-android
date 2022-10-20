package com.cryptomcgrath.pyrexia.statlist

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.cryptomcgrath.pyrexia.thermostat.AUTO_REFRESH_INTERVAL
import com.cryptomcgrath.pyrexia.thermostat.TAG
import com.cryptomcgrath.pyrexia.thermostat.ThermostatEvent
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.EventQueue
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

internal class StatListViewModel(pyDevice: PyDevice): ViewModel() {

    class Factory(private val pyDevice: PyDevice) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatListViewModel(pyDevice) as T
        }
    }

    val store = RxStore.create(statListReducerFun)
    val dispatcher = Dispatcher.create(store)
    internal val eventQueue = EventQueue.create()

    private val pyrexiaService = PyrexiaService(pyDevice)
    private val disposables = CompositeDisposable()
    private var autoRefreshDisposable: Disposable? = null

    val showError = ObservableBoolean(false)
    val errorText = ObservableField<String>()
    val loading = ObservableBoolean(false)

    init {
        relayEventsToFragment()
    }

    private fun relayEventsToFragment() {
        dispatcher.getEventBus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { event ->
                    when (event) {
                        is StatListEvent.OnClickIncreaseTemp -> increaseTemp(event.id)
                        is StatListEvent.OnClickDecreaseTemp -> decreaseTemp(event.id)
                    }
                    updateUi()
                    eventQueue.post(event)
                },
                onError = {
                    Log.e(TAG, "error relaying event to fragment ${it.message}")
                }
            ).addTo(disposables)
    }

    private fun updateUi() {
        showError.set(store.state.connectionError != null)
        store.state.connectionError?.let {
            errorText.set(it.message.orEmpty())
        }
    }

    private fun refreshData() {
        pyrexiaService.getStatList()
            .doOnSubscribe {
                dispatcher.post(StatListEvent.SetLoading(true))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    dispatcher.post(StatListEvent.NewStatList(it))
                },
                onError = {
                    dispatcher.post(StatListEvent.ConnectionError(it))
                }
            ).addTo(disposables)
    }

    fun setupAutoRefresh() {
        refreshData()
        autoRefreshDisposable = Observable.interval(AUTO_REFRESH_INTERVAL, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    Log.d(TAG, "refreshing data")
                    refreshData()
                },
                onError = {
                    dispatcher.post(StatListEvent.ConnectionError(it))
                }
            )
    }

    fun cancelAutoRefresh() {
        autoRefreshDisposable?.dispose()
    }

    private fun increaseTemp(id: Int) {
        pyrexiaService.statIncrease(id)
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

    private fun decreaseTemp(id: Int) {
        pyrexiaService.statDecrease(id)
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

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        autoRefreshDisposable?.dispose()
    }
}