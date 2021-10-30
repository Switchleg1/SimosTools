package com.app.simostools

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SwitchGauge : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val progressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val minmaxPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 5.0f
    }
    private val minmaxBGPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 5.0f
    }
    private val rect            = RectF()
    private val minAngle        = 120f
    private val maxAngle        = 300f
    private val maxProgress     = 100f
    private var angleProgress   = 0f
    private var angleMin        = 0f
    private var angleMax        = 0f
    private var currentWidth    = 0f
    private var currentHeight   = 0f
    private var currentProgress = 0f
    private var currentStyle    = GaugeType.BAR
    private var currentIndex    = 0
    private var currentEnable   = true
    private var currentMin      = 0f
    private var currentMax      = 0f
    private var showMinMax      = true
    private var marginStart     = 10.0f
    private var marginEnd       = 10.0f


    override fun onDraw(canvas: Canvas) {
        if(currentEnable) {
            when (currentStyle) {
                GaugeType.BAR -> {
                    drawRect(100.0f, backgroundPaint.strokeWidth, canvas, backgroundPaint)
                    drawRect(currentProgress, progressPaint.strokeWidth, canvas, progressPaint)
                    if(showMinMax) {
                        drawLine(currentMin, progressPaint.strokeWidth, canvas, minmaxPaint)
                        drawLine(currentMax, progressPaint.strokeWidth, canvas, minmaxPaint)
                    }
                }
                GaugeType.ROUND -> {
                    drawCircle(maxAngle, canvas, backgroundPaint)
                    drawCircle(angleProgress, canvas, progressPaint)

                    if(showMinMax) {
                        drawCircle(maxAngle, canvas, minmaxBGPaint)
                        drawCircle(angleMax+1.0f, canvas, minmaxPaint)
                        drawCircle(angleMin-1.0f, canvas, minmaxBGPaint)
                    }
                }
            }
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        currentWidth = width.toFloat()
        currentHeight = height.toFloat()
        marginEnd = currentWidth - (marginStart * 2.0f)

        invalidate()
    }

    private fun drawCircle(finish: Float, canvas: Canvas, paint: Paint) {
        val strokeWidth = paint.strokeWidth / 2.0f
        rect.set(marginStart + strokeWidth, strokeWidth, currentWidth - strokeWidth - marginStart, currentHeight - strokeWidth)
        canvas.drawArc(rect, minAngle, finish, false, paint)
    }

    private fun drawRect(finish: Float, width: Float, canvas: Canvas, paint: Paint) {
        val offsetY = (currentHeight-width) / 2.0f
        val stop = marginStart + (finish / 100.0f * marginEnd)

        canvas.drawRoundRect(marginStart, offsetY, stop, currentHeight-offsetY, 10f, 10f, paint)
    }

    private fun drawLine(position: Float, width: Float, canvas: Canvas, paint: Paint) {
        val offsetY = (currentHeight-width+marginStart) / 2.0f
        val begin = marginStart + (position / 100.0f * marginEnd)

        canvas.drawLine(begin, offsetY, begin, currentHeight-offsetY, paint)
    }

    private fun calculateAngle(progress: Float) = maxAngle / maxProgress * progress

    fun setStyle(style: GaugeType, redraw: Boolean = true) {
        currentStyle = style

        when(currentStyle) {
            GaugeType.BAR -> {
                progressPaint.style     = Paint.Style.FILL
                backgroundPaint.style   = Paint.Style.FILL
            }
            GaugeType.ROUND -> {
                progressPaint.style     = Paint.Style.STROKE
                backgroundPaint.style   = Paint.Style.STROKE
            }
        }

        if(redraw)
            invalidate()
    }

    fun getStyle() : GaugeType {
        return currentStyle
    }

    fun setMinMax(allow: Boolean, redraw: Boolean = true) {
        showMinMax = allow

        if(redraw)
            invalidate()
    }

    fun getMinMax(): Boolean {
        return showMinMax
    }

    fun setProgress(progress: Float, min: Float, max: Float, redraw: Boolean = true) {
        currentProgress = when {
            progress > 100.0f   -> 100.0f
            progress < 0.0f     -> 0.0f
            else                -> progress
        }
        currentMin = when {
            min > 100.0f    -> 100.0f
            min < 0.0f      -> 0.0f
            else            -> min
        }
        currentMax = when {
            max > 100.0f    -> 100.0f
            max < 0.0f      -> 0.0f
            else            -> max
        }

        angleProgress   = calculateAngle(currentProgress)
        angleMin        = calculateAngle(currentMin)
        angleMax        = calculateAngle(currentMax)

        if(redraw)
            invalidate()
    }

    fun getProgress(): Float {
        return currentProgress
    }

    fun setProgressColor(color: Int, redraw: Boolean = true) {
        progressPaint.color = color

        if(redraw)
            invalidate()
    }

    fun setProgressBackgroundColor(color: Int, redraw: Boolean = true) {
        backgroundPaint.color = color
        minmaxBGPaint.color = color

        if(redraw)
            invalidate()
    }

    fun setMinMaxColor(color: Int, redraw: Boolean = true) {
        minmaxPaint.color = color

        if(redraw)
            invalidate()
    }

    fun setProgressWidth(width: Float, redraw: Boolean = true) {
        progressPaint.strokeWidth = width
        backgroundPaint.strokeWidth = width

        if(redraw)
            invalidate()
    }

    fun setMinMaxWidth(width: Float, redraw: Boolean = true) {
        minmaxPaint.strokeWidth = width
        minmaxBGPaint.strokeWidth = width

        if(redraw)
            invalidate()
    }

    fun setRounded(rounded: Boolean, redraw: Boolean = true) {
        progressPaint.strokeCap     = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        backgroundPaint.strokeCap   = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        minmaxPaint.strokeCap       = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        minmaxBGPaint.strokeCap     = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT

        if(redraw)
            invalidate()
    }

    fun setIndex(ind: Int) {
        currentIndex = ind
    }

    fun getIndex(): Int {
        return currentIndex
    }

    fun setEnable(enabled: Boolean, redraw: Boolean = true) {
        currentEnable = enabled

        if(redraw)
            invalidate()
    }

    fun getEnable(): Boolean {
        return currentEnable
    }
}