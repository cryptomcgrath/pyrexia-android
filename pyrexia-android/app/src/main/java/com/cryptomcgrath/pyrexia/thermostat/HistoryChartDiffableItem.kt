package com.cryptomcgrath.pyrexia.thermostat

import android.util.Log
import com.cryptomcgrath.pyrexia.CentralState
import com.cryptomcgrath.pyrexia.R
import com.cryptomcgrath.pyrexia.model.History
import com.cryptomcgrath.pyrexia.model.Program
import com.cryptomcgrath.pyrexia.util.DiffableItem
import com.edwardmcgrath.blueflux.core.RxStore

internal class HistoryChartDiffableItem(val store: RxStore<CentralState>): DiffableItem {
    val bgColor = R.color.white

    override fun areContentsTheSame(other: DiffableItem): Boolean {
        return other is HistoryChartDiffableItem
    }

    override fun areItemsTheSame(other: DiffableItem): Boolean {
        return other is HistoryChartDiffableItem
    }
}

internal fun List<History>.toSeries(mode: Program.Mode?): List<PointsChart.Series> {
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
        type = PointsChart.Series.Type.SET_POINT
    ))

    // **** on points plot ****
    var onPoints = mutableListOf<PointsChart.Point>()
    this.forEach {
        if (it.controlOn) {
            onPoints.add(
                PointsChart.Point(
                    name = it.programAction.name,
                    x = it.actionTs.toDouble(),
                    y = it.sensorValue.toDouble(),
                )
            )
        } else {
            if (onPoints.isNotEmpty()) {
                result.add(
                    PointsChart.Series(
                        points = onPoints,
                        type = if (mode == Program.Mode.COOL)
                            PointsChart.Series.Type.ON_COOL
                        else
                            PointsChart.Series.Type.ON_HEAT
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
                type = if (mode == Program.Mode.COOL)
                    PointsChart.Series.Type.ON_COOL
                else
                    PointsChart.Series.Type.ON_HEAT
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
        type = PointsChart.Series.Type.TEMP
    ))
    return result
}

private const val TAG="HistoryChartDiffableItem"
