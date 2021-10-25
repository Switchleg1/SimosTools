package com.app.simoslogger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.Exception

class CustomViewModel : ViewModel() {
    var pidsPerLayout = 1
    var lastWarning = false
    var lastEnabled = false
    var dataList: Array<DATAStruct?>? = null
}

class CustomFragment1 : CustomFragment() {
    override val TAG = "CustomFragment1"
    override var mCustomName: String = "Custom1"
    override var mLayoutName: Int = R.id.CustomLayoutScroll1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom1, container, false)
    }
}

class CustomFragment2 : CustomFragment() {
    override val TAG = "CustomFragment2"
    override var mCustomName: String = "Custom2"
    override var mLayoutName: Int = R.id.CustomLayoutScroll2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom2, container, false)
    }
}

class CustomFragment3 : CustomFragment() {
    override val TAG = "CustomFragment3"
    override var mCustomName: String = "Custom3"
    override var mLayoutName: Int = R.id.CustomLayoutScroll3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom3, container, false)
    }
}

class CustomFragment4 : CustomFragment() {
    override val TAG = "CustomFragment4"
    override var mCustomName: String = "Custom4"
    override var mLayoutName: Int = R.id.CustomLayoutScroll4

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom4, container, false)
    }
}

open class CustomFragment : Fragment() {
    open val TAG = "CustomFragment"
    private var mLayouts: Array<View?>? = null
    private var mGauges: Array<SwitchGauge?>? = null
    private var mTextViews: Array<TextView?>? = null
    open var mCustomName: String = "Custom1"
    open var mLayoutName: Int = R.id.CustomLayoutScroll1
    private lateinit var mViewModel: CustomViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get view model
        mViewModel = ViewModelProvider(this).get(CustomViewModel::class.java)

        //check orientation and type
        var layoutType = R.layout.pid_portrait
        var currentOrientation = resources.configuration.orientation

        if (Settings.alwaysPortrait)
            currentOrientation = Configuration.ORIENTATION_PORTRAIT

