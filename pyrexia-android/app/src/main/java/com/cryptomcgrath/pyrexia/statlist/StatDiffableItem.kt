package com.cryptomcgrath.pyrexia.statlist


import android.util.Log
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.deviceconfig.secsToLastUpdatedTimeString
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.cryptomcgrath.pyrexia.thermostat.ThermostatEvent
import com.cryptomcgrath.pyrexia.util.toFormattedTemperatureString
import com.edwardmcgrath.blueflux.core.Dispatcher
import java.util.*

internal class StatDiffableItem(private val stat: VirtualStat,
                                private val pyDevice: PyDevice,
                                private val dispatcher: Dispatcher,
                                val updating: Boolean): DiffableItem {

    val name = stat.program.name
    val setPointText = stat.program.setPoint.toFormattedTemperatureString()
    val sensorValue = stat.sensor.value.toFormattedTemperatureString()
    private val elapsedSecs = Date().time / 1000 - stat.lastRefreshTimeSecs
    private val secsSinceSensorUpdate = (stat.currentTimeSecs ?: 0L) - stat.sensor.lastUpdatedTs
    private val totalSecs = elapsedSecs + secsSinceSensorUpdate
    private val hasTimeData = stat.sensor.lastUpdatedTs > 0L && (stat.currentTimeSecs ?: 0L) > 0L
    val message: String get() {
        Log.d("StatDiffableItem", "elapsedSecs=$elapsedSecs secSinceSensorUpdate=$secsSinceSensorUpdate totalSecs=$totalSecs")
        return when {
                    hasTimeData && totalSecs > 60 -> totalSecs.secsToLastUpdatedTimeString()
            stat.control.controlOn -> {
                if (stat.program.mode == Program.Mode.HEAT) "Heating" else "Cooling"
            }
            else -> ""
        }
    }
    private val isEnabled = stat.program.enabled

    val backgroundColor: Int = when {
        !stat.program.enabled -> R.color.grey42
        stat.control.controlOn && stat.program.mode == Program.Mode.HEAT -> R.color.heating
        stat.control.controlOn && stat.program.mode == Program.Mode.COOL -> R.color.cooling
        else -> R.color.cobalt
    }

    fun onClickStat() {
        dispatcher.post(StatListEvent.OnStatSelected(stat, stat.program.name))
    }

    fun onClickIncrease() {
        Log.d("StatDiffableItem", "onClickIncrease")
        if (isEnabled) {
            dispatcher.post(ThermostatEvent.RequestIncreaseTemp(pyDevice, stat.program.id))
        }
    }

    fun onClickDecrease() {
        Log.d("StatDiffableItem", "onClickDecrease")
        if (isEnabled) {
            dispatcher.post(ThermostatEvent.RequestDecreaseTemp(pyDevice, stat.program.id))
        }
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is StatDiffableItem &&
                other.stat == stat &&
                other.updating == updating
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is StatDiffableItem &&
                other.stat.program.id == stat.program.id
    }
}