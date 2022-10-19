package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.model.ProgramRun
import com.edwardmcgrath.blueflux.core.State
import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.max

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
    val data: List<History>,
    val tailTemp: Float
) {
    val startTemp: Float = data.firstOrNull()?.sensorValue ?: 0f
    val startTs: Long = data.firstOrNull()?.actionTs ?: 0L
    val endTemp: Float = data.lastOrNull()?.sensorValue ?: 0f
    val endTs: Long = data.lastOrNull()?.actionTs ?: 0L
    val runTime = endTs - startTs
    val deltaT = abs(endTemp - startTemp)
    val deltaTail = abs(tailTemp - startTemp)

    val q = deltaTail / (runTime.toFloat() / 60f)

    enum class State {
        NONE,
        RUN,
        WAIT
    }
}

internal fun List<History>.toCycles(mode: Program.Mode): List<Cycle> {
    val cycles = mutableListOf<Cycle>()
    var cycle = mutableListOf<History>()
    var state = Cycle.State.NONE
    var tailTemp: Float? = null
    var tailStartTs: Long = 0L
    var cnt = 0
    this.forEach {
        when (state) {
            Cycle.State.RUN -> {
                cycle.add(it)
            }

            Cycle.State.WAIT -> {
                tailTemp?.let { tt ->
                    if (it.actionTs - tailStartTs < Q_TAIL_SECS) {
                        if (mode == Program.Mode.HEAT) {
                            tailTemp = max(tt, it.sensorValue)
                        } else if (mode == Program.Mode.COOL) {
                            tailTemp = min(tt, it.sensorValue)
                        }
                    }
                }
            }

            else -> Unit
        }
        when (it.programAction) {
            History.Action.COMMAND_ON -> {
                state = Cycle.State.RUN
                cycle = mutableListOf()
                tailTemp?.let { tt ->
                    cycles[cnt-1] = cycles[cnt-1].copy(
                        tailTemp = tt
                    )
                }
            }
            History.Action.COMMAND_OFF -> {
                state = Cycle.State.WAIT
                cycles.add(Cycle(data = cycle, tailTemp = it.sensorValue))
                tailTemp = it.sensorValue
                tailStartTs = it.actionTs
                cnt++
            }
            else -> Unit
        }
    }
    if (cnt > 0) {
        tailTemp?.let { tt ->
            cycles[cnt-1] = cycles[cnt-1].copy(
                tailTemp = tt
            )
        }
    }
    return cycles
}

// time after COMMAND_OFF to measure temperature rise/fall for performance (q)
private const val Q_TAIL_SECS = 300