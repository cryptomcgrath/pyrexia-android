package com.cryptomcgrath.pyrexia.devicelist

import com.cryptomcgrath.pyrexia.model.PyDevice
import com.edwardmcgrath.blueflux.core.Event

sealed class DeviceListEvent : Event {
    data class NewDeviceList(val deviceList: List<PyDevice>) : DeviceListEvent()
    data class DatabaseError(val throwable: Throwable) : DeviceListEvent()
    data class GoToStatList(val pyDevice: PyDevice) : DeviceListEvent()
    data class AddDevice(val pyDevice: PyDevice) : DeviceListEvent()
    object AddEmptyItem : DeviceListEvent()
    object CancelEmptyItem : DeviceListEvent()
}