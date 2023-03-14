package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.PyDevice
import com.edwardmcgrath.blueflux.core.Event

internal sealed class ThermostatEvent: Event {
    data class OnClickRefill(val pyDevice: PyDevice, val controlId: Int): ThermostatEvent()
    data class ShowNetworkError(val throwable: Throwable, val finish: Boolean) : ThermostatEvent()

    data class RequestEnableStat(val pyDevice: PyDevice, val statId: Int): ThermostatEvent()
    data class RequestDisableStat(val pyDevice: PyDevice, val statId: Int): ThermostatEvent()

    data class RequestIncreaseTemp(val pyDevice: PyDevice, val statId: Int): ThermostatEvent()
    data class RequestDecreaseTemp(val pyDevice: PyDevice, val statId: Int): ThermostatEvent()

    data class RequestHistoryBefore(val pyDevice: PyDevice, val statId: Int, val beforeTs: Int): ThermostatEvent()

}