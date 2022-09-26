package com.cryptomcgrath.pyrexia.thermostat

import com.edwardmcgrath.blueflux.core.ReducerFun

internal val thermostatReducerFun: ReducerFun<ThermostatState> = { inState, event ->
    val state = inState ?: ThermostatState()

    when(event) {
        is ThermostatEvent.NewProgramsRun -> {
            state.copy(
                programsRun = event.programsRun,
                selectedProgramId = event.programsRun.firstOrNull()?.program?.id
            )
        }

        else -> state
    }
}