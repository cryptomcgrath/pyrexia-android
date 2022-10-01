package com.cryptomcgrath.pyrexia.devicelist

import com.cryptomcgrath.pyrexia.model.PyDevice
import com.edwardmcgrath.blueflux.core.State

data class DeviceListState(
    val deviceList: List<PyDevice> = listOf(),

    val connectionError: Throwable? = null
) : State {
    fun hasAddEmptyItem(): Boolean {
        return deviceList.any {
            it.name.isEmpty()
        }
    }
}