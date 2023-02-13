package com.cryptomcgrath.pyrexia.deviceconfig

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.InverseMethod
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.CentralState
import com.cryptomcgrath.pyrexia.DevicesRepo
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.cryptomcgrath.pyrexia.util.toFormattedTemperatureString
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.EventQueue
import com.edwardmcgrath.blueflux.core.RxStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

internal class StatEditViewModel(private val repo: DevicesRepo,
                                 private val store: RxStore<CentralState>,
                                 private val pyDevice: PyDevice,
                                 private val stat: VirtualStat) : ViewModel() {

    class Factory(private val repo: DevicesRepo,
                  private val store: RxStore<CentralState>,
                  private val pyDevice: PyDevice,
                  private val stat: VirtualStat
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatEditViewModel(repo, store, pyDevice, stat) as T
        }
    }

    private val disposables = CompositeDisposable()
    val eventQueue = EventQueue.create()
    var name = stat.program.name
    val nameError = ObservableField<String>()
    var mode = stat.program.mode.slug
    val modeError = ObservableField<String>()

    var enabled = stat.program.enabled

    var setPoint = stat.program.setPoint
    var controlId = stat.program.control_id
    var controlError = ObservableField<String>()
    val controls: List<Control> get() {
        return store.state.getDeviceState(pyDevice).controls
    }
    var sensorId = stat.program.sensor_id
    val sensorError = ObservableField<String>()
    val sensors: List<Sensor> get() {
        return store.state.getDeviceState(pyDevice).sensors
    }
    val sensorDrawableInt = ObservableField<Int>()
    val setPointText = stat.program.setPoint.toFormattedTemperatureString()
    val sensorValue = ObservableField<String>()

    val backgroundColor: Int = when {
        !stat.program.enabled -> R.color.grey42
        stat.control.controlOn && stat.program.mode == Program.Mode.HEAT -> R.color.heating
        stat.control.controlOn && stat.program.mode == Program.Mode.COOL -> R.color.cooling
        else -> R.color.cobalt
    }

    init {
        updateUi()
    }

    fun onClickSave(view: View?) {
        view?.hideKeyboard()
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

    fun onSensorTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                updateUi()
            }
        }
    }

    private fun updateUi() {
        sensorDrawableInt.set(
            sensors.firstOrNull { it.id == sensorId }?.sensorType?.imageResId
                ?: 0
        )
        sensorValue.set(
            (sensors.firstOrNull() {
                it.id == sensorId
            }?.value ?: 0f).toFormattedTemperatureString()
        )
    }

    private fun checkErrors(): Boolean {
        nameError.set(null)
        var error = false
        if (name.isEmpty()) {
            nameError.set("Name cannot be blank")
            error = true
        }
        if (!setOf(Program.Mode.HEAT, Program.Mode.COOL).contains(Program.Mode.fromString(mode))) {
            modeError.set("Mode cannot be empty")
            error = true
        }
        if (controlId == 0) {
            controlError.set("Selected a control")
            error = true
        }
        if (sensorId == 0) {
            sensorError.set("Select a sensor")
            error = true
        }
        return error
    }

    private fun saveStat(program: Program) {
        repo.saveStat(
            pyDevice = pyDevice,
            program = program
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    eventQueue.post(StatEditUiEvent.SaveStatSuccess)
                },
                onError = {
                    eventQueue.post(StatEditUiEvent.ShowNetworkError(it))
                }
            ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    sealed class StatEditUiEvent : Event {
        object SaveStatSuccess : StatEditUiEvent()
        data class ShowNetworkError(val throwable: Throwable): StatEditUiEvent()
    }
}

object Converters {
    fun control_index_to_name(controls: List<Control>, index: Int): String {
        return controls.firstOrNull {
            it.id == index
        }?.name ?: ""
    }

    @InverseMethod(value = "control_index_to_name")
    fun control_name_to_index(controls:List<Control>, name: String): Int {
        return controls.firstOrNull {
            it.name == name
        }?.id ?: 0
    }

    fun sensor_index_to_name(sensors: List<Sensor>, index: Int): String {
        return sensors.firstOrNull {
            it.id == index
        }?.name ?: ""
    }

    @InverseMethod(value = "sensor_index_to_name")
    fun sensor_name_to_index(sensors: List<Sensor>, name: String): Int {
        return sensors.firstOrNull {
            it.name == name
        }?.id ?: 0
    }
}


