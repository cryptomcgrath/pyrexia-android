package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.edwardmcgrath.blueflux.core.State
import kotlin.math.abs

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

    val minHistoryTs get() = history.values.minByOrNull {
        it.actionTs
    }?.actionTs

    val nextHistoryOffset = history.values.size+1
}

internal data class Cycle(
    val data: List<History>
) {
    val startTemp: Float = data.firstOrNull()?.sensorValue ?: 0f
    val startTs: Long = data.firstOrNull()?.actionTs ?: 0L
    val endTemp: Float = data.lastOrNull()?.sensorValue ?: 0f
    val endTs: Long = data.lastOrNull()?.actionTs ?: 0L
    val durationSeconds = endTs - startTs
    val deltaT = abs(endTemp - startTemp)
    val q = durationSeconds / deltaT
}

internal fun List<History>.toCycles(): List<Cycle> {
    val cycles = mutableListOf<Cycle>()
    var cycle = mutableListOf<History>()
    this.forEach {
        if (it.controlOn) {
            cycle.add(it)
        } else {
            if (cycle.isNotEmpty()) {
                cycles.add(
                    Cycle(data = cycle)
                )
                cycle = mutableListOf()
            }
        }
    }
    return cycles
}