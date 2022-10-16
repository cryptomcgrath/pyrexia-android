package com.cryptomcgrath.pyrexia.thermostat

import android.content.Context
import android.util.Log
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.RxStore

internal class HistoryChartDiffableItem(context: Context,
                                        store: RxStore<ThermostatState>): DiffableItem {
    private val history = store.state.historyOldtoNew

    val series = mutableListOf<PointsChart.Series>()
    val yLabels = mutableListOf<PointsChart.Label>()

    val bgColor = R.color.white

    init {
        var onPoints = mutableListOf<PointsChart.Point>()
        history.forEach {
            if (it.controlOn) {
                onPoints.add(
                    PointsChart.Point(
                        name = it.programAction.name,
                        x = it.actionTs.toDouble(),
                        y = it.sensorValue.toDouble()
                    )
                )
            } else {
                if (onPoints.isNotEmpty()) {
                    series.add(
                        PointsChart.Series(
                            points = onPoints,
                            color = R.color.heating,
                            lineWidth = context.resources.getDimension(R.dimen.pointschart_commandon_line_width),
                            label = ""
                        )
                    )
                    onPoints = mutableListOf<PointsChart.Point>()
                }
            }
        }
        // if currently on add points
        if (onPoints.isNotEmpty()) {
            series.add(
                PointsChart.Series(
                    points = onPoints,
                    color = R.color.heating,
                    lineWidth = context.resources.getDimension(R.dimen.pointschart_commandon_line_width),
                    label = ""
                )
            )
        }
        val points = history.map {
            Log.d(TAG, "point xstr=${it.actionTs} x=${it.actionTs.toDouble()} y=${it.sensorValue}")
            PointsChart.Point(
                name = it.programAction.name,
                x = it.actionTs.toDouble(),
                y = it.sensorValue.toDouble()
            )
        }
        series.add(PointsChart.Series(
            points = points,
            label = "",
            color = R.color.grey42,
            lineWidth = context.resources.getDimension(R.dimen.pointschart_default_line_width)
        ))
    }

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is HistoryChartDiffableItem &&
                other.history == history
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is HistoryChartDiffableItem
    }
}
