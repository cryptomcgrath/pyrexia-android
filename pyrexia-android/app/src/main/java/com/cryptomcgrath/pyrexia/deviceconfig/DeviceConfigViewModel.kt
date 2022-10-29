package com.cryptomcgrath.pyrexia.deviceconfig

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

    private val pyrexiaService = PyrexiaService(pyDevice)

    init {
        reactToEvents()
        dispatcher.post(DeviceConfigEvent.Init(pyDevice))
    }

    private fun reactToEvents() {
        dispatcher.getEventBus()
            .subscribeBy(
                onNext = { event ->
                    when(event) {
                        is DeviceConfigEvent.Init -> {
                            fetchStats()
                            fetchSensors()
                            fetchControls()
                        }
                    }
                },
                onError = {

                }
            ).addTo(disposables)
    }

    private fun fetchStats() {
        pyrexiaService.getStatList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    dispatcher.post(DeviceConfigEvent.NewStats(it))
                },
                onError = {
                    Log.d(TAG, "error fetching stats ${it.stackTraceToString()}")
                }
            ).addTo(disposables)
    }

    private fun fetchSensors() {
        pyrexiaService.getSensors()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    dispatcher.post(DeviceConfigEvent.NewSensors(it))
                },
                onError = {
                    Log.d(TAG, "error fetching sensors ${it.stackTraceToString()}")
                    // todo: handle error
                }
            ).addTo(disposables)
    }

    private fun fetchControls() {
        pyrexiaService.getControls()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    dispatcher.post(DeviceConfigEvent.NewControls(it))
                },
                onError = {
                    Log.d(TAG, "error fetching controls ${it.stackTraceToString()}")
                }
            ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}

private const val TAG="DeviceConfigViewModel"