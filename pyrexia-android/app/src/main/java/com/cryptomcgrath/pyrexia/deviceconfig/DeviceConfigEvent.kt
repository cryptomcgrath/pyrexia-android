package com.cryptomcgrath.pyrexia.deviceconfig

import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.cryptomcgrath.pyrexia.model.Sensor
import com.edwardmcgrath.blueflux.core.Event

internal sealed class DeviceConfigEvent: Event {

    data class GoToSensorEdit(val sensor: Sensor): DeviceConfigEvent()
    data class GoToControlEdit(val control: Control): DeviceConfigEvent()
    data class GoToStatEdit(val stat: VirtualStat): DeviceConfigEvent()

    data class OnComponentAddSelected(val component: Component): DeviceConfigEvent()

    data class OnShutdownDevice(val pyDevice: PyDevice): DeviceConfigEvent()

    data class ShowNetworkError(val throwable: Throwable, val finish: Boolean = false): DeviceConfigEvent()

    object OnShutdownCompleted: DeviceConfigEvent()

    data class RequestSensorDelete(val pyDevice: PyDevice, val sensor: Sensor): DeviceConfigEvent()
    data class RequestControlDelete(val pyDevice: PyDevice, val control: Control): DeviceConfigEvent()
    data class RequestStatDelete(val pyDevice: PyDevice, val stat: VirtualStat): DeviceConfigEvent()
}