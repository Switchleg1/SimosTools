package com.app.simostools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.lang.Exception

open class LoggingBaseFragment: Fragment() {
    open var TAG                            = "LoggingBaseFragment"
    open var mFragmentName                  = "All"
    open var mLastWarning                   = false
    open var mLayouts: Array<View?>?        = null
    open var mGauges: Array<SwitchGauge?>?  = null
    open var mPIDsPerLayout                 = 1
    open var mLayoutType                    = R.layout.pid_portrait
    open var mLayoutName: Int               = R.id.loggingLayoutScroll
    open var mPIDList                       = byteArrayOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //check orientation and type
        checkOrientation()

        DebugLog.d(TAG, "onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        DebugLog.d(TAG, "onDestroyView")
    }

    override fun onResume() {
        super.onResume()

        DebugLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()

        DebugLog.d(TAG, "onPause")
    }

    override fun onStart() {
        super.onStart()

        //Build our layout
        buildLayout()

        //Do we keep the screen on?
        view?.keepScreenOn = ConfigSettings.KEEP_SCREEN_ON.toBoolean()

        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)

        DebugLog.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()

        //Build our layout
        clearLayout()

        //Do we keep the screen on?
        view?.keepScreenOn = false

        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)

        DebugLog.d(TAG, "onStop")
    }

    open fun checkOrientation() {
        //check orientation and type
        var currentOrientation = resources.configuration.orientation

        if (ConfigSettings.ALWAYS_PORTRAIT.toBoolean())
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

    private fun clearLayout() {
        mGauges?.let { gauges ->
            for (i in 0 until gauges.count()) {
                gauges[i] = null
            }
        }
        mGauges = null

        mLayouts?.let { layouts ->
            for (i in 0 until layouts.count()) {
                layouts[i] = null
            }
        }
        mLayouts = null

        view?.let { currentview ->
            //Clear current layout
            val lLayout = currentview.findViewById<LinearLayout>(mLayoutName)
            lLayout.removeAllViews()
        }

        DebugLog.d(TAG, "Cleared layout.")
    }

    open fun buildLayout() {
        view?.let { currentview ->
            try {
                //Build PID List
                buildPIDList()

                //Build layout
                PIDs.getList()?.let { list ->
                    var layoutCount = mPIDList.count() / mPIDsPerLayout
                    if (mPIDList.count() % mPIDsPerLayout != 0)
                        layoutCount++

                    mLayouts = arrayOfNulls(layoutCount)
                    mGauges = arrayOfNulls(mPIDList.count())
                    for (i in 0 until mPIDList.count()) {
                        //build child layout
                        var progID = 0
                        when (i % mPIDsPerLayout) {
                            0 -> {
                                val pidLayout = layoutInflater.inflate(mLayoutType, null)
                                val lLayout = currentview.findViewById<LinearLayout>(mLayoutName)
                                lLayout.addView(pidLayout)
                                mLayouts!![i / mPIDsPerLayout] = pidLayout
                                progID = R.id.pid_gauge
                            }
                            1 -> progID = R.id.pid_gauge1
                            2 -> progID = R.id.pid_gauge2
                        }

                        //get current pid and data
                        val data = PIDs.getData()!![mPIDList[i].toInt()]!!
                        val pid = list[mPIDList[i].toInt()]!!

                        //Setup the progress bar
                        mGauges!![i] = mLayouts!![i / mPIDsPerLayout]?.findViewById(progID)
                        val gauge = mGauges!![i]!!
                        gauge.isVisible = true
                        gauge.text = getString(R.string.textPID, pid.name, pid.format.format(pid.value),
                                    pid.unit, pid.format.format(data.min), pid.format.format(data.max))
                        gauge.setTextColor(ColorList.TEXT.value)
                        gauge.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                        gauge.setMinMaxColor(ColorList.GAUGE_WARN.value, false)
                        gauge.setMinMax(ConfigSettings.DRAW_MIN_MAX.toBoolean(), false)
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
                        gauge.setProgressBackgroundColor(ColorList.GAUGE_BG.value, false)
                        gauge.setStyle(ConfigSettings.GAUGE_TYPE.toGaugeType(), false)
                        when (ConfigSettings.GAUGE_TYPE.toGaugeType()) {
                            GaugeType.BAR -> gauge.setProgressWidth(250f, false)
                            GaugeType.ROUND -> gauge.setProgressWidth(50f, false)
                        }
                        if(kotlin.math.abs(pid.progMin) == kotlin.math.abs(pid.progMax))
                            gauge.setCentered(true, false)
                        gauge.setGraduations(ConfigSettings.DRAW_GRADUATIONS.toBoolean(), false)
                        gauge.setIndex(i)
                        gauge.setOnLongClickListener {
                            onGaugeClick(it)
                        }
                        gauge.setEnable(pid.enabled)
                    }

                    DebugLog.d(TAG, "Built layout.")
                }
            } catch (e: Exception) {
                DebugLog.e(TAG, "Unable to build PID layout.", e)
            }
        }
    }

    open fun updateGauges() {
        //Set the UI values
        var warnAny = false
        var lastI = -1
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
                    gauge.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                    gauge.setMinMaxColor(ColorList.GAUGE_WARN.value, false)
                } else {
                    gauge.setProgressColor(ColorList.GAUGE_WARN.value, false)
                    gauge.setMinMaxColor(ColorList.GAUGE_NORMAL.value, false)
                    warnAny = true
                }

                //update text which will invalidate and redraw
                gauge.text = getString(
                    R.string.textPID,
                    pid.name,
                    pid.format.format(pid.value),
                    pid.unit,
                    pid.format.format(data.min),
                    pid.format.format(data.max)
                )

                lastI = i
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
        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to update display [$lastI:${mPIDList.count()}]", e)
            buildLayout()
        }
    }

    open fun buildPIDList() {
    }

    open fun onGaugeClick(view: View?): Boolean {
        return true
    }

    fun getName(): String {
        return mFragmentName
    }
}