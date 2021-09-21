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

    override fun onDraw(canvas: Canvas) {
        canvas.save()
            try {
                canvas.translate(width * 0.5f, height * 0.5f)

                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                p.color = Color.RED
                p.strokeWidth = 10f
                for (i in 0 until mMaxValues) {
                    if (i >= mLinkedList!!.count())
                        break

                    canvas.drawLine(i * 10f, 0f, 200f, 200f, p)
                }
            } catch(e: IOException) {
                Log.e(TAG, "Exception while drawing", e)
            }

        canvas.restore()
    }

    fun addValues(values: FloatArray?) {
        try {
            mLinkedList!!.add(values)
            while(mLinkedList!!.count() > mMaxValues) {
                mLinkedList!!.drop(1)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Exception while adding values", e)
        }
    }
}