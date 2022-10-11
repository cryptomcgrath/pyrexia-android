package com.cryptomcgrath.pyrexia.thermostat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.cryptomcgrath.pyrexia.R
import kotlin.math.abs

class PointsChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bounds = RectF()
    private val plotBounds = RectF()
    private val margin=20f

    private var bgPaint = Paint()

    private val xLabels: MutableList<Label> = mutableListOf()
    private val yLabels: MutableList<Label> = mutableListOf()
    private val data: MutableList<DrawSeries> = mutableListOf()

    private val dataBounds = RectD()

    fun addLabelX(label: Label) {
        xLabels.add(label)
    }

    fun addLabelY(label: Label) {
        yLabels.add(label)
    }

    fun addSeries(seriesList: List<Series>) {
        data.addAll(
            seriesList.map { series ->
                DrawSeries(
                    series = series,
                    plotPaint = Paint().apply {
                        color = ContextCompat.getColor(context, series.color)
                        isAntiAlias = true
                        strokeWidth = series.lineWidth //resources.getDimension(R.dimen.pointschart_default_line_width)
                        style = Paint.Style.STROKE
                    }
                )
            }
        )
        computePoints()
        invalidate()
        requestLayout()
    }

    var bgColor: Int = R.color.white
        set(value) {
            bgPaint = Paint().apply {
                color = ContextCompat.getColor(context, value)
                isAntiAlias = true
            }
            field = value
            invalidate()
            requestLayout()
        }

    init {
        setWillNotDraw(false)
        isSaveEnabled = true

        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PointsChart)

        bgColor = typedArray.getInteger(R.styleable.PointsChart_bgColor, R.color.white)

        typedArray.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        bounds.set(
            left.toFloat(),
            top.toFloat(),
            right.toFloat(),
            bottom.toFloat())

        plotBounds.set(
            left.toFloat() + margin,
            top.toFloat() + margin,
            right.toFloat() - margin,
            bottom.toFloat() - margin
        )
        computePoints()
    }

    private fun autoScale() {
        val minX = data.map {
            it.series.points
        }.flatten().minByOrNull {
            it.x
        }?.x

        val maxX = data.map {
            it.series.points
        }.flatten().maxByOrNull {
            it.x
        }?.x

        val minY = data.map {
            it.series.points
        }.flatten().minByOrNull {
            it.y
        }?.y

        val maxY = data.map {
            it.series.points
        }.flatten().maxByOrNull {
            it.y
        }?.y

        if (minX != null && maxX != null && minY != null && maxY != null) {
            dataBounds.set(
                minX,
                minY,
                maxX,
                maxY
            )
        }
    }

    private fun computePoints() {
        autoScale()

        data.forEach {
            var j=0
            var xPrev = 0f
            var yPrev = 0f
            it.packedPoints = FloatArray(it.series.points.size * 4)
            it.series.points.forEachIndexed { idx, p ->
                val x = ((p.x - dataBounds.left) / dataBounds.width() * plotBounds.width() + margin).toFloat()
                val y = ((plotBounds.height() - ((p.y - dataBounds.top) / dataBounds.height() * plotBounds.height())) + margin).toFloat()
                if (idx == 0) {
                    it.packedPoints[j++] = x
                    it.packedPoints[j++] = y
                } else {
                    it.packedPoints[j++] = xPrev
                    it.packedPoints[j++] = yPrev
                }
                it.packedPoints[j++] = x
                xPrev = x
                it.packedPoints[j++] = y
                yPrev = y
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(bounds, bgPaint)

        data.forEach {
            canvas.drawLines(it.packedPoints, it.plotPaint)
        }
    }

    data class Point(
        val x: Double,
        val y: Double,
        val name: String)

    data class Series(
        val points: List<Point>,
        val label: String,
        val color: Int,
        val lineWidth: Float
    )

    data class Label(
        val position: Float,
        val name: String)

    private data class DrawSeries(
        val series: Series,
        var packedPoints: FloatArray = floatArrayOf(),
        val plotPaint: Paint = Paint()
    )
}

class RectD {
    var bottom: Double = 0.0
    var left: Double = 0.0
    var right: Double = 0.0
    var top: Double = 0.0

    fun width(): Double = abs(right - left)

    fun height(): Double = abs(top - bottom)

    fun set(l: Double, t: Double, r: Double, b: Double) {
        left = l
        top = t
        right = r
        bottom = b
    }
}
