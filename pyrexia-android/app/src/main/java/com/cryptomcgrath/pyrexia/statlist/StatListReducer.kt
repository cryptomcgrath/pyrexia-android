package com.cryptomcgrath.pyrexia.statlist

import com.edwardmcgrath.blueflux.core.ReducerFun


internal val statListReducerFun: ReducerFun<StatListState> = { inState, event ->
    val state = inState ?: StatListState()

    when(event) {
        is StatListEvent.NewStatList -> {
            state.copy(
                statList = event.statList,
                connectionError = null,
                isLoading = false
            )
        }

        is StatListEvent.ConnectionError -> {
            state.copy(
                connectionError = event.throwable,
                isLoading = false
            )
        }

        is StatListEvent.SetLoading -> {
            state.copy(
                //connectionError = null,
                isLoading = event.isLoading
            )
        }

        else -> state
    }
}