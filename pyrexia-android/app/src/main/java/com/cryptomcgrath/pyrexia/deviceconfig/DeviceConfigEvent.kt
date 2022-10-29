package com.cryptomcgrath.pyrexia.deviceconfig

import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.edwardmcgrath.blueflux.core.Event

internal sealed class DeviceConfigEvent: Event {

    data class Init(val pyDevice: PyDevice): DeviceConfigEvent()
    data class NewSensors(val sensors: List<Sensor>): DeviceConfigEvent()
    data class NewControls(val controls: List<Control>): DeviceConfigEvent()
    data class NewStats(val stats: List<ProgramRun>): DeviceConfigEvent()
}