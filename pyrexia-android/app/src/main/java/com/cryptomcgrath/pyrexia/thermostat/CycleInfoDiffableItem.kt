package com.cryptomcgrath.pyrexia.thermostat

import android.content.Context
import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.util.DiffableItem

internal class CycleInfoDiffableItem(
    val context: Context,
    val n: String,
    val runTime: String,
    val deltaT: String,
    val deltaTail: String,
    val waitTime: String,
    val q: String,
    val isTitle: Boolean = false
) : DiffableItem {
    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is CycleInfoDiffableItem &&
                other.n == n &&
                other.runTime == runTime &&
                other.deltaT == deltaT &&
                other.deltaTail == deltaTail &&
                other.waitTime == waitTime &&
                other.q == q
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is CycleInfoDiffableItem
    }
}

internal fun createCycleInfoItems(context: Context,
                                  history: List<History>,
                                  mode: Program.Mode): List<DiffableItem> {
    val cycles = history.toCycles(mode)
    val waitTimes = cycles.mapIndexedNotNull { idx, it ->
        if (idx >= 1) it.startTs - cycles[idx-1].endTs else null
    }
    val items = mutableListOf<DiffableItem>()
    items += CycleInfoDiffableItem(
        context = context,
        n = "n",
        runTime = "run",
        deltaT = "ΔT",
        deltaTail = "ΔTa",
        waitTime = "wait",
        q = "q",
        isTitle = true
    )
    items += CycleInfoDiffableItem(
        context = context,
        n = "Avg",
        runTime = cycles.map {
            it.runTime
        }.average().toInt().secondsToWords(),
        deltaT = "%3.2f°F".format(cycles.map { it.deltaT }.average()),
        deltaTail = "%3.2f°F".format(cycles.map { it.deltaTail }.average()),
        waitTime = waitTimes.average().toInt().secondsToWords(),
        q = "%3.2f".format(cycles.map { it.q }.average()),
        isTitle = true
    )
    items.addAll(cycles.mapIndexed { idx, it ->
        CycleInfoDiffableItem(
            context = context,
            n = it.startTs.toTimeLabel(),
            runTime = it.runTime.toInt().secondsToWords(),
            deltaT = "%3.2f°F".format(it.deltaT),
            deltaTail = "%3.2f°F".format(it.deltaTail),
            waitTime = waitTimes.getOrNull(idx-1)?.toInt()?.secondsToWords() ?: "--",
            q = "%3.2f".format(it.q)
        )
    })
    return items
}