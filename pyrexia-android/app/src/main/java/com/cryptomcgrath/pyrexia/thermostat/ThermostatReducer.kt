package com.cryptomcgrath.pyrexia.thermostat

import com.edwardmcgrath.blueflux.core.ReducerFun

internal val thermostatReducerFun: ReducerFun<ThermostatState> = { inState, event ->
    val state = inState ?: ThermostatState()

    when(event) {
        is ThermostatEvent.NewStatList -> {
            state.copy(
                statList = event.statList,
                selectedStatId = event.statList.firstOrNull()?.program?.id
            )
        }

        else -> state
    }
}