package com.cryptomcgrath.pyrexia.deviceconfig

import com.cryptomcgrath.pyrexia.model.Control
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.model.Sensor
import com.edwardmcgrath.blueflux.core.Event

internal sealed class DeviceConfigEvent: Event {

    data class Init(val pyDevice: PyDevice): DeviceConfigEvent()
    data class NetworkError(val throwable: Throwable, val finish: Boolean) : DeviceConfigEvent()

    data class NewDeviceConfig(val stats: List<ProgramRun>,
                               val sensors: List<Sensor>,
                               val controls: List<Control>): DeviceConfigEvent()
    data class GoToSensorEdit(val sensor: Sensor): DeviceConfigEvent()
    data class GoToSensorDelete(val sensor: Sensor) : DeviceConfigEvent()

    data class GoToControlEdit(val control: Control): DeviceConfigEvent()
    data class GoToControlDelete(val control: Control): DeviceConfigEvent()
    data class SetLoading(val loading: Boolean): DeviceConfigEvent()

    data class OnComponentAddSelected(val component: Component): DeviceConfigEvent()
}