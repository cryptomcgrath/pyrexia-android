package com.cryptomcgrath.pyrexia.deviceconfig

import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.devicelist.hideKeyboard
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
import java.text.SimpleDateFormat
import java.util.*

internal class SensorEditViewModel(private val pyDevice: PyDevice,
                                   private val sensor: Sensor): ViewModel() {

    class Factory(private val pyDevice: PyDevice,
                  private val sensor: Sensor) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SensorEditViewModel(pyDevice, sensor) as T
        }
    }

    private val pyrexiaService = PyrexiaService(pyDevice)

    private val store = RxStore.create(sensorEditReducerFun)
    private val dispatcher = Dispatcher.create(store)
    val eventQueue = EventQueue.create()
    private val disposables = CompositeDisposable()

    var name: String = sensor.name
    val nameError = ObservableField<String>()
    var addr: String = sensor.addr
    val addrError = ObservableField<String>()
    val addressHintResId = sensor.sensorType?.addrHintResId ?: R.string.sensor_addr_hint_generic
    var updateInterval: String = sensor.updateInterval.toString()
    val updateIntervalError = ObservableField<String>()

    val lastUpdated = sensor.lastUpdatedTs.toLastUpdatedTimeString()

    val sensorDrawableInt = sensor.sensorType?.imageResId ?: 0

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

private val lastUpdatedFormatter by lazy {
    SimpleDateFormat("MMM dd h:mma", Locale.US)
}

private fun Long.toLastUpdatedTimeString(): String {
    val now = Date().time / 1000
    val elapsed = now - this
    val d = (elapsed / 24*60*60).toInt()
    val h = ((elapsed - d * 24*60*60) / 3600).toInt()
    val m = (elapsed - (d * 24*60*60) - (h * 3600)) / 60
    val s = elapsed - (d * 24*60*60) - (h * 3600) - (m * 60)

    return when {
        d > 0 -> lastUpdatedFormatter.format(this*1000)
        h > 0 -> "$h hours $m minutes ago"
        m > 0 -> "$m minutes ago"
        else -> "$s seconds ago"
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