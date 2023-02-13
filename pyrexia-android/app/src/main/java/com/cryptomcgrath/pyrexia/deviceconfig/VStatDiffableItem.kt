package com.cryptomcgrath.pyrexia.deviceconfig

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.cryptomcgrath.pyrexia.thermostat.sentenceCase
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.cryptomcgrath.pyrexia.util.toFormattedTemperatureString
import com.edwardmcgrath.blueflux.core.Dispatcher

internal class VStatDiffableItem(val stat: VirtualStat,
                                 private val pyDevice: PyDevice,
                                 private val dispatcher: Dispatcher): DiffableItem {
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
        view?.let { showPopupMenu(view) }
    }

    private fun showPopupMenu(view: View) {
        PopupMenu(view.context, view).apply {
            menuInflater.inflate(R.menu.deviceconfig_overflow, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.component_delete -> {
                        AlertDialog.Builder(view.context)
                            .setTitle(R.string.delete_dialog_title)
                            .setMessage(view.context.getString(R.string.component_delete_confirm, stat.program.name))
                            .setPositiveButton(R.string.yes) { _, _ ->
                                dispatcher.post(DeviceConfigEvent.RequestStatDelete(pyDevice, stat))
                            }
                            .setIcon(R.drawable.ic_outline_delete_24)
                            .setNegativeButton(R.string.no) { _, _ -> }
                            .show()
                        true
                    }

                    R.id.component_edit -> {
                        dispatcher.post(DeviceConfigEvent.GoToStatEdit(stat))
                        true
                    }

                    else -> false
                }
            }
            setForceShowIcon(true)
            show()
        }
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