package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.Program
import com.edwardmcgrath.blueflux.core.Event

internal sealed class ThermostatEvent: Event {

    data class NewPrograms(val programs: List<Program>): ThermostatEvent()

}