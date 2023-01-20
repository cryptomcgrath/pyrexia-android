package com.cryptomcgrath.pyrexia.thermostat

import com.edwardmcgrath.blueflux.core.ReducerFun

internal val thermostatReducerFun: ReducerFun<ThermostatState> = { inState, event ->
    val state = inState ?: ThermostatState()

    when(event) {
        is ThermostatEvent.NewStatList -> {
            state.copy(
                statList = event.statList,
                connectionError = null,
                isLoading = false,
                isUpdating = false
            )
        }

        is ThermostatEvent.ConnectionError -> {
            state.copy(
                connectionError = event.throwable,
                isLoading = false,
                isUpdating = false
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

        is ThermostatEvent.NewHistory -> {
            val newHistory = state.history.toMutableMap()
            event.historyList.forEach {
                newHistory[it.id] = it
            }
            state.copy(
                history = newHistory,
                historyOffset = event.offset
            )
        }

        is ThermostatEvent.SetUpdating -> {
            state.copy(
                isUpdating = event.updating
            )
        }

        else -> state
    }
}