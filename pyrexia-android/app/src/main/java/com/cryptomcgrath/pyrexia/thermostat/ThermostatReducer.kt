package com.cryptomcgrath.pyrexia.thermostat

import com.edwardmcgrath.blueflux.core.ReducerFun

internal val thermostatReducerFun: ReducerFun<ThermostatState> = { inState, event ->
    val state = inState ?: ThermostatState()

    when(event) {
        is ThermostatEvent.NewStatList -> {
            state.copy(
                statList = event.statList,
                connectionError = null
            )
        }

        is ThermostatEvent.ConnectionError -> {
            state.copy(
                connectionError = event.throwable
            )
        }

        is ThermostatEvent.Init -> {
            state.copy(
                selectedStatId = event.id
            )
        }

        is ThermostatEvent.SetLoading -> {
            state.copy(
                isLoading = event.isLoading
            )
        }

        else -> state
    }
}