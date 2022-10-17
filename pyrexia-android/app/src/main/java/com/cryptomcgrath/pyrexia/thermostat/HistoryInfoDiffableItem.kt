package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.util.DiffableItem
import java.text.SimpleDateFormat
import java.util.Locale

internal class HistoryInfoDiffableItem(val history: List<History>) : DiffableItem {
    private val historySorted = history.sortedBy {
        it.actionTs
    }
    val numPoints = "%d points".format(history.size)

    private val firstTime = historySorted.firstOrNull()?.actionTs?.toTimeString()
    private val firstDay = historySorted.firstOrNull()?.actionTs?.toDayString()
    private val lastTime = historySorted.lastOrNull()?.actionTs?.toTimeString()
    private val lastDay = historySorted.lastOrNull()?.actionTs?.toDayString()

    val timeSpan = when {
        firstDay == lastDay -> "$firstDay $firstTime to $lastTime"
        else -> "$firstDay $firstTime to $lastDay $lastTime"
    }

    private val cycles = history.toCycles()

    private val numCycles = cycles.size
    private val averageCycleTime = cycles.map {
        it.durationSeconds
    }.average().toInt()
    private val averageDeltaT = cycles.map {
        it.deltaT
    }.average()

    val cyclesSummaryText = "Cycles %d, avg ΔT %3.1f°F, avg time %ds".format(numCycles, averageDeltaT, averageCycleTime)

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is HistoryInfoDiffableItem &&
                other.history == history
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is HistoryInfoDiffableItem
    }
}

private val dayFormatted by lazy {
    SimpleDateFormat("MMM d", Locale.US)
}

private val timeStampFormatted by lazy {
    SimpleDateFormat("h:mma", Locale.US)
}

private fun Long?.toDayString(): String {
    return if (this != null) dayFormatted.format(this * 1000) else ""
}

private fun Long?.toTimeString(): String {
    return if (this != null)
        timeStampFormatted.format(this*1000)
            .replace("AM", "a")
            .replace("PM", "p")
    else ""
}

