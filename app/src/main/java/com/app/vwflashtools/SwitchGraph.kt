package com.app.vwflashtools

import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import java.io.IOException
import java.util.LinkedList

class SwitchGraph(context: Context) : androidx.appcompat.widget.AppCompatImageView(context) {
    private val TAG = "SwitchGraph"
    private var mLinkedList: LinkedList<FloatArray?>? = LinkedList<FloatArray?>()
    private var mMaxValues: Int = 50
    private var mParamCount: Int = 8
    private var mLineLength: Float = 20f
    private var mValueMultiplier: Float = 10.0f
    private var mColorList: IntArray = IntArray(mParamCount)

    override fun onDraw(canvas: Canvas) {
        canvas.save()
            try {
                canvas.translate(0.0f, 0.0f)

                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                p.color = Color.RED
                p.strokeWidth = 10f
                val xMax = if(mMaxValues > mLinkedList!!.count()) {
                    mLinkedList!!.count()
                } else {
                    mMaxValues
                }

                for (x in 0 until xMax) {
                    val curValues = mLinkedList!![x]
                    val nextValues = if(x == mLinkedList!!.count()-1) {
                        mLinkedList!![x]!!
                    } else {
                        mLinkedList!![x+1]!!
                    }

                    for(y in 0 until curValues!!.count()) {
                        if(mColorList[y] != 0)
                            p.color = mColorList[y]

                        canvas.drawLine(width - (xMax-x) * mLineLength, height - curValues[y] * mValueMultiplier, width - ((xMax-x)-1) * mLineLength, height - nextValues[y] * mValueMultiplier, p)
                    }
                }
            } catch(e: IOException) {
                Log.e(TAG, "Exception while drawing", e)
            }

        canvas.restore()
    }

    fun addValues(values: FloatArray?) {
        try {
            if(values == null)
                return

            if(values.count() != mParamCount)
                return

            //constrain input values
            for(i in 0 until values.count()) {
                if(values[i] > 100.0f) {
                    values[i] = 100.0f
                } else if(values[i] < 0.0f){
                    values[i] = 0.0f
                }
            }

            mLinkedList!!.add(values)
            while(mLinkedList!!.count() > mMaxValues) {
                mLinkedList!!.remove()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Exception while adding values", e)
        }
    }

    fun setColor(index: Int, color: Int) {
        if((index >= mParamCount) or (index < 0))
            return

        mColorList[index] = color
    }
}