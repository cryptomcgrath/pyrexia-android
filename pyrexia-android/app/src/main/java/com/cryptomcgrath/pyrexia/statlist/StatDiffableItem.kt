package com.cryptomcgrath.pyrexia.statlist


import android.util.Log
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.cryptomcgrath.pyrexia.util.toFormattedTemperatureString
import com.edwardmcgrath.blueflux.core.Dispatcher

internal class StatDiffableItem(private val stat: ProgramRun,
                                private val dispatcher: Dispatcher) : DiffableItem {

    val name = stat.program.name
    val setPointText = stat.program.setPoint.toFormattedTemperatureString()
    val sensorValue = stat.sensor.value.toFormattedTemperatureString()
    private val isEnabled = stat.program.enabled

    val backgroundColor: Int = when {
        !stat.program.enabled -> R.color.grey42
        stat.control.controlOn && stat.program.mode == Program.Mode.HEAT -> R.color.heating
        stat.control.controlOn && stat.program.mode == Program.Mode.COOL -> R.color.cooling
        else -> R.color.cobalt
    }

    fun onClickStat() {
        dispatcher.post(StatListEvent.OnStatSelected(stat.program.id, stat.program.name))
    }

    fun onClickIncrease() {
        Log.d("StatDiffableItem", "onClickIncrease")
        if (isEnabled) {
            dispatcher.post(StatListEvent.OnClickIncreaseTemp(stat.program.id))
        }
    }

    fun onClickDecrease() {
        Log.d("StatDiffableItem", "onClickDecrease")
        if (isEnabled) {
            dispatcher.post(StatListEvent.OnClickDecreaseTemp(stat.program.id))
        }
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is StatDiffableItem &&
                other.stat == stat
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is StatDiffableItem &&
                other.stat.program.id == stat.program.id
    }
}