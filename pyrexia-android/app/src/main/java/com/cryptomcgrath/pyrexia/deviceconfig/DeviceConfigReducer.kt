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

        is DeviceConfigEvent.NewSensors -> {
            state.copy(
                sensors = event.sensors
            )
        }

        else -> state
    }
}