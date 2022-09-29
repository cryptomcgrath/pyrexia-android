package com.cryptomcgrath.pyrexia.thermostat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import kotlin.math.min
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

    private val centerDefaultColor: Int
    private val centerHeatingColor: Int
    private val centerCoolingColor: Int

    private var centerDefaultPaint: Paint
    private var centerHeatingPaint: Paint
    private var centerCoolingPaint: Paint
    private var rimPaint: Paint

    private var temperaturePaint: Paint
    private var setPointPaint: Paint
    private var temperatureTextSize: Float
    private var setPointTextSize: Float

    // the width of the bar in pixels
    private val barWidthPixels: Float

    // the amount of spacing to separate each bar around the circle, in degrees
    private var barGapDegrees: Float = 0f

    private val barGapPixels: Float

    var onClickIncreaseListener: OnClickIncreaseListener? = null
    var onClickDecreaseListener: OnClickDecreaseListener? = null

    var currentTemp: String
    var setPoint: String

    private val pieBounds = RectF()
    private val pieBoundsAnimatedOut = RectF()
    private val centerFillBounds = RectF()

    init {
        isSaveEnabled = true

        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ThermostatView)

        barWidthPixels = typedArray.getDimensionPixelSize(
            R.styleable.ThermostatView_barWidth,
            resources.getDimensionPixelSize(R.dimen.thermostatview_default_bar_width)).toFloat()
        barGapPixels = typedArray.getDimensionPixelSize(
            R.styleable.ThermostatView_barGap,
            resources.getDimensionPixelSize(R.dimen.thermostatview_default_bar_gap)).toFloat()

        centerDefaultColor =  typedArray.getColor(R.styleable.ThermostatView_centerDefaultColor,
            ContextCompat.getColor(getContext(), R.color.light_blue))
        centerHeatingColor = typedArray.getColor(R.styleable.ThermostatView_centerHeatingColor,
            ContextCompat.getColor(getContext(), R.color.heating))
        centerCoolingColor = typedArray.getColor(R.styleable.ThermostatView_centerCoolingColor,
            ContextCompat.getColor(getContext(), R.color.cooling))

        currentTemp = typedArray.getString(R.styleable.ThermostatView_currentTemp) ?: "---"
        setPoint = typedArray.getString(R.styleable.ThermostatView_setPoint) ?: "---"

        centerDefaultPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.light_grey)
            isAntiAlias = true
        }
        centerHeatingPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.heating)
            isAntiAlias = true
        }
        centerCoolingPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.cooling)
            isAntiAlias = true
        }

        // will be set in onLayout
        temperatureTextSize = 0f
        setPointTextSize = 0f
        temperaturePaint = Paint()
        setPointPaint = Paint()
        rimPaint = Paint()

        typedArray.recycle()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        xCenter = (r-l)/2f
        yCenter = (b-t)/2f

        centerRadius = xCenter-(barWidthPixels*2f)
        rimRadius = xCenter

        val margin = (barWidthPixels * 2).toInt()
        val marginHalf = margin / 2

        pieBounds.set(
            left.toFloat() + margin,
            top.toFloat() + margin,
            right.toFloat() - margin,
            bottom.toFloat() - margin)
        pieBoundsAnimatedOut.set(
            left.toFloat() + marginHalf,
            top.toFloat() + marginHalf,
            right.toFloat() - marginHalf,
            bottom.toFloat() - marginHalf)
        centerFillBounds.set(
            left.toFloat() + margin + barWidthPixels,
            top.toFloat() + margin + barWidthPixels,
            right.toFloat() - margin - barWidthPixels,
            bottom.toFloat() - margin - barWidthPixels)

        temperatureTextSize = xCenter / 2f
        setPointTextSize = temperatureTextSize * .4f
        temperaturePaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.white)
            isAntiAlias = true
            textSize = temperatureTextSize
            textAlign = Paint.Align.CENTER
        }
        setPointPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.white)
            isAntiAlias = true
            textSize = setPointTextSize
            textAlign = Paint.Align.CENTER
        }
        rimPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.silver)
            isAntiAlias = true
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
        // rim
        canvas.drawCircle(xCenter, yCenter, rimRadius, rimPaint)

        // center
        canvas.drawCircle(xCenter,yCenter,centerRadius,centerDefaultPaint)

        // current temp
        canvas.drawText(currentTemp, xCenter, yCenter+(temperatureTextSize/2), temperaturePaint)
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
     *  270 .     x,y       90
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
        var theta = Math.toDegrees(atan2(dy, dx).toDouble()) + 90
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

// parses a color string in the format #ffffff and returns the color int
// or returns null if the color string is invalid
fun parseColorOrNull(colorString: String?): Int? {
    if (colorString.isNullOrBlank()) return null
    var cleaned = colorString.trim()
    if (cleaned.length == 6 && !cleaned.contains("#")) {
        cleaned = "#$cleaned"
    }
    return try {
        Color.parseColor(cleaned)
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun convertDpToPixel(dp: Float, context: Context): Float {
    return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}