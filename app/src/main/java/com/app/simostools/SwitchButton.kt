package com.app.simostools

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity

class SwitchButton: androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context) {
        super.setGravity(Gravity.CENTER)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        super.setGravity(Gravity.CENTER)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        super.setGravity(Gravity.CENTER)
    }

    private var mRect       = RectF(0f, 0f, 0f,0f)
    var margin              = 10f
    var round               = 50f
    val paintBG: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.FILL
        color       = Color.LTGRAY
        strokeCap   = Paint.Cap.ROUND
    }
    val paintRim: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.STROKE
        color       = Color.BLUE
        strokeCap   = Paint.Cap.ROUND
        strokeWidth = 7f
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(mRect, round, round, paintBG)
        canvas.drawRoundRect(mRect, round, round, paintRim)
        super.onDraw(canvas)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        mRect.set(margin, margin, width.toFloat()-margin, height.toFloat()-margin)

        invalidate()
    }
}