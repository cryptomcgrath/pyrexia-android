package com.cryptomcgrath.pyrexia.deviceconfig

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.EventQueue
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class DeviceConfigViewModel(pyDevice: PyDevice) : ViewModel() {

    class Factory(private val pyDevice: PyDevice) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceConfigViewModel(pyDevice) as T
        }
    }

    private val disposables = CompositeDisposable()
    val store = RxStore.create(deviceConfigReducerFun)
    val dispatcher = Dispatcher.create(store)
    val eventQueue = EventQueue.create()

    private val pyrexiaService = PyrexiaService(pyDevice)

    init {
        reactToEvents()
        dispatcher.post(DeviceConfigEvent.Init(pyDevice))
    }

    private fun reactToEvents() {
        dispatcher.getEventBus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { event ->
                    // relay event to fragment
                    eventQueue.post(event)
                },
                onError = {
                    Log.e(TAG, "error reacting to event "+it.stackTraceToString())
                }
            ).addTo(disposables)
    }

    fun refreshData() {
        fetchDeviceConfig()
    }

    private fun fetchDeviceConfig() {
        Singles.zip(
            pyrexiaService.getStatList(),
            pyrexiaService.getSensors(),
            pyrexiaService.getControls()
        ).doOnSubscribe {
            dispatcher.post(DeviceConfigEvent.SetLoading(true))
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { (stats, sensors, controls) ->
                    dispatcher.post(DeviceConfigEvent.NewDeviceConfig(stats, sensors, controls))
                },
                onError = {
                    dispatcher.post(DeviceConfigEvent.ServicesError(it))
                }
        ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}

private const val TAG="DeviceConfigViewModel"