package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.edwardmcgrath.blueflux.core.Event

internal sealed class ThermostatEvent: Event {

    data class Init(val id:Int) : ThermostatEvent()
    data class NewStatList(val statList: List<ProgramRun>): ThermostatEvent()
    data class ConnectionError(val throwable: Throwable): ThermostatEvent()
    data class SetLoading(val isLoading: Boolean): ThermostatEvent()
    data class NewHistory(val offset: Int, val historyList: List<History>): ThermostatEvent()
}