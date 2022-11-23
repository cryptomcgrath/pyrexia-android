package com.cryptomcgrath.pyrexia.deviceconfig

import android.app.Application
import android.view.View
import androidx.annotation.StringRes
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.EventQueue
import com.edwardmcgrath.blueflux.core.ReducerFun
import com.edwardmcgrath.blueflux.core.RxStore
import com.edwardmcgrath.blueflux.core.State
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class SensorEditViewModel(application: Application,
                                   pyDevice: PyDevice,
                                   private val sensor: Sensor): ViewModel() {

    class Factory(private val application: Application,
                  private val pyDevice: PyDevice,
                  private val sensor: Sensor) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SensorEditViewModel(application, pyDevice, sensor) as T
        }
    }

    private val pyrexiaService = PyrexiaService(application, pyDevice)

    private val store = RxStore.create(sensorEditReducerFun)
    private val dispatcher = Dispatcher.create(store)
    val eventQueue = EventQueue.create()
    private val disposables = CompositeDisposable()

    var name: String = sensor.name
    val nameError = ObservableField<String>()
    var addr: String = sensor.addr
    val addrError = ObservableField<String>()
    @StringRes
    val addressHintResId = sensor.sensorType?.addrHintResId ?: R.string.sensor_addr_hint_generic
    var updateInterval: String = sensor.updateInterval.toString()
    val updateIntervalError = ObservableField<String>()
    val sensorDrawableInt = sensor.sensorType?.imageResId ?: 0

    // TODO: show readonly info about sensor
    val lastUpdated = sensor.lastUpdatedTs.toLastUpdatedTimeString()

    init {
        dispatcher.getEventBus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    eventQueue.post(it)
                },
                onError = {
                    // ignore
                }
            ).addTo(disposables)

        dispatcher.post(SensorEditEvent.Init(sensor))
    }

    fun onClickSave(view: View?) {
        view?.hideKeyboard()
        if (!checkErrors()) {
            saveSensor(
                sensor.copy(
                    name = name,
                    addr = addr,
                    updateInterval = updateInterval.toIntOrNull() ?: 0
                )
            )
        }
    }

    private fun saveSensor(sensor: Sensor) {
        if (sensor.id == 0) {
            pyrexiaService.addSensor(sensor)
        } else {
            pyrexiaService.updateSensor(sensor)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    dispatcher.post(SensorEditEvent.SaveSensorSuccess)
                },
                onError = {
                    // TODO:
                }
            ).addTo(disposables)
    }

    private fun checkErrors(): Boolean {
        nameError.set(null)
        addrError.set(null)
        updateIntervalError.set(null)
        var error = false
        if (name.isEmpty()) {
            nameError.set("Name cannot be blank")
            error = true
        }
        if (addr.isEmpty()) {
            addrError.set("Address cannot be blank")
            error = true
        }
        if ((updateInterval.toIntOrNull() ?: 0) <= 0) {
            updateIntervalError.set("Enter valid update interval in seconds")
            error = true
        }
        return error
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}

internal data class SensorEditState(
    val sensor: Sensor? = null
): State

internal val sensorEditReducerFun: ReducerFun<SensorEditState> = { inState, event ->
    val state = inState ?: SensorEditState()

    when (event) {
        is SensorEditEvent.Init -> {
            state.copy(
                sensor = event.sensor
            )
        }

        else -> state
    }
}

internal sealed class SensorEditEvent : Event {
    data class Init(val sensor: Sensor): SensorEditEvent()
    object SaveSensorSuccess: SensorEditEvent()
}