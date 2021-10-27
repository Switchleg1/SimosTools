package com.app.simoslogger

import android.content.res.Configuration
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.lang.Exception

open class BaseLoggingFragment: Fragment() {
    open var TAG = "BaseLoggingFragment"
    open var mLastWarning = false
    open var mLayouts: Array<View?>? = null
    open var mGauges: Array<SwitchGauge?>? = null
    open var mTextViews: Array<TextView?>? = null
    open var mPIDsPerLayout = 1
    open var mLayoutType = R.layout.pid_portrait
    open var mLayoutName: Int = R.id.loggingLayoutScroll
    open var mPIDList = byteArrayOf()

    open fun checkOrientation() {
        //check orientation and type
        var currentOrientation = resources.configuration.orientation

        if (Settings.alwaysPortrait)
            currentOrientation = Configuration.ORIENTATION_PORTRAIT

        when(currentOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                mLayoutType = R.layout.pid_land
                mPIDsPerLayout = 3
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                mLayoutType = R.layout.pid_portrait
                mPIDsPerLayout = 2
            }
        }
    }

    open fun onGaugeClick(view: View?): Boolean {
        return true
    }

    open fun buildLayout() {
        view?.let { currentview ->
            try {
                //Build layout
                PIDs.getList()?.let { list ->
                    var layoutCount = mPIDList.count() / mPIDsPerLayout
                    if (mPIDList.count() % mPIDsPerLayout != 0)
                        layoutCount++

                    mLayouts = arrayOfNulls(layoutCount)
                    mGauges = arrayOfNulls(mPIDList.count())
                    mTextViews = arrayOfNulls(mPIDList.count())
                    for (i in 0 until mPIDList.count()) {
                        //build child layout
                        var progID = 0
                        var txtID = 0
                        when (i % mPIDsPerLayout) {
                            0 -> {
                                val pidLayout = layoutInflater.inflate(mLayoutType, null)
                                val lLayout = currentview.findViewById<LinearLayout>(mLayoutName)
                                lLayout.addView(pidLayout)
                                mLayouts!![i / mPIDsPerLayout] = pidLayout
                                progID = R.id.pid_progress
                                txtID = R.id.pid_text
                            }
                            1 -> {
                                progID = R.id.pid_progress1
                                txtID = R.id.pid_text1
                            }
                            2 -> {
                                progID = R.id.pid_progress2
                                txtID = R.id.pid_text2
                            }
                        }

                        //Store progress and text views
                        mGauges!![i] = mLayouts!![i / mPIDsPerLayout]?.findViewById(progID)
                        mTextViews!![i] = mLayouts!![i / mPIDsPerLayout]?.findViewById(txtID)

                        //make visible
                        mGauges!![i]?.isVisible = true
                        mTextViews!![i]?.isVisible = true

                        //get current pid and data
                        val data = PIDs.getData()!![i]!!
                        val pid = list[mPIDList[i].toInt()]!!

                        //find text view and set text
                        val textView = mTextViews!![i]!!
                        textView.text = getString(
                            R.string.textPID,
                            pid.name,
                            pid.format.format(pid.value),
                            pid.unit,
                            pid.format.format(data.min),
                            pid.format.format(data.max)
                        )
                        textView.setTextColor(ColorList.TEXT.value)

                        //Setup the progress bar
                        val gauge = mGauges!![i]!!
                        gauge.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                        gauge.setMinMax(Settings.drawMinMax)
                        val prog = when (data.inverted) {
                            true -> (0 - (pid.value - pid.progMin)) * data.multiplier
                            false -> (pid.value - pid.progMin) * data.multiplier
                        }
                        val progMin = when (data.inverted) {
                            true -> (0 - (data.min - pid.progMin)) * data.multiplier
                            false -> (data.min - pid.progMin) * data.multiplier
                        }
                        val progMax = when (data.inverted) {
                            true -> (0 - (data.max - pid.progMin)) * data.multiplier
                            false -> (data.max - pid.progMin) * data.multiplier
                        }
                        gauge.setProgress(prog, progMin, progMax, false)
                        gauge.setRounded(false, false)
                        gauge.setProgressBackgroundColor(ColorList.GAUGE_BG.value, false)
                        gauge.setStyle(Settings.displayType, false)
                        when (Settings.displayType) {
                            DisplayType.BAR -> gauge.setProgressWidth(250f, false)
                            DisplayType.ROUND -> gauge.setProgressWidth(50f, false)
                        }
                        gauge.setIndex(i)
                        gauge.setOnLongClickListener {
                            onGaugeClick(it)
                        }
                        gauge.setEnable(pid.enabled)
                    }
                }
            } catch (e: Exception) {
                DebugLog.e(TAG, "Unable to build PID layout.", e)
            }
        }
    }

    open fun updatePIDText() {
        //Update text
        try {
            for (i in 0 until mPIDList.count()) {
                val pid = PIDs.getList()!![mPIDList[i].toInt()]
                mTextViews?.let { textView ->
                    PIDs.getData()?.let { datalist ->
                        val data = datalist[mPIDList[i].toInt()]

                        textView[i]?.text = getString(
                            R.string.textPID,
                            pid!!.name,
                            pid.format.format(pid.value),
                            pid.unit,
                            pid.format.format(data?.min),
                            pid.format.format(data?.max)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to update text", e)
        }
    }

    open fun updateProgress() {
        //Set the UI values
        var warnAny = false
        try {
            for (i in 0 until mPIDList.count()) {
                //get the current pid
                val pid = PIDs.getList()!![mPIDList[i].toInt()]!!
                val data = PIDs.getData()!![mPIDList[i].toInt()]!!
                val gauge = mGauges!![i]!!

                var prog = when (data.inverted) {
                    true -> (0 - (pid.value - pid.progMin)) * data.multiplier
                    false -> (pid.value - pid.progMin) * data.multiplier
                }
                val progMin = when (data.inverted) {
                    true -> (0 - (data.min - pid.progMin)) * data.multiplier
                    false -> (data.min - pid.progMin) * data.multiplier
                }
                val progMax = when (data.inverted) {
                    true -> (0 - (data.max - pid.progMin)) * data.multiplier
                    false -> (data.max - pid.progMin) * data.multiplier
                }
                gauge.setProgress(prog, progMin, progMax, false)

                //constrain value
                if(prog > 100f) prog = 100f
                else if(prog < 0f) prog = 0f

                //check if previous value is different
                if (prog != gauge.getProgress()) {
                    gauge.setProgress(prog, progMin, progMax,false)
                }

                //Check to see if we should be warning user
                if(!data.warn)  {
                    gauge.setProgressColor(ColorList.GAUGE_NORMAL.value)
                } else {
                    gauge.setProgressColor(ColorList.GAUGE_WARN.value)
                    warnAny = true
                }
            }
        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to update display", e)
        }

        //If any visible PIDS are in warning state set background color to warn
        if(warnAny) {
            if(!mLastWarning) {
                view?.setBackgroundColor(ColorList.BG_WARN.value)
            }
        } else {
            if(mLastWarning) {
                view?.setBackgroundColor(ColorList.BG_NORMAL.value)
            }
        }
        mLastWarning = warnAny
    }

    open fun setColor() {
        try {
            //Build layout
            mGauges?.let { gauge ->
                mTextViews?.let { text ->
                    var warnAny = false
                    PIDs.getData()?.let { dataList ->
                        for (i in 0 until mPIDList.count()) {
                            //get the current pid
                            val data = dataList[mPIDList[i].toInt()]!!
                            if (data.warn) {
                                gauge[i]?.setProgressColor(ColorList.GAUGE_WARN.value, false)
                                warnAny = true
                            } else {
                                gauge[i]?.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                            }

                            gauge[i]?.setMinMax(Settings.drawMinMax)
                            gauge[i]?.setProgressBackgroundColor(ColorList.GAUGE_BG.value, false)
                            gauge[i]?.setStyle(Settings.displayType, false)

                            when(Settings.displayType) {
                                DisplayType.BAR   -> gauge[i]?.setProgressWidth(250f)
                                DisplayType.ROUND -> gauge[i]?.setProgressWidth(50f)
                            }

                            text[i]?.setTextColor(ColorList.TEXT.value)
                        }
                    }
                    //Set background color
                    if (warnAny) view?.setBackgroundColor(ColorList.BG_WARN.value)
                    else view?.setBackgroundColor(ColorList.BG_NORMAL.value)

                    mLastWarning = warnAny
                }
            }
        } catch(e: Exception) {
            DebugLog.e(TAG, "Unable to update PID colors.", e)
        }
    }
}