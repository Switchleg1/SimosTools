package com.app.simostools

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class SwitchGraph: View {
    constructor(context: Context) : super(context) {
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }
    private val bgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }
    private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 5.0f
    }
    private val cursorPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 2.0f
    }
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.FILL_AND_STROKE
        color       = Color.WHITE
        strokeWidth = 2.0f
        textSize    = 50.0f
    }
    private var mCurrentWidth               = 0f
    private var mCurrentHeight              = 0f
    private var mCursorPosition             = 0f
    private var mZoomAmount                 = 1f
    private var mZoomingAmount              = 0f
    private var mZooming                    = false
    private var mZoomingX                   = 0f
    private var mZoomingY                   = 0f
    var data:Array<PlayBackDataStruct?>?    = null

    override fun onDraw(canvas: Canvas) {
        //draw background
        canvas.drawRect(0f, 0f, mCurrentWidth, mCurrentHeight, bgPaint)

        //draw zoom
        data?.let { dataList ->
            textPaint.color = Color.WHITE
            canvas.drawText(
                "Z[$mZoomAmount]",
                0f,
                50f,
                textPaint
            )

            //draw text
            var textPosition = 0
            for(i in 0 until dataList.count()) {
                dataList[i]?.let { data ->
                    if(data.enabled) {
                        var xPosition = ((mCursorPosition / mCurrentWidth) * data.data.count()).toInt()
                        if(xPosition >= data.data.count())
                            xPosition = data.data.count()-1
                        if(xPosition < 0)
                            xPosition = 0
                        val xValue = data.data[xPosition]
                        textPaint.color = data.color
                        canvas.drawText(
                            "${data.name} [$xValue]",
                            0f,
                            mCurrentHeight - (66f * textPosition),
                            textPaint
                        )
                        textPosition++
                    }
                }
            }

            //draw line graphs
            for(i in 0 until dataList.count()) {
                val pidItem = dataList[i]
                pidItem?.let {
                    if (pidItem.data.count() > 0 && pidItem.enabled) {
                        linePaint.color = pidItem.color
                        val startX = (1.0f - (mZoomAmount + mZoomingAmount)) * mCursorPosition
                        val incrementX = mCurrentWidth / pidItem.data.count() * (mZoomAmount + mZoomingAmount)
                        val incrementY = mCurrentHeight / (pidItem.max - pidItem.min)
                        var lastY = pidItem.data[0] * incrementY
                        for (x in 0 until pidItem.data.count()) {
                            canvas.drawLine(x * incrementX + startX, mCurrentHeight-lastY, (x + 1) * incrementX + startX, mCurrentHeight-(pidItem.data[x] * incrementY), linePaint)
                            lastY = pidItem.data[x] * incrementY
                        }
                    }
                }
            }

            //draw cursor
            canvas.drawLine(mCursorPosition, mCurrentHeight, mCursorPosition, 0f, cursorPaint)
        }
        super.onDraw(canvas)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        mCurrentWidth = width.toFloat()
        mCurrentHeight = height.toFloat()

        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if(it.pointerCount > 1) {
                if(!mZooming) {
                    mZooming = true
                    mZoomingX = it.x
                    mZoomingY = it.y
                } else {
                    when (it.action) {
                        else -> {
                            val zAmount = (it.x-mZoomingX) * 0.005f
                            mZoomingAmount = if(zAmount + mZoomAmount < 1f) 1.0f - mZoomAmount
                            else zAmount

                            invalidate()
                        }
                    }
                }
            } else {
                if(mZooming) {
                    mZooming = false
                    mZoomAmount += mZoomingAmount
                    mZoomingAmount = 0f
                }
                when (it.action) {
                    else -> {
                        mCursorPosition = it.x
                        invalidate()
                    }
                }
            }
        }
        return true
    }
}