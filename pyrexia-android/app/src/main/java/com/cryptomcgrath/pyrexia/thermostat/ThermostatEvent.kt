package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.edwardmcgrath.blueflux.core.Event

internal sealed class ThermostatEvent: Event {

    data class NewStatList(val statList: List<ProgramRun>): ThermostatEvent()

}