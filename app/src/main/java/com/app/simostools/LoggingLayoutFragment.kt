package com.app.simostools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.lang.Exception

class LoggingBaseFragment: Fragment() {
    private var TAG                             = "LoggingBaseFragment"
    private var mFragmentName                   = "ECU"
    private var mLastWarning                    = false
    private var mLayouts: Array<View?>?         = null
    private var mGauges: Array<SwitchGauge?>?   = null
    private var mPIDsPerLayout                  = 1
    private var mLayoutName: Int                = R.id.loggingLayoutScroll
    private var mPIDList                        = byteArrayOf()

    override fun onDestroy() {
        super.onDestroy()

        //Clear our layout
        clearLayout()

        DebugLog.d(TAG, "onDestroy")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_logging_layout, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf { it.containsKey(LAYOUT_NAME) }?.apply {
            mFragmentName = getString(LAYOUT_NAME, mFragmentName).toString()
        }

        //check orientation and type
        checkOrientation()
        buildLayout()

        DebugLog.d(TAG, "onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        DebugLog.d(TAG, "onDestroyView")
    }

    override fun onResume() {
        super.onResume()

        //buildLayout()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.READ_LOG.toString())
        activity?.registerReceiver(mBroadcastReceiver, filter)

        //Do we keep the screen on?
        view?.keepScreenOn = ConfigSettings.KEEP_SCREEN_ON.toBoolean()

        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)

        DebugLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()

        //Clear our layout
        //clearLayout()

        activity?.unregisterReceiver(mBroadcastReceiver)

        //Do we keep the screen on?
        view?.keepScreenOn = false

        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)

