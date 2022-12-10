package com.cryptomcgrath.pyrexia.thermostat

import android.content.Context
import android.util.Log
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.RxStore

internal class HistoryChartDiffableItem(val store: RxStore<ThermostatState>): DiffableItem {
    val bgColor = R.color.white

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is HistoryChartDiffableItem
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is HistoryChartDiffableItem
    }
}

internal fun List<History>.toSeries(context: Context): List<PointsChart.Series> {
    val result = mutableListOf<PointsChart.Series>()

    // **** set points plot ****
    val setPoints = this.map {
        PointsChart.Point(
            name = it.programAction.name,
            x = it.actionTs.toDouble(),
            y = it.setPoint.toDouble()
        )
    }
    result.add(PointsChart.Series(
        points = setPoints,
        label = "",
        color = R.color.hilite,
        lineWidth = context.resources.getDimension(R.dimen.pointschart_default_line_width)
    ))

    // **** on points plot ****
    var onPoints = mutableListOf<PointsChart.Point>()
    this.forEach {
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
                result.add(
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
        result.add(
            PointsChart.Series(
                points = onPoints,
                color = R.color.heating,
                lineWidth = context.resources.getDimension(R.dimen.pointschart_commandon_line_width),
                label = ""
            )
        )
    }

    // **** temperature plot ****
    val points = this.map {
        Log.d(TAG, "point xstr=${it.actionTs} x=${it.actionTs.toDouble()} y=${it.sensorValue}")
        PointsChart.Point(
            name = it.programAction.name,
            x = it.actionTs.toDouble(),
            y = it.sensorValue.toDouble()
        )
    }
    result.add(PointsChart.Series(
        points = points,
        label = "",
        color = R.color.grey42,
        lineWidth = context.resources.getDimension(R.dimen.pointschart_default_line_width)
    ))
    return result
}
