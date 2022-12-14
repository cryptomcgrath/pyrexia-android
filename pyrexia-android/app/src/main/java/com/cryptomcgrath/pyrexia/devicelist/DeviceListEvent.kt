package com.cryptomcgrath.pyrexia.devicelist

import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.statlist.StatListEvent
import com.edwardmcgrath.blueflux.core.Event

internal sealed class DeviceListEvent : Event {
    data class NewDeviceList(val deviceList: List<PyDevice>) : DeviceListEvent()
    data class DatabaseError(val throwable: Throwable) : DeviceListEvent()
    data class GoToStatList(val pyDevice: PyDevice) : DeviceListEvent()
    data class AddDevice(val pyDevice: PyDevice) : DeviceListEvent()
    object AddEmptyItem : DeviceListEvent()
    object CancelEmptyItem : DeviceListEvent()
    data class ForgetDevice(val pyDevice: PyDevice) : DeviceListEvent()
    data class GoToDeviceConfig(val pyDevice: PyDevice): DeviceListEvent()

}