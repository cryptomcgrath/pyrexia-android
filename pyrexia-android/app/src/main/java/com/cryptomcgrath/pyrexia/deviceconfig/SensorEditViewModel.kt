package com.cryptomcgrath.pyrexia.deviceconfig

import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.DevicesRepo
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.EventQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class SensorEditViewModel(
    private val repo: DevicesRepo,
    private val pyDevice: PyDevice,
    private val sensor: Sensor): ViewModel() {

    class Factory(private val repo: DevicesRepo,
                  private val pyDevice: PyDevice,
                  private val sensor: Sensor) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SensorEditViewModel(repo, pyDevice, sensor) as T
        }
    }

    val eventQueue = EventQueue.create()
    private val disposables = CompositeDisposable()

    var name: String = sensor.name
    val nameError = ObservableField<String>()
    var addr: String = sensor.addr
    val addrError = ObservableField<String>()
    val addressHintResId = sensor.sensorType?.addrHintResId ?: R.string.sensor_addr_hint_generic
    var updateInterval: String = sensor.updateInterval.toString()
    val updateIntervalError = ObservableField<String>()
    val sensorDrawableInt = sensor.sensorType?.imageResId ?: 0

    // TODO: show readonly info about sensor
    val lastUpdated = sensor.lastUpdatedTs.toLastUpdatedTimeString()

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
        repo.saveSensor(pyDevice, sensor)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    eventQueue.post(SensorEditUiEvent.SaveSensorSuccess)
                },
                onError = {
                    eventQueue.post(SensorEditUiEvent.ShowNetworkError(it))
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

    sealed class SensorEditUiEvent : Event {
        object SaveSensorSuccess: SensorEditUiEvent()
        data class ShowNetworkError(val throwable: Throwable): SensorEditUiEvent()
    }
}

