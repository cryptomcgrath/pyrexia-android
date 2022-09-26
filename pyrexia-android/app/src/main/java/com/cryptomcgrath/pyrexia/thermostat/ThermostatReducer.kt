package com.cryptomcgrath.pyrexia.thermostat

import com.edwardmcgrath.blueflux.core.ReducerFun

internal val thermostatReducerFun: ReducerFun<ThermostatState> = { inState, event ->
    val state = inState ?: ThermostatState()

    when(event) {
        is ThermostatEvent.NewPrograms -> {
            state.copy(
                programs = event.programs,
                selectedProgramId = event.programs.firstOrNull()?.id
            )
        }

        else -> state
    }
}