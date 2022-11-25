package com.cryptomcgrath.pyrexia.deviceconfig

import com.edwardmcgrath.blueflux.core.ReducerFun

internal val deviceConfigReducerFun: ReducerFun<DeviceConfigState> = { inState, event ->
    val state = inState ?: DeviceConfigState()

    when (event) {
        is DeviceConfigEvent.Init -> {
            state.copy(
                pyDevice = event.pyDevice
            )
        }

        is DeviceConfigEvent.NewDeviceConfig -> {
            state.copy(
                loading = false,
                stats = event.stats,
                sensors = event.sensors,
                controls = event.controls
            )
        }

        is DeviceConfigEvent.NetworkError -> {
            state.copy(
                loading = false
            )
        }

        is DeviceConfigEvent.SetLoading -> {
            state.copy(
                loading = event.loading
            )
        }

        else -> state
    }
}