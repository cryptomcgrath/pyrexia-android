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
    private val store = RxStore.create(controlEditReducerFun)
    private val dispatcher = Dispatcher.create(store)
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

    val lastTextResId = if (control.lastOnTime > control.lastOffTime)
        R.string.control_last_on
    else R.string.control_last_off

    val lastTimeString = if (control.lastOnTime > control.lastOffTime) {
        control.lastOnTime.toLastUpdatedTimeString()
    } else {
        control.lastOffTime.toLastUpdatedTimeString()
    }

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

        dispatcher.post(ControlEditEvent.Init(control))
    }

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
                    dispatcher.post(ControlEditEvent.SaveControlSuccess)
                },
                onError = {
                    dispatcher.post(ControlEditEvent.ShowNetworkError(it))
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
}

internal data class ControlEditState(
    val control: Control? = null
): State

internal val controlEditReducerFun: ReducerFun<ControlEditState> = { inState, event ->
    val state = inState ?: ControlEditState()

    when (event) {
        is ControlEditEvent.Init -> {
            state.copy(
                control = event.control
            )
        }

        else -> state
    }
}

internal sealed class ControlEditEvent : Event {
    data class Init(val control: Control): ControlEditEvent()
    object SaveControlSuccess: ControlEditEvent()
    data class ShowNetworkError(val throwable: Throwable): ControlEditEvent()
}