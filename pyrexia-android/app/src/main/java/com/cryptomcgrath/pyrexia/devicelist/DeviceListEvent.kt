package com.cryptomcgrath.pyrexia.devicelist

import com.cryptomcgrath.pyrexia.model.PyDevice
import com.edwardmcgrath.blueflux.core.Event

internal sealed class DeviceListEvent : Event {
    data class GoToStatList(val pyDevice: PyDevice) : DeviceListEvent()
    data class GoToDeviceConfig(val pyDevice: PyDevice): DeviceListEvent()
}