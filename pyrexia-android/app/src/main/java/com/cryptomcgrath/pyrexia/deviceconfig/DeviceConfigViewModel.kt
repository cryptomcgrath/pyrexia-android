package com.cryptomcgrath.pyrexia.deviceconfig

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.CentralEvent
import com.cryptomcgrath.pyrexia.CentralState
import com.cryptomcgrath.pyrexia.DevicesRepo
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.cryptomcgrath.pyrexia.service.isUnauthorized
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.EventQueue
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class DeviceConfigViewModel(
    private val repo: DevicesRepo,
    internal val store: RxStore<CentralState>,
    internal val dispatcher: Dispatcher,
    internal val pyDevice: PyDevice) : ViewModel() {

    class Factory(private val repo: DevicesRepo,
                  private val store: RxStore<CentralState>,
                  private val dispatcher: Dispatcher,
                  private val pyDevice: PyDevice) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceConfigViewModel(repo, store, dispatcher, pyDevice) as T
        }
    }

    private val disposables = CompositeDisposable()

    val eventQueue = EventQueue.create()
    val loading = ObservableBoolean()

    init {
        relayEventsToFragment()
        reactToStateChange()
    }

    private fun relayEventsToFragment() {
        dispatcher.getEventBus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { event ->
                    when (event) {
                        is DeviceConfigEvent.OnShutdownDevice -> {
                            shutdownDevice(event.pyDevice)
                        }

                        is DeviceConfigEvent.RequestSensorDelete -> {
                            deleteSensor(event.pyDevice, event.sensor)
                        }

                        is DeviceConfigEvent.RequestControlDelete -> {
                            deleteControl(event.pyDevice, event.control)
                        }

                        is DeviceConfigEvent.RequestStatDelete -> {
                            deleteStat(event.pyDevice, event.stat)
                        }
                    }

                    eventQueue.post(event)
                },
                onError = {
                    // ignore
                }
            ).addTo(disposables)
    }

    private fun reactToStateChange() {
        store.stateStream
            .map { it.getDeviceState(pyDevice.uid).loading }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    loading.set(it)
                }
            ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun refreshDeviceConfig() {
        repo.refreshDeviceConfig(pyDevice)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {},
                onError = {
                    dispatcher.post(CentralEvent.SetLoading(pyDevice.uid, false))
                    if (it.isUnauthorized()) {
                        dispatcher.post(CentralEvent.GoToLogin(pyDevice))
                    } else {
                        eventQueue.post(DeviceConfigEvent.ShowNetworkError(it, true))
                    }
                }
            )
            .addTo(disposables)
    }

    private fun shutdownDevice(pyDevice: PyDevice) {
        repo.shutdownDevice(pyDevice)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    eventQueue.post(DeviceConfigEvent.OnShutdownCompleted)
                },
                onError = {
                    if (it.isUnauthorized()) {
                        dispatcher.post(CentralEvent.GoToLogin(pyDevice))
                    } else {
                        eventQueue.post(DeviceConfigEvent.ShowNetworkError(it))
                    }
                }
            ).addTo(disposables)
    }

    private fun deleteSensor(pyDevice: PyDevice, sensor: Sensor) {
        repo.deleteSensor(pyDevice, sensor)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    // ignore
                },
                onError = {
                    eventQueue.post(DeviceConfigEvent.ShowNetworkError(it))
                }
            ).addTo(disposables)
    }

    private fun deleteControl(pyDevice: PyDevice, control: Control) {
        repo.deleteControl(pyDevice, control)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    // ignore
                },
                onError = {
                    eventQueue.post(DeviceConfigEvent.ShowNetworkError(it))
                }
            ).addTo(disposables)
    }

    private fun deleteStat(pyDevice: PyDevice, stat: VirtualStat) {

    }

}

private const val TAG="DeviceConfigViewModel"
