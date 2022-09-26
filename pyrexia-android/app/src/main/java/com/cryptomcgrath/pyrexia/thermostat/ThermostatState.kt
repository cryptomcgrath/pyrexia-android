package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.Program
import com.edwardmcgrath.blueflux.core.State

internal data class ThermostatState(
    val programs: List<Program> = emptyList(),
    val selectedProgramId: Int? = null
): State {
    val program = programs.firstOrNull {
        it.id == selectedProgramId
    }
}