        when(currentOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                layoutType = R.layout.pid_land
                mViewModel.pidsPerLayout = 3
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                layoutType = R.layout.pid_portrait
                mViewModel.pidsPerLayout = 2
            }
        }

        try {
            //Build layout
            PIDs.getList()?.let { list ->
                //get list of custom PIDS
                var customList = intArrayOf()
                for (i in 0 until list.count()) {
                    val did = list[i]!!
                    if(did.enabled && did.tabs.contains(mCustomName)) {
                        customList += i
                    }
                }

                DebugLog.d(TAG, "Custom count: ${customList.count()}")

                if (mViewModel.dataList == null || mViewModel.dataList?.count() != customList.count()) {
                    mViewModel.dataList = arrayOfNulls(customList.count())
                }

                var layoutCount = customList.count() / mViewModel.pidsPerLayout
                if(customList.count() % mViewModel.pidsPerLayout != 0)
                    layoutCount++

                DebugLog.d(TAG, "Layout count: $layoutCount")

                mLayouts = arrayOfNulls(layoutCount)
                mGauges = arrayOfNulls(customList.count())
                mTextViews = arrayOfNulls(customList.count())
                for (i in 0 until customList.count()) {
                    //build child layout
                    var progID = 0
                    var txtID = 0
                    when(i % mViewModel.pidsPerLayout) {
                        0 -> {
                            val pidLayout = layoutInflater.inflate(layoutType, null)
                            val lLayout = view.findViewById<LinearLayout>(mLayoutName)
                            lLayout.addView(pidLayout)
                            mLayouts!![i / mViewModel.pidsPerLayout] = pidLayout
                            progID = R.id.pid_progress
                            txtID = R.id.pid_text
                        }
                        1-> {
                            progID = R.id.pid_progress1
                            txtID = R.id.pid_text1
                        }
                        2-> {
                            progID = R.id.pid_progress2
                            txtID = R.id.pid_text2
                        }
                    }

                    //Store progress and text views
                    mGauges!![i] = mLayouts!![i / mViewModel.pidsPerLayout]?.findViewById(progID)
                    mTextViews!![i] = mLayouts!![i / mViewModel.pidsPerLayout]?.findViewById(txtID)

                    //make visible
                    mGauges!![i]?.isVisible = true
                    mTextViews!![i]?.isVisible = true

                    //build new data structure if first time around
                    var data = DATAStruct(0.0f, 0.0f, false, 1.0f, false)
                    if (mViewModel.dataList!![i] == null) {
                        mViewModel.dataList!![i] = data
                    }
                    data = mViewModel.dataList!![i]!!

                    //get current did
                    val did = list[customList[i]]
                    did?.let {
                        //Check for low value PIDS
                        var progMax = did.progMax
                        var progMin = did.progMin

                        //if progress bar is flipped
                        if (did.progMin > did.progMax) {
                            progMax = did.progMin
                            progMin = did.progMax
                            data.inverted = true
                        }

                        //build progress multiplier
                        data.multiplier = 100.0f / (progMax - progMin)

                        //find text view and set text
                        val textView = mTextViews!![i]!!
                        textView.text = getString(
                            R.string.textPID,
                            did.name,
                            did.format.format(did.value),
                            did.unit,
                            did.format.format(data.min),
                            did.format.format(data.max)
                        )

                        //Setup the progress bar
                        val guage = mGauges!![i]!!
                        guage.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                        val prog = when (data.inverted) {
                            true -> (0 - (did.value - did.progMin)) * data.multiplier
                            false -> (did.value - did.progMin) * data.multiplier
                        }
                        guage.setProgress(prog, false)
                        guage.setRounded(true, false)
                        guage.setProgressBackgroundColor(ColorList.GAUGE_BG.value, false)
                        guage.setStyle(Settings.displayType, false)
                        when(Settings.displayType) {
                            DisplayType.BAR   -> guage.setProgressWidth(250f, false)
                            DisplayType.ROUND -> guage.setProgressWidth(50f, false)
                        }
                        guage.setIndex(customList[i])
                        guage.setOnLongClickListener {
                            onGaugeClick(it)
                        }
                        guage.setEnable(did.enabled)
                    }
                }
            }
        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to build PID layout.", e)
        }

        //Do we keep the screen on?
        view.keepScreenOn = Settings.keepScreenOn

        //update PID text
        updatePIDText()

        //Set background color
        if (mViewModel.lastWarning) view.setBackgroundColor(ColorList.BG_WARN.value)
            else view.setBackgroundColor(ColorList.BG_NORMAL.value)
    }

    override fun onResume() {
        super.onResume()

        setColor()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.READ_LOG.toString())
        this.activity?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        this.activity?.unregisterReceiver(mBroadcastReceiver)
    }

    private fun updatePIDText() {
        //Update text
        try {
            for (i in 0 until mTextViews!!.count()) {
                val did = PIDs.getList()!![mGauges!![i]!!.getIndex()]
                mTextViews?.let { textView ->
                    mViewModel.dataList?.let { datalist ->
                        val data = datalist[i]

                        textView[i]?.text = getString(
                            R.string.textPID,
                            did!!.name,
                            did.format.format(did.value),
                            did.unit,
                            did.format.format(data?.min),
                            did.format.format(data?.max)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to update text", e)
        }
    }

    private fun onGaugeClick(view: View?): Boolean {
        resetStats()

        return true
    }

    private fun resetStats() {
        try {
            mViewModel.dataList?.let { dataList ->
                for (i in 0 until dataList.count()) {
                    val data = dataList[i]
                    data?.let {
                        val did = PIDs.getList()!![i]
                        did?.let {
                            data.max = did.value
                            data.min = did.value
                        }
                    }
                }
            }
        } catch (e: Exception)
        {
            DebugLog.e(TAG, "Unable to reset min/max list.", e)
        }

        updatePIDText()
    }

    private fun setColor() {
        try {
            //Build layout
            mGauges?.let { prog ->
                mTextViews?.let { text ->
                    mViewModel.dataList?.let { data ->
                        for (i in 0 until prog.count()) {
                            //get the current did
                            val dataList = data[i]!!
                            if (dataList.lastColor) prog[i]?.setProgressColor(ColorList.GAUGE_WARN.value, false)
                            else prog[i]?.setProgressColor(ColorList.GAUGE_NORMAL.value, false)

                            prog[i]?.setProgressBackgroundColor(ColorList.GAUGE_BG.value, false)
                            prog[i]?.setStyle(Settings.displayType, false)

                            when(Settings.displayType) {
                                DisplayType.BAR   -> prog[i]?.setProgressWidth(250f)
                                DisplayType.ROUND -> prog[i]?.setProgressWidth(50f)
                            }

                            text[i]?.setTextColor(ColorList.TEXT.value)
                        }
                    }
                    //Set background color
                    if (mViewModel.lastWarning) view?.setBackgroundColor(ColorList.BG_WARN.value)
                    else view?.setBackgroundColor(ColorList.BG_NORMAL.value)
                }
            }
        } catch(e: Exception) {
            DebugLog.e(TAG, "Unable to update PID colors.", e)
        }
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                GUIMessage.READ_LOG.toString() -> {
                    val readCount = intent.getIntExtra("readCount", 0)
                    val readTime = intent.getLongExtra("readTime", 0)
                    val readResult = intent.getSerializableExtra("readResult") as UDSReturn

                    //Make sure we received an ok
                    if(readResult != UDSReturn.OK) {
                        return
                    }

                    //Clear stats are startup
                    if(readCount < 50) {
                        resetStats()
                    }

                    //Update PID Text
                    updatePIDText()

                    //Set the UI values
                    var anyWarning = false
                    mGauges?.let { gauges ->
                        try {
                            for (i in 0 until gauges.count()) {
                                //get the current did
                                val gauge = gauges[i]!!
                                val did = PIDs.getList()!![gauge.getIndex()]!!
                                val data = mViewModel.dataList!![i]!!

                                //set min/max
                                if (did.value > data.max)
                                    data.max = did.value

                                if (did.value < data.min)
                                    data.min = did.value

                                //Update progress is the value is different
                                var newProgress = when (data.inverted) {
                                    true -> (0 - (did.value - did.progMin)) * data.multiplier
                                    false -> (did.value - did.progMin) * data.multiplier
                                }

                                //constrain value
                                if (newProgress > 100f) newProgress = 100f
                                    else if (newProgress < 0f) newProgress = 0f

                                //check if previous value is different
                                if (newProgress != gauge.getProgress()) {
                                    gauge.setProgress(newProgress)
                                }

                                //Check to see if we should be warning user
                                if ((did.value > did.warnMax) or (did.value < did.warnMin)) {

                                    if (!data.lastColor) {
                                        gauge.setProgressColor(ColorList.GAUGE_WARN.value)
                                    }

                                    data.lastColor = true
                                    anyWarning = true
                                } else {
                                    if (data.lastColor) {
                                        gauge.setProgressColor(ColorList.GAUGE_NORMAL.value)
                                    }

                                    data.lastColor = false
                                }
                            }
                        } catch (e: Exception) {
                            DebugLog.e(TAG, "Unable to update custom display", e)
                        }

                        //If any visible PIDS are in warning state set background color to warn
                        if (anyWarning) {
                            if (!mViewModel.lastWarning) {
                                view?.setBackgroundColor(ColorList.BG_WARN.value)
                            }

                            mViewModel.lastWarning = true
                        } else {
                            if (mViewModel.lastWarning) {
                                view?.setBackgroundColor(ColorList.BG_NORMAL.value)
                            }

                            mViewModel.lastWarning = false
                        }
                    }
                }
            }
        }
    }
}