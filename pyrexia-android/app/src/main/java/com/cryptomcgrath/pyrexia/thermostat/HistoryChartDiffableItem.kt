package com.cryptomcgrath.pyrexia.thermostat

import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.RxStore

internal class HistoryChartDiffableItem(store: RxStore<ThermostatState>): DiffableItem {

    val points = store.state.historyOldtoNew.joinToString(",") {
        "${it.actionTs},${it.sensorValue}"
    }

    val bgColor = R.color.cobalt

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is HistoryChartDiffableItem &&
                other.points == points
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is HistoryChartDiffableItem
    }
}