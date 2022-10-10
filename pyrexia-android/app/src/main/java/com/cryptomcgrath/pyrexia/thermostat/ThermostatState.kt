package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.edwardmcgrath.blueflux.core.State

internal data class ThermostatState(
    val statList: List<ProgramRun> = emptyList(),
    val selectedStatId: Int? = null,
    val connectionError: Throwable? = null,
    val isLoading: Boolean = false,
    val historyOffset: Int = 0,
    val history: Map<Int, History> = emptyMap()
): State {
    val current = statList.firstOrNull {
        it.program.id == selectedStatId
    }

    val historyOldtoNew get() = history.values.sortedBy {
        it.actionTs
    }
}