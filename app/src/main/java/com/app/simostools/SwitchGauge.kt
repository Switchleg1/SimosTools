package com.app.simostools

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withRotation


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
    private val tickPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
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
    private var showGraduations = true
    private var marginStart     = 10.0f
    private var marginEnd       = 10.0f
    private var centerGauge     = false

    override fun onDraw(canvas: Canvas) {
        if(currentEnable) {
            when (currentStyle) {
                GaugeType.BAR -> {
                    if(centerGauge) {
                        drawRect(100.0f, backgroundPaint.strokeWidth, canvas, backgroundPaint)
                        if (currentProgress < 50.0f) {
                            drawRect(50.0f, progressPaint.strokeWidth, canvas, progressPaint)
                            drawRect(currentProgress, backgroundPaint.strokeWidth, canvas, backgroundPaint)
                        } else {
                            drawRect(currentProgress, progressPaint.strokeWidth, canvas, progressPaint)
                            drawRect(50.0f, backgroundPaint.strokeWidth, canvas, backgroundPaint)
                        }
                    } else {
                        drawRect(100.0f, backgroundPaint.strokeWidth, canvas, backgroundPaint)
                        drawRect(currentProgress, progressPaint.strokeWidth, canvas, progressPaint)
                    }

                    if(showMinMax) {
                        drawLine(currentMin, progressPaint.strokeWidth, canvas, minmaxPaint)
                        drawLine(currentMax, progressPaint.strokeWidth, canvas, minmaxPaint)
                    }
                }
                GaugeType.ROUND -> {
                    if(centerGauge) {
                        drawCircle(minAngle, maxAngle, canvas, backgroundPaint)
                        if (currentProgress < 50.0f) {
                            drawCircle(minAngle + angleProgress, maxAngle * 0.5f - angleProgress, canvas, progressPaint)
                        } else {
                            drawCircle(minAngle + maxAngle * 0.5f, angleProgress - maxAngle * 0.5f, canvas, progressPaint)
                        }
                    } else {
                        drawCircle(minAngle, maxAngle, canvas, backgroundPaint)
                        drawCircle(minAngle, angleProgress, canvas, progressPaint)
                    }

                    if(showMinMax) {
                        drawCircle(minAngle, maxAngle, canvas, minmaxBGPaint)
                        drawCircle(minAngle + angleMin-1.0f, angleMax+1.0f-angleMin, canvas, minmaxPaint)
                    }

                    if(showGraduations) {
                        for (i in 1 until 10) {
                            val start = minAngle + 30.0f * i.toFloat()
                            drawCircle(start - 0.3f, 0.6f, canvas, tickPaint)
                        }
                    }
                }
            }
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        currentWidth = width.toFloat()
        currentHeight = height.toFloat()
        marginEnd = currentWidth - (marginStart * 2.0f)

        var r = ((backgroundPaint.color and 0xFF0000) shr 16) + 40
        var g = ((backgroundPaint.color and 0xFF00) shr 8) + 40
        var b = (backgroundPaint.color and 0xFF) + 40
        if(r > 255)
            r = 255
        if(g > 255)
            g = 255
        if(b > 255)
            b = 255

        backgroundPaint.shader = RadialGradient(currentWidth/2f, currentHeight/2f,
                            (currentWidth+currentHeight)/4f, Color.BLACK,
                            Color.rgb(r, g, b),
                            Shader.TileMode.CLAMP)

        invalidate()
    }

    private fun drawCircle(start: Float, finish: Float, canvas: Canvas, paint: Paint) {
        val strokeWidth = paint.strokeWidth / 2.0f
        rect.set(marginStart + strokeWidth, strokeWidth, currentWidth - strokeWidth - marginStart, currentHeight - strokeWidth)
        canvas.drawArc(rect, start, finish, false, paint)
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
                backgroundPaint.style   = Paint.Style.FILL_AND_STROKE
                tickPaint.style         = Paint.Style.STROKE
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

    fun setGraduations(allow: Boolean, redraw: Boolean = true) {
        showGraduations = allow

        if(redraw)
            invalidate()
    }

    fun getGraduations(): Boolean {
        return showGraduations
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

        val r = (color and 0xFF0000) shr 16
        val g = (color and 0xFF00) shr 8
        val b = color and 0xFF
        tickPaint.color = Color.rgb(255-r, 255-g,255-b)

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
        tickPaint.strokeWidth = width + marginStart

        if(redraw)
            invalidate()
    }

    fun setMinMaxWidth(width: Float, redraw: Boolean = true) {
        minmaxPaint.strokeWidth = width
        minmaxBGPaint.strokeWidth = width

        if(redraw)
            invalidate()
    }

    fun getMinMaxWidth(): Float {
        return minmaxPaint.strokeWidth
    }

    fun setRounded(rounded: Boolean, redraw: Boolean = true) {
        progressPaint.strokeCap     = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        backgroundPaint.strokeCap   = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        minmaxPaint.strokeCap       = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        minmaxBGPaint.strokeCap     = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT

        if(redraw)
            invalidate()
    }

    fun setCentered(centered: Boolean, redraw: Boolean = true) {
        centerGauge = centered

        if(redraw)
            invalidate()
    }

    fun getCentered(): Boolean {
        return centerGauge
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