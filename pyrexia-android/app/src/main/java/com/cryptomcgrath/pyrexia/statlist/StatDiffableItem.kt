package com.cryptomcgrath.pyrexia.statlist


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
    val modeText = stat.program.mode.name
    val isEnabled = stat.program.enabled
    val background = when {
        !stat.program.enabled -> R.color.light_grey
        stat.control.controlOn && stat.program.mode == Program.Mode.HEAT -> R.color.heating
        stat.control.controlOn && stat.program.mode == Program.Mode.COOL -> R.color.cooling
        else -> R.color.light_blue
    }

    fun onClickStat() {
        dispatcher.post(StatListEvent.OnStatSelected(stat.program.id))
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