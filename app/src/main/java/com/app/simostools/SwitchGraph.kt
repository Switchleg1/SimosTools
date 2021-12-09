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
        strokeWidth = 1.0f
        textSize    = 35.0f
    }
    private val bgTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.FILL_AND_STROKE
        color       = Color.WHITE
        strokeWidth = 1.0f
        textSize    = 50.0f
    }
    private var mCurrentWidth                       = 0f
    private var mCurrentHeight                      = 0f
    private var mCursorPosition                     = 0f
    private var mZoomAmount                         = 1f
    private var mZoomingAmount                      = 0f
    private var mZooming                            = false
    private var mZoomingX                           = 0f
    private var mZoomingY                           = 0f
    private var mTextCount                          = 0
    private var mTextHeight                         = 0f
    private var mTextPerLine                        = 2
    private var mData:Array<LogViewerDataStruct?>?  = null

    fun setData(data:Array<LogViewerDataStruct?>?) {
        mData = data

        data?.let { dataList ->
            //get text height
            var textHeight = 0
            for (i in 0 until dataList.count()) {
                dataList[i]?.let { data ->
                    if (data.enabled) {
                        textHeight++
                    }
                }
            }

            mTextCount = textHeight
            mTextHeight = (((mTextCount-1) / mTextPerLine) + 1) * textPaint.textSize
        }
    }

    fun setTextPerLine(count: Int) {
        mTextPerLine = count
        mTextHeight = (((mTextCount-1) / mTextPerLine) + 1) * textPaint.textSize
    }

    fun setTextBGColor(color: Int) {
        bgTextPaint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        //local variable
        val textSplit = mCurrentHeight-mTextHeight
        //draw background
        canvas.drawRect(0f, 0f, mCurrentWidth, textSplit, bgPaint)

        //draw zoom
        mData?.let { dataList ->
            textPaint.color = Color.WHITE
            canvas.drawText(
                "Z[$mZoomAmount]",
                0f,
                textPaint.textSize,
                textPaint
            )

            //draw line graphs
            for(i in 0 until dataList.count()) {
                val pidItem = dataList[i]
                pidItem?.let {
                    if (pidItem.data.count() > 0 && pidItem.enabled) {
                        linePaint.color = pidItem.color
                        val startX = (1.0f - (mZoomAmount + mZoomingAmount)) * mCursorPosition
                        val startY = textSplit
                        val incrementX = mCurrentWidth / pidItem.data.count() * (mZoomAmount + mZoomingAmount)
                        val incrementY = startY / (pidItem.max - pidItem.min)
                        var lastY = pidItem.data[0] * incrementY
                        for (x in 0 until pidItem.data.count()) {
                            canvas.drawLine(x * incrementX + startX, startY-lastY, (x + 1) * incrementX + startX, startY-(pidItem.data[x] * incrementY), linePaint)
                            lastY = pidItem.data[x] * incrementY
                        }
                    }
                }
            }

            //draw cursor
            canvas.drawLine(mCursorPosition, mCurrentHeight, mCursorPosition, 0f, cursorPaint)

            //set bg color
            canvas.drawRect(0f, mCurrentHeight, mCurrentWidth, textSplit, bgTextPaint)

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
                            "${data.name} [${data.format.format(xValue)}]",
                            (textPosition % mTextPerLine).toFloat() * mCurrentWidth / mTextPerLine,
                            mCurrentHeight - (textPaint.textSize * (textPosition/mTextPerLine)),
                            textPaint
                        )
                        textPosition++
                    }
                }
            }
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