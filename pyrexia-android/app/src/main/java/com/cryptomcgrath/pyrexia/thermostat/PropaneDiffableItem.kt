package com.cryptomcgrath.pyrexia.thermostat

import android.view.View
import androidx.appcompat.app.AlertDialog
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.deviceconfig.DeviceConfigEvent
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher
import kotlin.math.max

internal class PropaneDiffableItem(private val dispatcher: Dispatcher,
                                   totalRun: Int,
                                   runCapacity: Int) : DiffableItem {
    val showPropane = runCapacity > 0
    private val remainingSecs = max(0f, (runCapacity - totalRun).toFloat())
    private val pctRemaining =
        if (runCapacity > 0)
            remainingSecs / runCapacity.toFloat() * 100f
        else 0f
    val propaneRemaining = "Propane: "+pctRemaining.toInt()+"%"
    val percentFull = pctRemaining.toInt()

    fun onClickRefill(view: View?) {
        view?.let {
            AlertDialog.Builder(view.context)
                .setMessage(view.context.getString(R.string.control_refill_confirm))
                .setPositiveButton(R.string.yes) { _, _ -> dispatcher.post(ThermostatEvent.OnClickRefill) }
                .setNegativeButton(R.string.no) { _, _ -> }
                .show()
        }
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is PropaneDiffableItem &&
                other.showPropane == showPropane &&
                other.propaneRemaining == propaneRemaining
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is PropaneDiffableItem
    }
}