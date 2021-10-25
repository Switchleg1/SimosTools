package com.app.simoslogger

import android.content.Context
import android.graphics.Canvas
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

    private val rect = RectF()
    private val startAngle = 120f
    private val maxAngle = 300f
    private val maxProgress = 100f
    private var angle = 0f
    private var currentWidth = 0f
    private var currentHeight = 0f
    private var currentProgress = 0f
    private var currentStyle = DisplayType.BAR
    private var currentIndex = 0
    private var currentEnable = true


    override fun onDraw(canvas: Canvas) {
        if(currentEnable) {
            when (currentStyle) {
                DisplayType.BAR -> {
                    drawRect(100.0f, canvas, backgroundPaint)
                    drawRect(currentProgress, canvas, progressPaint)
                }
                DisplayType.ROUND -> {
                    drawCircle(maxAngle, canvas, backgroundPaint)
                    drawCircle(angle, canvas, progressPaint)
                }
            }
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        currentWidth = width.toFloat()
        currentHeight = height.toFloat()
        invalidate()
    }

    private fun drawCircle(angle: Float, canvas: Canvas, paint: Paint) {
        val strokeWidth = paint.strokeWidth
        rect.set(strokeWidth, strokeWidth, currentWidth - strokeWidth, currentHeight - strokeWidth)
        canvas.drawArc(rect, startAngle, angle, false, paint)
    }

    private fun drawRect(width: Float, canvas: Canvas, paint: Paint) {
        val offsetY = (currentHeight-paint.strokeWidth) / 2.0f
        val offsetX = 10.0f
        val stop = width / 100.0f * (currentWidth-10.0f)

        if(stop > offsetX)
            canvas.drawRoundRect(offsetX, offsetY, stop, currentHeight-offsetY, 10.0f, 10.0f, paint)
    }

    private fun calculateAngle(progress: Float) = maxAngle / maxProgress * progress

    fun setStyle(style: DisplayType) {
        currentStyle = style

        when(currentStyle) {
            DisplayType.BAR -> {
                progressPaint.style = Paint.Style.FILL
                backgroundPaint.style = Paint.Style.FILL
            }
            DisplayType.ROUND -> {
                progressPaint.style = Paint.Style.STROKE
                backgroundPaint.style = Paint.Style.STROKE
            }
        }
        invalidate()
    }

    fun getStyle() : DisplayType {
        return currentStyle
    }

    fun setProgress(progress: Float) {
        currentProgress = when {
            progress > 100.0f -> {
                100.0f
            }
            progress < 0.0f -> {
                0.0f
            }
            else -> {
                progress
            }
        }
        angle = calculateAngle(currentProgress)
        invalidate()
    }

    fun getProgress(): Float {
        return currentProgress
    }

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }

    fun setProgressBackgroundColor(color: Int) {
        backgroundPaint.color = color
        invalidate()
    }

    fun setProgressWidth(width: Float) {
        progressPaint.strokeWidth = width
        backgroundPaint.strokeWidth = width
        invalidate()
    }

    fun setRounded(rounded: Boolean) {
        progressPaint.strokeCap = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        backgroundPaint.strokeCap = if (rounded) Paint.Cap.ROUND else Paint.Cap.BUTT
        invalidate()
    }

    fun setIndex(ind: Int) {
        currentIndex = ind
    }

    fun getIndex(): Int {
        return currentIndex
    }

    fun setEnable(enabled: Boolean) {
        currentEnable = enabled
        invalidate()
    }

    fun getEnable(): Boolean {
        return currentEnable
    }
}