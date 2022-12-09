package com.cryptomcgrath.pyrexia.thermostat

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import com.cryptomcgrath.pyrexia.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PointsChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {

    private val bounds = RectF()
    private val plotBounds = RectF()
    private val margin=resources.getDimension(R.dimen.pointschart_default_margin)

    private var bgPaint = Paint()

    private val xLabels: MutableList<DrawLabel> = mutableListOf()
    private val yLabels: MutableList<DrawLabel> = mutableListOf()
    private val data: MutableList<DrawSeries> = mutableListOf()

    private val dataBounds = RectD()
    private val allDataBounds = RectD()
    private val textBounds = Rect()

    private val gesture = GestureDetector(context, this)
    private val scaleGesture = ScaleGestureDetector(context, this)
    private var originX = 0f
    private var originY = 0f
    private var scale = 1.0f
    private var minScale = 0.5f
    private var maxScale = 8.0f
    private var markLength = 20f

    var listener: Listener? = null

    private val labelPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.grey42)
        isAntiAlias = true
        textSize = margin
    }

    private val dashPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.grey42)
        isAntiAlias = true
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 40f), 0f)
    }

    private val circlePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.hilite)
        isAntiAlias = true
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    fun setSeriesData(seriesList: List<Series>) {
        synchronized(data) {
            data.clear()
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
            computeLabels()
            invalidate()
            requestLayout()
        }
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
        computeLabels()
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

        var minY = data.map {
            it.series.points
        }.flatten().minByOrNull {
            it.y
        }?.y

        var maxY = data.map {
            it.series.points
        }.flatten().maxByOrNull {
            it.y
        }?.y

        // add top and bottom margin of temperature
        if (minY != null && maxY != null) {
            minY -= (maxY - minY)
            maxY += (maxY - minY)
        }

        if (minX != null && maxX != null && minY != null && maxY != null) {
            allDataBounds.set(
                minX,
                minY,
                maxX,
                maxY
            )
            if (dataBounds.height() < 1) {
                dataBounds.set(
                    minX,
                    minY,
                    maxX,
                    maxY
                )
            }
        }
    }

    private fun computePoints() {
        autoScale()

        data.forEach {
            var j = 0
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

    }

    private fun computeLabels() {
        yLabels.clear()
        yLabels.add(
            DrawLabel(
                label = Label(position = dataBounds.top.toFloat(), name = String.format("%3.2f", dataBounds.top)),
                startPoint = PointF(dataBounds.left.scaleNoPanX(), dataBounds.top.scaleYNoPan()),
                endPoint = PointF(dataBounds.right.scaleNoPanX(), dataBounds.top.scaleYNoPan())
            ))
        yLabels.add(
            DrawLabel(
                label = Label(position = dataBounds.bottom.toFloat(), name = String.format("%3.2f", dataBounds.bottom)),
                startPoint = PointF(dataBounds.left.scaleNoPanX(), dataBounds.bottom.scaleYNoPan()),
                endPoint = PointF(dataBounds.right.scaleNoPanX(), dataBounds.bottom.scaleYNoPan())
            ))

        xLabels.clear()
        xLabels.add(
            DrawLabel(
                label = Label(position = dataBounds.left.toFloat(), name = dataBounds.left.toLong().toTimeLabel()),
                startPoint = PointF(dataBounds.left.scaleX(), dataBounds.bottom.scaleY()),
                endPoint = PointF(dataBounds.left.scaleX(), dataBounds.bottom.scaleY()+margin)
            )
        )
        xLabels.add(
            DrawLabel(
                label = Label(position = dataBounds.right.toFloat(), name = dataBounds.right.toLong().toTimeLabel()),
                startPoint = PointF(dataBounds.right.scaleX(), dataBounds.bottom.scaleY()),
                endPoint = PointF(dataBounds.right.scaleX(), dataBounds.bottom.scaleY()+margin)
            )
        )
    }

    private fun Double.scaleY(): Float {
        return ((plotBounds.height()*scale - ((this - dataBounds.top) / dataBounds.height() * (plotBounds.height() * scale - margin))).toFloat() - originY)
    }

    private fun Double.scaleYNoPan(): Float {
        return ((plotBounds.height() *scale - ((this - dataBounds.top) / dataBounds.height() * scale * plotBounds.height() - margin))).toFloat()
    }

    private fun Float.unScaleY(): Double {
        return (plotBounds.height() * scale - (this + originY + margin)) / (plotBounds.height() * scale) * dataBounds.height() + dataBounds.top
    }

    private fun Double.scaleX(): Float {
        return ((this - dataBounds.left) / dataBounds.width() * plotBounds.width() * scale).toFloat() - originX
    }

    private fun Float.unScaleX(): Double {
        return ((this + originX)/ (plotBounds.width() * scale)) * (dataBounds.right - dataBounds.left) + dataBounds.left
    }

    private fun Double.scaleNoPanX(): Float {
        return ((this - dataBounds.left) / dataBounds.width() * plotBounds.width() * scale).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(bounds, bgPaint)

        synchronized(data) {
            // hilite
            val midX = plotBounds.width()/2
            data.forEach {
                if (it.series.color == R.color.heating && it.packedPoints.size >= 4) {
                    val x1 = it.packedPoints[0]
                    val x2 = it.packedPoints[it.packedPoints.size-2]
                    if (x1 < midX && x2 >= midX) {
                        val y1 = it.packedPoints[1]
                        val y2 = it.packedPoints[it.packedPoints.size-1]
                        canvas.drawOval(x1-margin, y1+margin, x2+margin, y2-margin, circlePaint)
                    }
                }

            }

            data.forEach {
                canvas.drawLines(it.packedPoints, it.plotPaint)
            }

            // y labels
            labelPaint.getTextBounds("12:00p", 0, "12:00p".length, textBounds)
            val ySpacing = (abs(textBounds.top) * 5).toInt()
            //yLabels.forEach {
            //    canvas.drawLine(it.startPoint.x, it.startPoint.y, it.endPoint.x, it.endPoint.y, it.plotPaint)
            //    canvas.drawText(it.label.name, it.startPoint.x, it.startPoint.y, labelPaint)
            //}
            for (y in plotBounds.top.toInt() until plotBounds.bottom.toInt() step ySpacing) {
                canvas.drawText(
                    y.toFloat().unScaleY().toTempLabel(),
                    1f,
                    y.toFloat(),
                    labelPaint
                )
                canvas.drawLine(1f, y.toFloat(), plotBounds.right, y.toFloat(), dashPaint)
            }

            // x labels
            val xSpacing = (textBounds.right * 1.5).toInt()
            for (x in plotBounds.left.toInt() until plotBounds.right.toInt() step xSpacing) {
                textBounds.drawTextCentered(
                    canvas,
                    labelPaint,
                    x.toFloat().unScaleX().toLong().toTimeLabel(),
                    x.toFloat(),
                    plotBounds.bottom+margin/2,
                )
                canvas.drawLine(x.toFloat(), plotBounds.bottom - markLength, x.toFloat(), plotBounds.bottom + markLength, labelPaint)
            }
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
        val startPoint: PointF,
        val endPoint: PointF,
        val plotPaint: Paint = Paint())

    private data class DrawSeries(
        val series: Series,
        var packedPoints: FloatArray = floatArrayOf(),
        val plotPaint: Paint = Paint()
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        // try using the scroll/fling/tap/double-tap first
        if (!gesture.onTouchEvent(event)) {
            // if not handling, send it through to the zoom gesture
            return scaleGesture.onTouchEvent(event)
        }
        return true
    }

    // ** gesture detector **
    override fun onDown(p0: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(p0: MotionEvent) {
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, dx: Float, dy: Float): Boolean {
        originX += dx
        originY += dy
        computePoints()
        checkForRequestMoreData()
        invalidate()
        return true
    }

    private fun checkForRequestMoreData() {
        // check to request more data
        val minTsInView = plotBounds.left.unScaleX().toLong()
        Log.d(TAG, "checkForRequestMoreData minTsInView=${minTsInView} allDataBounds.left = ${allDataBounds.left}")
        if (minTsInView < allDataBounds.left) {
            listener?.onMoreDataRequest(minTsInView)
        }
    }

    override fun onLongPress(p0: MotionEvent) {
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return false
    }

    // ** scale detector **
    override fun onScale(s: ScaleGestureDetector): Boolean {
        if (s.scaleFactor < 0.01) return false // ignore small changes

        scale = min(
            max(scale * s.scaleFactor, minScale),
            maxScale
        )

        Log.d(TAG, "onScale scale=${scale} originX=${originX} originY=${originY}")
        computePoints()
        invalidate()

        return true
    }

    override fun onScaleBegin(p0: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(s: ScaleGestureDetector) {
        scale = min(max(scale*s.scaleFactor, minScale), maxScale)
        computePoints()
        invalidate();
    }

    interface Listener {
        fun onMoreDataRequest(timeStamp: Long)
    }
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

private val labelTimeFormatter by lazy {
    SimpleDateFormat("h:mma", Locale.US)
}

internal fun Long.toTimeLabel(): String {
    return labelTimeFormatter.format(this*1000)
        .replace("AM","a")
        .replace("PM","p")
}

private fun Double.toTempLabel(): String {
    return "%3.2f".format(this)
}
