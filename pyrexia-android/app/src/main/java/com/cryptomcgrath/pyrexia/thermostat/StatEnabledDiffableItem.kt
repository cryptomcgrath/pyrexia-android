package com.cryptomcgrath.pyrexia.thermostat

import android.widget.CompoundButton
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.Dispatcher


internal class StatEnabledDiffableItem(val dispatcher: Dispatcher,
                                       val enabled: Boolean,
                                       val program_id: Int) : DiffableItem{

    fun onEnabledChanged(buttonView: CompoundButton, isChecked: Boolean) {
        dispatcher.post(ThermostatViewModel.UiEvent.StatEnable(id = program_id, enable = isChecked))
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is StatEnabledDiffableItem &&
                other.enabled == enabled &&
                other.program_id == program_id
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is StatEnabledDiffableItem
    }
}