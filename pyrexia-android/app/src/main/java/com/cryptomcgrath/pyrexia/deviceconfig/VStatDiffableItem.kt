package com.cryptomcgrath.pyrexia.deviceconfig

import android.view.View
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.cryptomcgrath.pyrexia.thermostat.sentenceCase
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.cryptomcgrath.pyrexia.util.toFormattedTemperatureString
import com.edwardmcgrath.blueflux.core.Dispatcher

internal class VStatDiffableItem(val stat: ProgramRun, dispatcher: Dispatcher): DiffableItem {
    val name = stat.program.name
    val setPointText = stat.program.setPoint.toFormattedTemperatureString()
    val sensorValue = stat.sensor.value.toFormattedTemperatureString()
    val modeText = stat.program.mode.name.sentenceCase()

    val backgroundColor: Int = when {
        !stat.program.enabled -> R.color.grey42
        stat.control.controlOn && stat.program.mode == Program.Mode.HEAT -> R.color.heating
        stat.control.controlOn && stat.program.mode == Program.Mode.COOL -> R.color.cooling
        else -> R.color.cobalt
    }

    fun onClickOverflow(view: View?) {

    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is VStatDiffableItem &&
                other.stat == stat
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is VStatDiffableItem &&
                other.stat.program.id == stat.program.id
    }
}