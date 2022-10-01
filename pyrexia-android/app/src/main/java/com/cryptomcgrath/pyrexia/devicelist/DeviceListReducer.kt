package com.cryptomcgrath.pyrexia.devicelist

import com.cryptomcgrath.pyrexia.model.PyDevice
import com.edwardmcgrath.blueflux.core.ReducerFun

internal val deviceListReducerFun: ReducerFun<DeviceListState> = { inState, event ->
    val state = inState ?: DeviceListState()

    when (event) {
        is DeviceListEvent.GoToStatList -> {
            // remove empty add item, if any
            state.copy(
                deviceList = state.deviceList.filter {
                    it.uid != 0
                }
            )
        }

        is DeviceListEvent.CancelEmptyItem -> {
            state.copy(
                deviceList = state.deviceList.filter {
                    it.uid != 0
                }
            )
        }

        is DeviceListEvent.NewDeviceList -> {
            val newList = event.deviceList.toMutableList()
            // if we do not have any devices setup, then add empty one
            if (newList.isEmpty()) {
                newList.add(PyDevice())
            }
            state.copy(
                deviceList = newList,
                connectionError = null
            )
        }

        is DeviceListEvent.DatabaseError -> {
            state.copy(
                connectionError = event.throwable
            )
        }

        DeviceListEvent.AddEmptyItem -> {
            val newList = state.deviceList.toMutableList()
            newList.add(PyDevice())
            state.copy(
                deviceList = newList
            )
        }

        else -> state
    }
}