        DebugLog.d(TAG, "onPause")
    }

    override fun onStart() {
        super.onStart()

        DebugLog.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()

        DebugLog.d(TAG, "onStop")
    }

    private fun checkOrientation() {
        //check orientation and type
        var currentOrientation = resources.configuration.orientation

        if (ConfigSettings.ALWAYS_PORTRAIT.toBoolean())
            currentOrientation = Configuration.ORIENTATION_PORTRAIT

        when(currentOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                mPIDsPerLayout = 3
            }
            Configuration.ORIENTATION_PORTRAIT -> {
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

    private fun buildLayout() {
        view?.let { currentview ->

            try {
                //Build PID List
                buildPIDList(mFragmentName == "ECU", mFragmentName == "DSG")

                //Build layout
                val pidList = if(mFragmentName == "DSG") PIDs.getDSGList()
                else PIDs.getList()
                pidList?.let { list ->
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
                                val pidLayout = layoutInflater.inflate(R.layout.fragment_pid, null)
                                val lLayout = currentview.findViewById<LinearLayout>(mLayoutName)
                                lLayout.addView(pidLayout)
                                mLayouts!![i / mPIDsPerLayout] = pidLayout
                                progID = R.id.pid_gauge
                            }
                            1 -> progID = R.id.pid_gauge1
                            2 -> progID = R.id.pid_gauge2
                        }

                        //get current data
                        val pidData = if(mFragmentName == "DSG") PIDs.getDSGData()
                        else PIDs.getData()

                        val data = pidData!![mPIDList[i].toInt()]!!
                        val pid = list[mPIDList[i].toInt()]!!

                        //Setup the progress bar
                        mGauges!![i] = mLayouts!![i / mPIDsPerLayout]?.findViewById(progID)
                        val gauge = mGauges!![i]!!
                        gauge.isVisible = true
                        gauge.setTextColor(ColorList.TEXT.value)
                        gauge.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                        gauge.setRimColor(ColorList.BT_RIM.value)
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
                            GaugeType.BAR_H -> gauge.setProgressWidth(400f, false)
                            GaugeType.BAR_V -> gauge.setProgressWidth(400f, false)
                            GaugeType.BASIC -> gauge.setProgressWidth(400f, false)
                            GaugeType.ROUND -> {
                                gauge.setProgressWidth(50f, false)
                                gauge.textSize = gauge.textSize * 0.25f
                            }
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
                    DebugLog.d(TAG, "buildLayout ${mGauges?.count()}")
                    updateGauges()
                }
            } catch (e: Exception) {
                DebugLog.e(TAG, "buildLayout - exception", e)
            }
        } ?: DebugLog.d(TAG, "buildLayout - view is invalid.")
    }

    private fun updateGauges() {
        mGauges?.let { gauges ->
            //Set the UI values
            var warnAny = false
            var lastI = -1
            try {
                if(gauges.count() != mPIDList.count()) {
                    DebugLog.d(TAG, "updateGauges - gauge count does not match pid count[${gauges.count()}:${mPIDList.count()}]")
                }
                for (i in 0 until mPIDList.count()) {
                    //get current PID & data
                    val pidList = if(mFragmentName == "DSG") PIDs.getDSGList()
                    else PIDs.getList()
                    val pidData = if(mFragmentName == "DSG") PIDs.getDSGData()
                    else PIDs.getData()

                    //get the current pid
                    val pid = pidList!![mPIDList[i].toInt()]!!
                    val data = pidData!![mPIDList[i].toInt()]!!
                    val gauge = gauges[i]!!

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
                    if (prog > 100f) prog = 100f
                    else if (prog < 0f) prog = 0f

                    //check if previous value is different
                    if (prog != gauge.getProgress()) {
                        gauge.setProgress(prog, progMin, progMax, false)
                    }

                    //Check to see if we should be warning user
                    if (!data.warn) {
                        gauge.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                        gauge.setMinMaxColor(ColorList.GAUGE_WARN.value, false)
                        gauge.setProgressBackgroundColor(ColorList.GAUGE_BG.value, false)
                    } else {
                        gauge.setProgressColor(ColorList.GAUGE_WARN.value, false)
                        gauge.setMinMaxColor(ColorList.GAUGE_NORMAL.value, false)
                        gauge.setProgressBackgroundColor(ColorList.BG_WARN.value, false)
                        warnAny = true
                    }

                    //update text which will invalidate and redraw
                    gauge.text = Html.fromHtml(
                        "<b><small>${pid.name}<br></small><big>" +
                                "<font color=\"#${ColorList.GAUGE_VALUE.value.toColorHex()}\">" +
                                "${pid.format.format(pid.value)}</font></big></b>" +
                                "<small><br>${pid.format.format(data.min)} <b>:</b> " +
                                "${pid.format.format(data.max)}<br>${pid.unit}</small>"
                        , Html.FROM_HTML_OPTION_USE_CSS_COLORS)

                    lastI = i
                }

                //If any visible PIDS are in warning state set background color to warn
                if (warnAny) {
                    if (!mLastWarning) {
                        mLastWarning = true
                        view?.setBackgroundColor(ColorList.BG_WARN.value)
                    } else {
                        mLastWarning = false
                        view?.setBackgroundColor(ColorList.BG_NORMAL.value)
                    }
                } else {
                    if (mLastWarning) {
                        view?.setBackgroundColor(ColorList.BG_NORMAL.value)
                    }
                    mLastWarning = false
                }

                DebugLog.d(TAG, "updateGauges [${lastI+1}:${mPIDList.count()}]")
            } catch (e: Exception) {
                DebugLog.e(TAG, "updateGauges - exception [${lastI+1}:${mPIDList.count()}]", e)
            }
        }?: run {
            DebugLog.d(TAG, "updateGauges - gauges are invalid pidlist count ${mPIDList.count()}")
        }
    }

    private fun buildPIDList(all: Boolean, dsg: Boolean) {
        //Build our list of PIDS in this layout
        val pidList = if(dsg) PIDs.getDSGList()
        else PIDs.getList()
        pidList?.let { list ->
            //get list of custom PIDS
            var customList = byteArrayOf()
            for (i in 0 until list.count()) {
                list[i]?.let { pid ->
                    if (pid.enabled && (all || dsg || pid.tabs.contains(mFragmentName))) {
                        customList += i.toByte()
                    }
                }
            }
            if(!all && !dsg) {
                for (i in 0 until customList.count()) {
                    var movedAhead = false
                    var lastPos = -1
                    do {
                        movedAhead = false
                        list[customList[i].toInt()]?.let { pid ->
                            pid.tabs.split(".").forEach {
                                try {
                                    val curPos = customList[i]
                                    val pidPos = it.substringAfter("|").toInt()
                                    if (pidPos < customList.count()) {
                                        customList[i] = customList[pidPos]
                                        customList[pidPos] = curPos
                                        if (pidPos > curPos && pidPos != lastPos)
                                            movedAhead = true

                                        lastPos = pidPos   //prevent an endless loop
                                    } else {
                                        lastPos = -1
                                    }
                                } catch (e: Exception) {
                                    DebugLog.d(TAG, "Error in PID layout position")
                                }
                            }
                        }
                    } while (movedAhead)
                }
            }
            mPIDList = customList
        }
    }

    private fun onGaugeClick(view: View?): Boolean {
        PIDs.resetData(mFragmentName == "DSG")
        updateGauges()

        return true
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                GUIMessage.READ_LOG.toString() -> {
                    val readCount = intent.getIntExtra("readCount", 0)
                    val readTime = intent.getLongExtra("readTime", 0)
                    val readResult = intent.getSerializableExtra("readResult") as UDSReturn

                    //Make sure we received an ok
                    if (readResult != UDSReturn.OK) {
                        return
                    }

                    //Update callback
                    updateGauges()
                }
                else -> { }
            }
        }
    }
}