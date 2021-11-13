package com.app.simostools

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity

class SwitchGauge: androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context) {
        super.setGravity(Gravity.CENTER)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        super.setGravity(Gravity.CENTER)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        super.setGravity(Gravity.CENTER)
    }

    private val progressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val tickPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 5.0f
    }
    private val minmaxPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 5.0f
    }
    val rimPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.STROKE
        color       = Color.DKGRAY
        strokeCap   = Paint.Cap.ROUND
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
    private var currentStyle    = GaugeType.BAR_V
    private var currentIndex    = 0
    private var currentEnable   = true
    private var currentMin      = 0f
    private var currentMax      = 0f
    private var showMinMax      = true
    private var showGraduations = true
    private var centerGauge     = false
    var margin                  = 10f
    var round                   = 50f

    override fun onDraw(canvas: Canvas) {
        if(currentEnable) {
            when (currentStyle) {
                GaugeType.BAR_H -> drawBar(false, canvas)
                GaugeType.BAR_V -> drawBar(true, canvas)
                GaugeType.BB    -> drawBB(canvas)
                GaugeType.ROUND -> drawRound(canvas)
            }
        }
        super.onDraw(canvas)
    }

    private fun drawBar(vertical: Boolean, canvas: Canvas) {
        drawBB(canvas)

        progressPaint.alpha = 180
        if(centerGauge) {
            if (currentProgress < 50.0f) {
                drawRect(vertical, currentProgress, 50.0f, canvas, progressPaint)
            } else {
                drawRect(vertical, 50.0f, currentProgress, canvas, progressPaint)
            }
        } else {
            drawRect(vertical, 0f, currentProgress, canvas, progressPaint)
        }

        if(showGraduations) {
            tickPaint.alpha = 25
            for (i in 1 until 10) {
                drawLine(vertical, i.toFloat()*10f, progressPaint.strokeWidth+margin*1.5f, canvas, tickPaint)
            }
        }

        if(showMinMax) {
            drawLine(vertical, currentMin, progressPaint.strokeWidth, canvas, minmaxPaint)
            drawLine(vertical, currentMax, progressPaint.strokeWidth, canvas, minmaxPaint)
        }
    }

    private fun drawBB(canvas: Canvas) {
        drawRect(false, 0f, 100.0f, canvas, backgroundPaint)
        drawRect(false, 0f, 100.0f, canvas, rimPaint)
    }

    private fun drawRound(canvas: Canvas) {
        drawCircle(minAngle, maxAngle, canvas, backgroundPaint)
        if(centerGauge) {
            if (currentProgress < 50.0f) {
                drawCircle(minAngle + angleProgress, maxAngle * 0.5f - angleProgress, canvas, progressPaint)
            } else {
                drawCircle(minAngle + maxAngle * 0.5f, angleProgress - maxAngle * 0.5f, canvas, progressPaint)
            }
        } else {
            drawCircle(minAngle, angleProgress, canvas, progressPaint)
        }

        if(showMinMax) {
            drawCircle(minAngle + angleMin-0.5f, angleMax+1.0f-angleMin, canvas, minmaxPaint)
        }

        if(showGraduations) {
            for (i in 1 until 10) {
                val start = minAngle + 30.0f * i.toFloat()
                drawCircle(start - 0.25f, 0.5f, canvas, tickPaint)
            }
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        currentWidth = width.toFloat()
        currentHeight = height.toFloat()

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
        val margin_h = margin / 2f
        val strokeWidth = paint.strokeWidth / 2.0f
        rect.set(margin_h + strokeWidth, strokeWidth, currentWidth - strokeWidth - margin_h, currentHeight - strokeWidth)
        canvas.drawArc(rect, start, finish, false, paint)
    }

    private fun drawRect(vertical: Boolean, start: Float, finish: Float, canvas: Canvas, paint: Paint) {
        when(vertical) {
            true    -> drawRectV(start, finish, canvas, paint)
            false   -> drawRectH(start, finish, canvas, paint)
        }
    }

    private fun drawRectH(start: Float, finish: Float, canvas: Canvas, paint: Paint) {
        val marginEnd = currentWidth-margin*2f
        val offsetY = margin
        val begin = margin + (start / 100.0f * marginEnd)
        val stop = margin + (finish / 100.0f * marginEnd)

        canvas.drawRoundRect(begin, offsetY, stop, currentHeight-offsetY, round, round, paint)
    }

    private fun drawRectV(start: Float, finish: Float, canvas: Canvas, paint: Paint) {
        val marginEnd = currentHeight-margin*2f
        val offsetX = margin
        val begin = margin + ((100.0f-start) / 100.0f * marginEnd)
        val stop = margin + ((100.0f-finish) / 100.0f * marginEnd)

        canvas.drawRoundRect(offsetX, begin, currentWidth-offsetX, stop, round, round, paint)
    }

    private fun drawLine(vertical: Boolean, start: Float, finish: Float, canvas: Canvas, paint: Paint) {
        when(vertical) {
            true    -> drawLineV(start, finish, canvas, paint)
            false   -> drawLineH(start, finish, canvas, paint)
        }
    }

    private fun drawLineH(position: Float, width: Float, canvas: Canvas, paint: Paint) {
        val marginEnd = currentWidth-margin
        val offsetY = (currentHeight-width+margin) / 2.0f
        val begin = margin + (position / 100.0f * marginEnd)

        canvas.drawLine(begin, offsetY, begin, currentHeight-offsetY, paint)
    }

    private fun drawLineV(position: Float, height: Float, canvas: Canvas, paint: Paint) {
        val marginEnd = currentHeight-margin
        val offsetX = (currentWidth-height+margin) / 2.0f
        val begin = margin + (position / 100.0f * marginEnd)

        canvas.drawLine(offsetX, begin, currentWidth-offsetX, begin, paint)
    }

    private fun calculateAngle(progress: Float) = maxAngle / maxProgress * progress

    private fun setTickWidth() {
        when(currentStyle) {
            GaugeType.BAR_H -> tickPaint.strokeWidth   = minmaxPaint.strokeWidth
            GaugeType.BAR_V -> tickPaint.strokeWidth   = minmaxPaint.strokeWidth
            GaugeType.BB    -> tickPaint.strokeWidth   = minmaxPaint.strokeWidth
            GaugeType.ROUND -> tickPaint.strokeWidth   = backgroundPaint.strokeWidth + margin
        }
    }

    fun setStyle(style: GaugeType, redraw: Boolean = true) {
        currentStyle = style

        when(currentStyle) {
            GaugeType.BAR_H -> {
                progressPaint.style     = Paint.Style.FILL
                backgroundPaint.style   = Paint.Style.FILL
            }
            GaugeType.BAR_V -> {
                progressPaint.style     = Paint.Style.FILL
                backgroundPaint.style   = Paint.Style.FILL
            }
            GaugeType.BB -> {
                progressPaint.style     = Paint.Style.FILL
                backgroundPaint.style   = Paint.Style.FILL
            }
            GaugeType.ROUND -> {
                progressPaint.style     = Paint.Style.STROKE
                backgroundPaint.style   = Paint.Style.FILL_AND_STROKE
            }
        }

        setTickWidth()

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

    fun setRimColor(color: Int, redraw: Boolean = true) {
        rimPaint.color = color

        if(redraw)
            invalidate()
    }

    fun setProgressWidth(width: Float, redraw: Boolean = true) {
        progressPaint.strokeWidth = width
        backgroundPaint.strokeWidth = width

        setTickWidth()

        if(redraw)
            invalidate()
    }

    fun setMinMaxWidth(width: Float, redraw: Boolean = true) {
        minmaxPaint.strokeWidth = width

        setTickWidth()

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
        tickPaint.strokeCap         = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT

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