package com.cryptomcgrath.pyrexia.thermostat

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.cryptomcgrath.pyrexia.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt


class ThermostatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // center point of the pie view, set/updated in onLayout
    private var xCenter = 0f
    private var yCenter = 0f

    // radius around the center that we intercept touches
    private var centerRadius = 0f
    private var rimRadius = 0f
    private var markLength = 0f

    private var centerPaint: Paint = Paint()
    private var rimPaint: Paint
    private var tickPaint: Paint
    private var tickLight: Paint
    private var tickDark: Paint

    private var temperaturePaint: Paint
    private var setPointPaint: Paint
    private var temperatureTextSize: Float
    private var setPointTextSize: Float

    var onClickIncreaseListener: OnClickIncreaseListener? = null
    var onClickDecreaseListener: OnClickDecreaseListener? = null

    private val setPointFloat: Float get() {
        return setPoint.replace("°", "").toFloatOrNull() ?: 0f
    }

    private val bounds = RectF()
    private val centerBounds = RectF()
    private val textBounds = Rect()

    var centerColor: Int = ContextCompat.getColor(context, R.color.black)
        @SuppressLint("ResourceAsColor")
        set(value) {
            field = value
            centerPaint = Paint().apply {
                color = ContextCompat.getColor(context, value)
                isAntiAlias = true
            }
            invalidate()
            requestLayout()
        }

    var currentTemp: String = ""
        set(value) {
            field = value

            invalidate()
            requestLayout()
        }

    var setPoint: String = ""
        set(value) {
            field = value

            invalidate()
            requestLayout()
        }

    var bezelWidth: Float = resources.getDimension(R.dimen.thermostatview_default_bezel_width)
        set(value) {
            field = value

            invalidate()
            requestLayout()
        }

    var tickGapDegrees: Int = resources.getInteger(R.integer.thermostatview_default_tick_gap)
        set(value) {
            field = value

            invalidate()
            requestLayout()
        }

    init {
        setWillNotDraw(false)
        isSaveEnabled = true

        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThermostatView)

        bezelWidth = typedArray.getDimension(R.styleable.ThermostatView_bezelWidth, resources.getDimension(R.dimen.thermostatview_default_bezel_width))

        tickGapDegrees = typedArray.getInteger(
            R.styleable.ThermostatView_tickGapDegrees,
            resources.getInteger(R.integer.thermostatview_default_tick_gap))

        centerColor = typedArray.getInteger(R.styleable.ThermostatView_centerColor, R.color.black)
        currentTemp = typedArray.getString(R.styleable.ThermostatView_currentTemp) ?: "---"
        setPoint = typedArray.getString(R.styleable.ThermostatView_setPoint) ?: "---"

        // will be set in onLayout
        temperatureTextSize = 0f
        setPointTextSize = 0f
        temperaturePaint = Paint()
        setPointPaint = Paint()
        rimPaint = Paint()
        tickPaint = Paint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.white)
            strokeWidth = convertDpToPixel(1f, context)
        }
        tickLight = Paint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.light_grey)
            strokeWidth = convertDpToPixel(1f, context)
        }
        tickDark = Paint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.light_grey)
            strokeWidth = convertDpToPixel(3f, context)
        }

        typedArray.recycle()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        xCenter = (r-l)/2f
        yCenter = (b-t)/2f

        centerRadius = xCenter-(bezelWidth*2f)
        rimRadius = xCenter
        markLength = (bezelWidth*2f) / 8

        val margin = (bezelWidth * 2f).toInt()
        val marginHalf = margin / 2

        bounds.set(
            l.toFloat(),
            t.toFloat(),
            r.toFloat(),
            b.toFloat())
        centerBounds.set(
            l.toFloat() + margin + bezelWidth,
            t.toFloat() + margin + bezelWidth,
            r.toFloat() - margin - bezelWidth,
            b.toFloat() - margin - bezelWidth)

        temperatureTextSize = xCenter / 2f
        setPointTextSize = temperatureTextSize * .4f


        rimPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.silver)
            isAntiAlias = true
        }

        setPointPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.white)
            isAntiAlias = true
            textSize = setPointTextSize
        }
        temperaturePaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.white)
            isAntiAlias = true
            textSize = temperatureTextSize
        }
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // use parent width for x and y desired sizes if not specified
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec);

        val xDim = measureDimension(desiredWidth, widthMeasureSpec)
        val yDim = measureDimension(desiredWidth, heightMeasureSpec)
        setMeasuredDimension(xDim, yDim)
    }

    override fun onDraw(canvas: Canvas) {
        // paint bg
        canvas.drawCircle(xCenter, yCenter, rimRadius, centerPaint)

        // rim
        for (i in 1..360 step tickGapDegrees) {
            val r1 = centerRadius
            val t1 = Math.toRadians(i.toDouble())
            val x1 = r1 * cos(t1) + xCenter
            val y1 = r1 * sin(t1) + yCenter
            val r2 = rimRadius
            val x2 = r2 * cos(t1) + xCenter
            val y2 = r2 * sin(t1) + yCenter

            canvas.drawLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), tickLight)
        }

        // center
        canvas.drawCircle(xCenter, yCenter, centerRadius, centerPaint)

        // draw set point tick
        val setPointAngle = setPointFloat.temperatureToDegrees()
        val p1 = computeDestPoint(xCenter, yCenter, setPointAngle, centerRadius - markLength)
        val p2 = computeDestPoint(xCenter, yCenter, setPointAngle, rimRadius)
        val p3 = computeDestPoint(xCenter, yCenter, setPointAngle, centerRadius - setPointTextSize)
        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, tickDark)

        // current temp
        textBounds.drawTextCentered(canvas, temperaturePaint, currentTemp, xCenter, yCenter)

        // set point
        textBounds.drawTextCentered(canvas, setPointPaint, setPoint, p3.x, p3.y)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return handleTouchEvent(event)
    }

    private var touchTheta: Float = 0f
    private var downEvent: MotionEvent? = null

    private fun handleTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_UP -> {
                downEvent?.let {
                    val dist = it.distanceFrom(xCenter.toInt(), yCenter.toInt())
                    when {
                        dist > centerRadius && dist < rimRadius -> {
                            val touchTemperature = touchTheta.degreesToTemperature()
                            if (touchTemperature > setPointFloat) {
                                onClickIncreaseListener?.onClickIncrease()
                            } else if (touchTemperature < setPointFloat) {
                                onClickDecreaseListener?.onClickDecrease()
                            } else {
                                // equals
                            }
                        }

                        dist < centerRadius -> {
                            callOnClick()
                        }

                        else -> return false
                    }
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> return true

            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val dist = event.distanceFrom(xCenter.toInt(), yCenter.toInt())

                when {
                    dist < rimRadius && dist > centerRadius -> {
                        touchTheta = event.angleAroundCenter(xCenter.toInt(), yCenter.toInt()).toFloat()
                        downEvent = event
                    }
                    dist < centerRadius -> {
                        downEvent = event
                        touchTheta = 0f
                    }
                    else -> return false
                }
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = super.onSaveInstanceState()
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    /**
     * Returns the angle in degrees of the MotionEvent around the given
     * center point where 0/360 degrees is up
     *
     *      .
     *      . ME
     *      .   \
     *  180 .     x,y       0
     *      .
     *      .------------
     *
     * @param x coord of the center point
     * @param y coord of the center point
     *
     * @return angle from 0 to 360
     */
    private fun MotionEvent.angleAroundCenter(x: Int, y: Int): Double {
        val dx = this.x - x
        val dy = this.y - y
        var theta = Math.toDegrees(atan2(dy, dx).toDouble())
        if (theta < 0) theta += 360
        if (theta > 360) theta -= 360
        return theta
    }

    /**
     *
     * @param px - x coord of point
     * @param py - y coord of point
     *
     * @return the distance between the specified point and the motion event
     */
    private fun MotionEvent.distanceFrom(px: Int, py: Int): Float {
        return sqrt((px - this.x) * (px - this.x) + (py - this.y) * (py - this.y))
    }

    interface OnClickIncreaseListener {
        fun onClickIncrease()
    }

    interface OnClickDecreaseListener {
        fun onClickDecrease()
    }
}

private const val startDial = -40f
private const val endDial = 115f
private const val dialFactor = (endDial - startDial) / 360f

fun Float.degreesToTemperature(): Float {
    return this * dialFactor + startDial
}

fun Float.temperatureToDegrees(): Float {
    return (this - startDial) / dialFactor
}

fun computeDestPoint(startX: Float, startY: Float, angle: Float, r: Float): PointF {
    val theta = Math.toRadians(angle.toDouble())
    val x = r * cos(theta) + startX
    val y = r * sin(theta) + startY
    return PointF(x.toFloat(), y.toFloat())
}

@BindingAdapter("onClickIncrease")
fun ThermostatView.setOnClickIncreaseListener(listener: ThermostatView.OnClickIncreaseListener?) {
    this.onClickIncreaseListener = listener
}

@BindingAdapter("onClickDecrease")
fun ThermostatView.setOnClickDecreaseListener(listener: ThermostatView.OnClickDecreaseListener?) {
    this.onClickDecreaseListener = listener
}

private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
    val specMode = View.MeasureSpec.getMode(measureSpec)
    val specSize = View.MeasureSpec.getSize(measureSpec)

    return when (specMode) {
        View.MeasureSpec.EXACTLY -> specSize
        View.MeasureSpec.AT_MOST -> min(desiredSize, specSize)
        else -> desiredSize
    }
}

fun convertDpToPixel(dp: Float, context: Context): Float {
    return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Rect.drawTextCentered(canvas: Canvas, paint: Paint, text: String, cx: Float, cy: Float) {
    paint.getTextBounds(text, 0, text.length, this)
    canvas.drawText(text, cx - this.exactCenterX(), cy - this.exactCenterY(), paint)
}