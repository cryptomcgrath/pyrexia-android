package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.edwardmcgrath.blueflux.core.State

internal data class ThermostatState(
    val programsRun: List<ProgramRun> = emptyList(),
    val selectedProgramId: Int? = null
): State {
    val program = programsRun.firstOrNull {
        it.program.id == selectedProgramId
    }
}