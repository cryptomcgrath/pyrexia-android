package com.cryptomcgrath.pyrexia.deviceconfig

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.cryptomcgrath.pyrexia.thermostat.sentenceCase
import com.cryptomcgrath.pyrexia.util.toFormattedTemperatureString
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.EventQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class StatEditViewModel(application: Application,
                                 pyDevice: PyDevice,
                                 private val stat: VirtualStat) : AndroidViewModel(application) {

    class Factory(private val application: Application,
                  private val pyDevice: PyDevice,
                  private val stat: VirtualStat
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatEditViewModel(application, pyDevice, stat) as T
        }
    }

    private val pyrexiaService = PyrexiaService(application, pyDevice)
    private val disposables = CompositeDisposable()

    val eventQueue = EventQueue.create()

    var name = stat.program.name
    val nameError = ObservableField<String>()
    var mode = stat.program.mode.name
    var enabled = stat.program.enabled

    var sensorId = stat.program.sensor_id
    var setPoint = stat.program.setPoint
    var controlId = stat.program.control_id

    val setPointText = stat.program.setPoint.toFormattedTemperatureString()
    val sensorValue = stat.sensor.value.toFormattedTemperatureString()
    val modeText = stat.program.mode.name.sentenceCase()

    val backgroundColor: Int = when {
        !stat.program.enabled -> R.color.grey42
        stat.control.controlOn && stat.program.mode == Program.Mode.HEAT -> R.color.heating
        stat.control.controlOn && stat.program.mode == Program.Mode.COOL -> R.color.cooling
        else -> R.color.cobalt
    }

    fun onClickSave(view: View?) {
        if (!checkErrors()) {
            saveStat(
                stat.program.copy(
                    name = name,
                    enabled = enabled,
                    mode = Program.Mode.fromString(mode),
                    control_id = controlId,
                    sensor_id = sensorId,
                    setPoint = setPoint
                )
            )
        }
    }

    private fun checkErrors(): Boolean {
        nameError.set(null)
        var error = false
        if (name.isEmpty()) {
            nameError.set("Name cannot be blank")
            error = true
        }
        return error
    }

    private fun saveStat(program: Program) {
        if (program.id == 0) {
            pyrexiaService.addStat(program)
        } else {
            pyrexiaService.updateStat(program)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    eventQueue.post(StatEditUiEvent.SaveStatSuccess)
                },
                onError = {
                    // TODO:
                }
            ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    sealed class StatEditUiEvent : Event {
        object SaveStatSuccess : StatEditUiEvent()
    }
}
