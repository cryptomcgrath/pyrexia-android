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
    private val margin=resources.getDimension(R.dimen.pointschart_default_margin)

    private var bgPaint = Paint()

    private val xLabels: MutableList<DrawLabel> = mutableListOf()
    private val yLabels: MutableList<DrawLabel> = mutableListOf()
    private val data: MutableList<DrawSeries> = mutableListOf()

    private val dataBounds = RectD()

    fun addLabelX(label: Label) {
        xLabels.add(
            DrawLabel(
                label = label,
                plotPaint = Paint().apply {
                    color = ContextCompat.getColor(context, R.color.grey42)
                    isAntiAlias = true
                    strokeWidth = resources.getDimension(R.dimen.pointschart_thin_line_width)
                    style = Paint.Style.STROKE
                }
            ))
    }

    fun addLabelY(label: Label) {
        yLabels.add(
            DrawLabel(
                label = label,
                plotPaint = Paint().apply {
                    color = ContextCompat.getColor(context, R.color.grey42)
                    isAntiAlias = true
                    strokeWidth = resources.getDimension(R.dimen.pointschart_thin_line_width)
                    style = Paint.Style.STROKE
                }
            ))
    }

    fun addSeries(seriesList: List<Series>) {
        data.addAll(
            seriesList.map { series ->
                DrawSeries(
                    series = series,
                    plotPaint = Paint().apply {
                        color = ContextCompat.getColor(context, series.color)
                        isAntiAlias = true
                        strokeWidth = series.lineWidth
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
            left.toFloat(),
            top.toFloat() + margin,
            right.toFloat(),
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
                val x = p.x.scaleX()
                val y = p.y.scaleY()
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

        yLabels.clear()
        yLabels.add(
            DrawLabel(
                label = Label(position = dataBounds.top.toFloat(), name = String.format("%3.2f", dataBounds.top)),
                packedPoints = floatArrayOf(
                    dataBounds.left.scaleX(),
                    dataBounds.top.scaleY(),
                    dataBounds.right.scaleX(),
                    dataBounds.top.scaleY()
                )
            ))
        yLabels.add(
            DrawLabel(
                label = Label(position = dataBounds.bottom.toFloat(), name = String.format("%3.2f", dataBounds.bottom)),
                packedPoints = floatArrayOf(
                    dataBounds.left.scaleX(),
                    dataBounds.bottom.scaleY(),
                    dataBounds.right.scaleX(),
                    dataBounds.bottom.scaleY()
                )
            ))
    }

    private fun Double.scaleY(): Float {
        return ((plotBounds.height() - ((this - dataBounds.top) / dataBounds.height() * plotBounds.height() - margin))).toFloat()
    }

    private fun Double.scaleX(): Float {
        return ((this - dataBounds.left) / dataBounds.width() * plotBounds.width()).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(bounds, bgPaint)

        data.forEach {
            canvas.drawLines(it.packedPoints, it.plotPaint)
        }

        // y labels
        yLabels.forEach {
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

    data class DrawLabel(
        val label: Label,
        val packedPoints: FloatArray = floatArrayOf(),
        val plotPaint: Paint = Paint())

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
