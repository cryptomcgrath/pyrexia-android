package com.cryptomcgrath.pyrexia.thermostat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.cryptomcgrath.pyrexia.R

class PointsChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bounds = RectF()
    private val plotBounds = RectF()
    private val margin=20f

    private var dataPoints = ArrayList<Pair<Float,Float>>(MAX_POINTS)
    private val drawPoints = ArrayList<PointF>(MAX_POINTS)
    private var drawPointsPacked = FloatArray(MAX_POINTS)

    private var numPoints = 0

    private var bgPaint = Paint()
    private var plotPaint = Paint()

    var points: String = ""
        set(value) {
            val ps = value.split(",")
            if (ps.size > 1) {
                dataPoints.clear()
                for (i in ps.indices step 2) {
                    val x = ps[i].toLongOrNull()
                    val y = ps[i+1].toFloatOrNull()
                    if (x != null && y != null) {
                        dataPoints.add(Pair(x.toFloat(),y))
                        Log.d(TAG, "datapoint i=$i $x,$y")
                    }
                }
                computePoints()
            }

            field = value
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

    var lineWidth: Float = resources.getDimension(R.dimen.pointschart_default_line_width)
        set(value) {
            plotPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.red_error)
                isAntiAlias = true
                strokeWidth = value
                style = Paint.Style.STROKE
            }
            field = value
            invalidate()
            requestLayout()
        }

    init {
        setWillNotDraw(false)
        isSaveEnabled = true

        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PointsChart)

        points = typedArray.getString(R.styleable.PointsChart_points).orEmpty()
        bgColor = typedArray.getInteger(R.styleable.PointsChart_bgColor, R.color.white)
        lineWidth = typedArray.getDimension(R.styleable.PointsChart_lineWidth, resources.getDimension(R.dimen.pointschart_default_line_width))

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

    private fun computePoints() {
        val minX = dataPoints.minByOrNull {
            it.first
        }?.first
        val maxX = dataPoints.maxByOrNull {
            it.first
        }?.first
        val minY = dataPoints.minByOrNull {
            it.second
        }?.second
        val maxY = dataPoints.maxByOrNull {
            it.second
        }?.second

        if (minX != null && maxX != null && minY != null && maxY != null) {
            var j=0
            var xPrev = 0f
            var yPrev = 0f
            drawPointsPacked = FloatArray(dataPoints.size*4)
            dataPoints.forEachIndexed { idx, it ->
                val x = (it.first - minX) / (maxX - minX) * plotBounds.width() + margin
                val y = (it.second - minY) / (maxY - minY) * plotBounds.height() + margin
                Log.d(TAG, "drawpoint $x,$y")
                drawPoints.add(PointF(x,y))
                if (idx == 0) {
                    drawPointsPacked[j++] = x
                    drawPointsPacked[j++] = y
                } else {
                    drawPointsPacked[j++] = xPrev
                    drawPointsPacked[j++] = yPrev
                }
                drawPointsPacked[j++] = x
                xPrev = x
                drawPointsPacked[j++] = y
                yPrev = y
            }
            numPoints = dataPoints.size
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(bounds, bgPaint)
        canvas.drawLines(drawPointsPacked, plotPaint)
    }

    interface Point {
        val x: Float
        val y: Float
        val name: String
    }

    interface Series {
        val points: List<Point>
        val label: String
        val color: Int
    }

    interface Label {
        val position: Float
        val name: String
    }
}

private const val MAX_POINTS = 200

