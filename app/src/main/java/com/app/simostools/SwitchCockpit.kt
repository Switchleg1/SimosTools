package com.app.simostools

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class SwitchCockpit: View {
    constructor(context: Context) : super(context) {
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }
    private val bgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.BLACK
        strokeWidth = 100.0f
    }
    private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 5.0f
    }
    private val gaugeRing: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.WHITE
        strokeWidth = 50.0f
    }
    private val boostPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.FILL_AND_STROKE
        color       = Color.WHITE
        strokeWidth = 1.0f
    }
    private val rpmPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.FILL_AND_STROKE
        color       = Color.WHITE
        strokeWidth = 1.0f
    }
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.FILL_AND_STROKE
        color       = Color.WHITE
        strokeWidth = 1.0f
        textSize    = 30f
        textAlign   = Paint.Align.CENTER
    }
    private var mCurrentWidth                       = 0f
    private var mCurrentHeight                      = 0f
    private var mCurrentWidthCenter                 = 0f
    private var mCurrentHeightCenter                = 0f
    private var mRect                               = RectF()
    var dataVelocity                                = 0f
    var dataRPM                                     = 0f
    var dataBoost                                   = 0f
    var dataAccelerationLatitude                    = 0f
    var dataAccelerationLongitude                   = 0f

    fun doDraw() {
        invalidate()
    }

    private fun drawBoostGauge(canvas: Canvas, boost: Float, rpm: Float, size: Float) {
        val boostSegmentCount = 20
        val boostSegmentSize = 350f / boostSegmentCount
        val angleSegment = 50f / boostSegmentCount
        val strokeWidth = boostPaint.strokeWidth / 2.0f
        val boostAmount = boost / 300f * boostSegmentCount
        val rpmAmount = rpm / 7000f * boostSegmentCount
        val colorSegment = 1f / boostSegmentCount.toFloat()

        boostPaint.color = Color.GREEN
        rpmPaint.color = Color.GREEN
        for(i in 0 .. boostSegmentCount) {
            if(i >= boostAmount.toInt()) boostPaint.color = Color.WHITE
            else boostPaint.color = Color.rgb(colorSegment * i.toFloat(), 1f - (colorSegment * i.toFloat()), 0f)
            if(i >= rpmAmount.toInt()) rpmPaint.color = Color.WHITE
            else rpmPaint.color = Color.rgb(colorSegment * i.toFloat(), 1f - (colorSegment * i.toFloat()), 0f)

            canvas.drawRoundRect(mCurrentWidthCenter - 300f, mCurrentHeightCenter + 140f - (boostSegmentSize * (i - 0.5f)), mCurrentWidthCenter - 100f, mCurrentHeightCenter + 140f - (boostSegmentSize * i), 50f, 50f, boostPaint)
            canvas.drawRoundRect(mCurrentWidthCenter + 300f, mCurrentHeightCenter + 140f - (boostSegmentSize * (i - 0.5f)), mCurrentWidthCenter + 100f, mCurrentHeightCenter + 140f - (boostSegmentSize * i), 50f, 50f, rpmPaint)
        }

        canvas.drawCircle(mCurrentWidthCenter, mCurrentHeightCenter, 200f, bgPaint)
    }

    private fun drawVelocityGauge(canvas: Canvas, velocity: Float, size: Float) {
        val angleStart = 140f
        val angleSegmentCount = 50
        val angleSegment = 260f / angleSegmentCount
        val amount = velocity / 220f * angleSegmentCount
        val strokeWidth = linePaint.strokeWidth / 2.0f
        mRect.set(mCurrentWidthCenter - 200f, mCurrentHeightCenter - 200f, 200f + mCurrentWidthCenter, 200f + mCurrentHeightCenter)

        gaugeRing.color = Color.RED
        for(i in 0 .. angleSegmentCount) {
            if(i == amount.toInt())
                gaugeRing.color = Color.WHITE
            canvas.drawArc(mRect, angleStart+(angleSegment * i)-(angleSegment*0.25f), angleSegment * 0.5f, false, gaugeRing)
        }
    }

    private fun drawAccelerationGauge(canvas: Canvas, latitude: Float, longitude: Float, size: Float) {

    }

    private fun drawVelocityText(canvas: Canvas, velocity: Float, size: Float) {
        textPaint.textSize = 30f * size
        canvas.drawText(
            "$velocity Km/h",
            mCurrentWidthCenter,
            mCurrentHeightCenter+(100f*size),
            textPaint
        )
    }

    override fun onDraw(canvas: Canvas) {
        //draw background
        canvas.drawRect(0f, 0f, mCurrentWidth, mCurrentHeight, bgPaint)

        drawBoostGauge(canvas, dataBoost, dataRPM, 1.0f)
        drawAccelerationGauge(canvas, dataAccelerationLatitude, dataAccelerationLongitude, 1.0f)
        drawVelocityGauge(canvas, dataVelocity, 1.0f)
        drawVelocityText(canvas, dataVelocity, 1.0f)

        super.onDraw(canvas)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        mCurrentWidth = width.toFloat()
        mCurrentHeight = height.toFloat()
        mCurrentWidthCenter = mCurrentWidth / 2.0f
        mCurrentHeightCenter = mCurrentHeight / 2.0f

        invalidate()
    }
}