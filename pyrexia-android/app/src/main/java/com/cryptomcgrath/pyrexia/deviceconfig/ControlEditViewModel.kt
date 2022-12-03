package com.cryptomcgrath.pyrexia.deviceconfig

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.EventQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class ControlEditViewModel(application: Application,
                                    pyDevice: PyDevice,
                                    private val control: Control) : AndroidViewModel(application) {

    class Factory(private val application: Application,
                  private val pyDevice: PyDevice,
                  private val control: Control
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ControlEditViewModel(application, pyDevice, control) as T
        }
    }

    private val pyrexiaService = PyrexiaService(application, pyDevice)

    val eventQueue = EventQueue.create()
    private val disposables = CompositeDisposable()

    var name = control.name
    val nameError = ObservableField<String>()
    var minRun = control.minRun.toString()
    val minRunError = ObservableField<String>()
    var minRest = control.minRest.toString()
    val minRestError = ObservableField<String>()
    var gpio = control.gpio.toString()
    val gpioError = ObservableField<String>()
    var gpioOnHigh = control.gpioOnHigh
    var runCapacity = control.runCapacity.toString()
    val totalRun = control.totalRun.toString()
    val showRunTime = control.totalRun > 0

    val lastTextResId = if (control.lastOnTime > control.lastOffTime)
        R.string.control_last_on
    else R.string.control_last_off

    val lastTimeString = if (control.lastOnTime > control.lastOffTime) {
        control.lastOnTime.toLastUpdatedTimeString()
    } else {
        control.lastOffTime.toLastUpdatedTimeString()
    }
    val showLastText = control.lastOffTime > 0

    fun onClickSave(view: View?) {
        view?.hideKeyboard()
        if (!checkErrors()) {
            saveControl(
                control.copy(
                    name = name,
                    minRun = minRun.toIntOrNull() ?: 0,
                    minRest = minRest.toIntOrNull() ?: 0,
                    gpio = gpio.toIntOrNull() ?: 0,
                    gpioOnHigh = gpioOnHigh,
                    runCapacity = runCapacity.toIntOrNull() ?: 0
                )
            )
        }
    }

    private fun saveControl(control: Control) {
        if (control.id == 0) {
            pyrexiaService.addControl(control)
        } else {
            pyrexiaService.updateControl(control)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    eventQueue.post(ControlEditUiEvent.SaveControlSuccess)
                },
                onError = {
                    eventQueue.post(ControlEditUiEvent.ShowNetworkError(it))
                }
            ).addTo(disposables)
    }

    private fun checkErrors(): Boolean {
        var error = false
        nameError.set(null)
        minRunError.set(null)
        minRestError.set(null)
        gpioError.set(null)

        if (name.isEmpty()) {
            nameError.set(getApplication<Application>().getString(R.string.control_name_error))
            error = true
        }
        if (minRun.toIntOrNull() == null || (minRun.toIntOrNull() ?:0) < 0) {
            minRunError.set(getApplication<Application>().getString(R.string.control_min_run_error))
            error = true
        }
        if (!minRest.isPositiveInt()) {
            minRestError.set(getApplication<Application>().getString(R.string.control_min_rest_error))
            error = true
        }
        if (!gpio.isValidGpioPin()) {
            gpioError.set(getApplication<Application>().getString(R.string.control_gpio_error))
            error = true
        }
        return error
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    internal sealed class ControlEditUiEvent : Event {
        object SaveControlSuccess: ControlEditUiEvent()
        data class ShowNetworkError(val throwable: Throwable): ControlEditUiEvent()
    }
}

