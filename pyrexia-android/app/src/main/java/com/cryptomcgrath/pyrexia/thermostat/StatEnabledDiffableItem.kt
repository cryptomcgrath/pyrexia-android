package com.cryptomcgrath.pyrexia.thermostat

import android.widget.CompoundButton
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher


internal class StatEnabledDiffableItem(val dispatcher: Dispatcher,
                                       val enabled: Boolean,
                                       val pyDevice: PyDevice,
                                       val statId: Int) : DiffableItem{

    fun onEnabledChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (isChecked) {
            dispatcher.post(ThermostatEvent.RequestEnableStat(pyDevice, statId))
        } else {
            dispatcher.post(ThermostatEvent.RequestDisableStat(pyDevice, statId))
        }
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is StatEnabledDiffableItem &&
                other.enabled == enabled &&
                other.statId == statId
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is StatEnabledDiffableItem
    }
}