package com.cryptomcgrath.pyrexia.thermostat

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.cryptomcgrath.pyrexia.R

class PropaneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var xCenter = 0f
    private var yCenter = 0f
    private val bounds = RectF()
    private var percentFullTextSize = 0f

    private var fullPaint: Paint = Paint()
    private var emptyPaint: Paint = Paint()

    var fullColor: Int = ContextCompat.getColor(context, R.color.green)
        @SuppressLint("ResourceAsColor")
        set(value) {
            field = value
            fullPaint = Paint().apply {
                color = ContextCompat.getColor(context, value)
                isAntiAlias = true
                strokeWidth = 1f
                style = Paint.Style.FILL_AND_STROKE
            }
            invalidate()
            requestLayout()
        }

    var emptyColor: Int = ContextCompat.getColor(context, R.color.white)
        @SuppressLint("ResourceAsColor")
        set(value) {
            field = value
            emptyPaint = Paint().apply {
                color = ContextCompat.getColor(context, value)
                isAntiAlias = true
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = 1f
            }
            invalidate()
            requestLayout()
        }

    var percentFull: Int = 0
        set(value) {
            field = value

            invalidate()
            requestLayout()
        }

    init {
        setWillNotDraw(false)
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PropaneView)

        percentFull = typedArray.getInteger(R.styleable.PropaneView_percentFull, 0)
        emptyColor = typedArray.getInteger(R.styleable.PropaneView_emptyColor, R.color.white)
        fullColor = typedArray.getInteger(R.styleable.PropaneView_fullColor, R.color.green)
        typedArray.recycle()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        xCenter = (r-l)/2f
        yCenter = (b-t)/2f

        bounds.set(
            l.toFloat(),
            t.toFloat(),
            r.toFloat(),
            b.toFloat())

        percentFullTextSize = xCenter / 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // use parent width for x and y desired sizes if not specified
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec);

        val xDim = measureDimension(desiredWidth, widthMeasureSpec)
        val yDim = measureDimension(desiredWidth, heightMeasureSpec)
        setMeasuredDimension(xDim, yDim)
    }

    private var ringStep = 7
    private var ringHeight = 3
    private var ringMargin = 0f

    override fun onDraw(canvas: Canvas) {
        val btm = bounds.bottom //- ringHeight
        for (x in bounds.top.toInt()..btm.toInt() step ringStep) {
            val y = (x-bounds.top / (btm-bounds.top) * 100f)
            ringMargin = when {
                y < 20f -> 20f - y
                y > 80f -> y - 80f
                else -> 0f
            } / 50f * (bounds.right - bounds.left)
            Log.d("PropaneView", "x=$x ringMargin=$ringMargin")
            canvas.drawOval(bounds.left+ringMargin,x.toFloat(),bounds.right-ringMargin,(x-ringHeight).toFloat(), emptyPaint)
            if (100f - y <= percentFull) {
                canvas.drawOval(bounds.left+ringMargin,x.toFloat(),bounds.right-ringMargin,(x-ringHeight).toFloat(), fullPaint)
            }
        }
    }
}