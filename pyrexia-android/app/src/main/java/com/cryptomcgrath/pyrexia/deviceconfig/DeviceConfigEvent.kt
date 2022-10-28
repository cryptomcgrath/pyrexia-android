package com.cryptomcgrath.pyrexia.deviceconfig

import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.edwardmcgrath.blueflux.core.Event

internal sealed class DeviceConfigEvent: Event {

    data class Init(val pyDevice: PyDevice): DeviceConfigEvent()
    data class NewSensors(val sensors: List<Sensor>): DeviceConfigEvent()
}