package com.cryptomcgrath.pyrexia.deviceconfig

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
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
import retrofit2.HttpException

internal class DeviceConfigViewModel(application: Application,
                                     private val pyDevice: PyDevice) : AndroidViewModel(application) {

    class Factory(private val application: Application,
                  private val pyDevice: PyDevice) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceConfigViewModel(application, pyDevice) as T
        }
    }

    private val disposables = CompositeDisposable()
    val store = RxStore.create(deviceConfigReducerFun)
    val dispatcher = Dispatcher.create(store)
    val eventQueue = EventQueue.create()

    private val pyrexiaService = PyrexiaService(application, pyDevice)

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
                    when (event) {
                        is DeviceConfigEvent.GoToSensorDelete -> deleteSensor(event.sensor)
                        is DeviceConfigEvent.GoToControlDelete -> deleteControl(event.control)
                    }
                    // relay event to fragment
                    eventQueue.post(event)
                },
                onError = {
                    Log.e(TAG, "error reacting to event "+it.stackTraceToString())
                }
            ).addTo(disposables)
    }

    fun refreshData() {
        pyrexiaService.isLoggedIn()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    if (it) {
                        fetchDeviceConfig()
                    } else {
                        dispatcher.post(DeviceConfigEvent.GoToLogin)
                    }
                }, onError = {

                }
            ).addTo(disposables)
    }

    private fun deleteSensor(sensor: Sensor) {
        if (sensor.id > 0) {
            pyrexiaService.deleteSensor(sensor)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        refreshData()
                    },
                    onError = {
                        dispatcher.post(DeviceConfigEvent.NetworkError(it, false))
                    }
                ).addTo(disposables)
        } else {
            refreshData()
        }
    }

    private fun deleteControl(control: Control) {
        if (control.id > 0) {
            pyrexiaService.deleteControl(control)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        refreshData()
                    },
                    onError = {
                        dispatcher.post(DeviceConfigEvent.NetworkError(it, false))
                    }
                ).addTo(disposables)
        } else {
            refreshData()
        }
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
                    if (it.isUnauthorized()) {
                        dispatcher.post(DeviceConfigEvent.GoToLogin)
                    } else {
                        dispatcher.post(DeviceConfigEvent.NetworkError(it, true))
                    }
                }
        ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}

private const val TAG="DeviceConfigViewModel"

internal  fun Throwable.isUnauthorized(): Boolean {
    return this is HttpException && setOf(401,403).contains(this.code())
}