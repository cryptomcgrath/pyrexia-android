package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.model.VirtualStat
import com.edwardmcgrath.blueflux.core.Event

internal sealed class ThermostatEvent: Event {
    data class Init(val id:Int) : ThermostatEvent()
    data class NewStatList(val statList: List<VirtualStat>): ThermostatEvent()
    data class ConnectionError(val throwable: Throwable): ThermostatEvent()
    data class SetLoading(val isLoading: Boolean): ThermostatEvent()
    data class NewHistory(val offset: Int, val historyList: List<History>): ThermostatEvent()
    data class RequestMoreHistory(val timeStamp: Long): ThermostatEvent()
    object OnClickRefill: ThermostatEvent()
    data class SetUpdating(val updating: Boolean): ThermostatEvent()
}