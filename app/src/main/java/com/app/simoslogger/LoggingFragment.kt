package com.app.simoslogger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.content.res.Configuration
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.Exception

data class DATAStruct(var min: Float,
                        var max: Float,
                        var lastColor: Boolean,
                        var multiplier: Float,
                        var inverted: Boolean)

class LoggingViewModel : ViewModel() {
    var pidsPerLayout = 1
    var lastWarning = false
    var lastEnabled = false
    var dataList: Array<DATAStruct?>? = null
}

class LoggingFragment : Fragment() {
    private var TAG = "LoggingFragment"
    private var mPackCount: TextView? = null
    private var mLayouts: Array<View?>? = null
    private var mProgressBars: Array<SwitchGauge?>? = null
    private var mTextViews: Array<TextView?>? = null
    private lateinit var mViewModel: LoggingViewModel

    fun setColor() {
        try {
            //Build layout
            mProgressBars?.let { prog ->
                mTextViews?.let { text ->
                    mViewModel.dataList?.let { data ->
                        for (i in 0 until prog.count()) {
                            //get the current did
                            val dataList = data[i]!!
                            if (dataList.lastColor) prog[i]?.setProgressColor(ColorList.GAUGE_WARN.value)
                                else prog[i]?.setProgressColor(ColorList.GAUGE_NORMAL.value)

                            prog[i]?.setProgressBackgroundColor(ColorList.GAUGE_BG.value)
                            prog[i]?.setStyle(Settings.displayType)

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get view model
        mViewModel = ViewModelProvider(this).get(LoggingViewModel::class.java)

        view.findViewById<Button>(R.id.buttonExit).setOnClickListener {
            //Stop our BT Service
            val serviceIntent = Intent(requireActivity(), BTService::class.java)
            serviceIntent.action = BTServiceTask.STOP_SERVICE.toString()
            ContextCompat.startForegroundService(requireActivity(), serviceIntent)

            requireActivity().finish()
        }

        view.findViewById<Button>(R.id.buttonReset).setOnClickListener {
            resetStats()
        }

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

        //Set packet textview
        mPackCount = view.findViewById(R.id.textViewPackCount)
        mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)

        try {
            //Build layout
            PIDs.getList()?.let { list ->
                if (mViewModel.dataList == null || mViewModel.dataList?.count() != list.count()) {
                    mViewModel.dataList = arrayOfNulls(list.count())
                }

                var layoutCount = list.count() / mViewModel.pidsPerLayout
                if(list.count() % mViewModel.pidsPerLayout != 0)
                    layoutCount++

                mLayouts = arrayOfNulls(layoutCount)
                mProgressBars = arrayOfNulls(list.count())
                mTextViews = arrayOfNulls(list.count())
                for (i in 0 until list.count()) {
                    //build child layout
                    var progID = 0
                    var txtID = 0
                    when(i % mViewModel.pidsPerLayout) {
                        0 -> {
                            val pidLayout = layoutInflater.inflate(layoutType, null)
                            val lLayout = view.findViewById<LinearLayout>(R.id.loggingLayoutScroll)
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
                    mProgressBars!![i] = mLayouts!![i / mViewModel.pidsPerLayout]?.findViewById(progID)
                    mTextViews!![i] = mLayouts!![i / mViewModel.pidsPerLayout]?.findViewById(txtID)

                    //make visible
                    mProgressBars!![i]?.isVisible = true
                    mTextViews!![i]?.isVisible = true

                    //build new data structure if first time around
                    var data = DATAStruct(0.0f, 0.0f, false, 1.0f, false)
                    if (mViewModel.dataList!![i] == null) {
                        mViewModel.dataList!![i] = data
                    }
                    data = mViewModel.dataList!![i]!!

                    //get current did
                    val did = list[i]
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
                        val progressBar = mProgressBars!![i]!!
                        progressBar.setProgressColor(ColorList.GAUGE_NORMAL.value)
                        val prog = when (data.inverted) {
                            true -> (0 - (did.value - did.progMin)) * data.multiplier
                            false -> (did.value - did.progMin) * data.multiplier
                        }
                        progressBar.setProgress(prog)
                        progressBar.setRounded(true)
                        progressBar.setProgressBackgroundColor(ColorList.GAUGE_BG.value)
                        progressBar.setStyle(Settings.displayType)
                        when(Settings.displayType) {
                            DisplayType.BAR   -> progressBar.setProgressWidth(250f)
                            DisplayType.ROUND -> progressBar.setProgressWidth(50f)
                        }
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
            for (i in 0 until PIDs.getList()!!.count()) {
                val did = PIDs.getList()!![i]
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

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                GUIMessage.READ_LOG.toString() -> {
                    val readCount = intent.getIntExtra("readCount", 0)
                    val readTime = intent.getLongExtra("readTime", 0)
                    val readResult = intent.getSerializableExtra("readResult") as UDSReturn

                    //Make sure we received an ok
                    if(readResult != UDSReturn.OK) {
                        mPackCount?.text = readResult.toString()
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
                    try {
                        for (i in 0 until PIDs.getList()!!.count()) {
                            //get the current did
                            val did = PIDs.getList()!![i]!!
                            val data = mViewModel.dataList!![i]!!
                            val progressBar = mProgressBars!![i]!!

                            //set min/max
                            if (did.value > data.max)
                                data.max = did.value

                            if (did.value < data.min)
                                data.min = did.value

                            //Update progress is the value is different
                            var newProgress = when(data.inverted) {
                                true -> (0 - (did.value - did.progMin)) * data.multiplier
                                false -> (did.value - did.progMin) * data.multiplier
                            }

                            //constrain value
                            if(newProgress > 100f) newProgress = 100f
                                else if(newProgress < 0f) newProgress = 0f

                            //check if previous value is different
                            if (newProgress != progressBar.getProgress()) {
                                progressBar.setProgress(newProgress)
                            }

                            //Check to see if we should be warning user
                            if ((did.value > did.warnMax) or (did.value < did.warnMin)) {

                                if (!data.lastColor) {
                                    progressBar.setProgressColor(ColorList.GAUGE_WARN.value)
                                }

                                data.lastColor = true
                                anyWarning = true
                            } else {
                                if (data.lastColor) {
                                    progressBar.setProgressColor(ColorList.GAUGE_NORMAL.value)
                                }

                                data.lastColor = false
                            }
                        }
                    } catch (e: Exception) {
                        DebugLog.e(TAG, "Unable to update display", e)
                    }

                    //If any visible PIDS are in warning state set background color to warn
                    if(anyWarning) {
                        if(!mViewModel.lastWarning) {
                            view?.setBackgroundColor(ColorList.BG_WARN.value)
                        }

                        mViewModel.lastWarning = true
                    } else {
                        if(mViewModel.lastWarning) {
                            view?.setBackgroundColor(ColorList.BG_NORMAL.value)
                        }

                        mViewModel.lastWarning = false
                    }

                    //Update fps
                    val fps = readCount.toFloat() / (readTime.toFloat() / 1000.0f)
                    mPackCount?.text = getString(R.string.textview_fps, "%03.1f".format(fps))
                    if (UDSLogger.isEnabled()) {
                        //Highlight packet count in red since we are logging
                        if(!mViewModel.lastEnabled) {
                            mPackCount?.setTextColor(ColorList.GAUGE_WARN.value)
                        }
                        mViewModel.lastEnabled = true
                    } else {
                        //Not logging set packet count to black
                        if(mViewModel.lastEnabled) {
                            mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)
                        }
                        mViewModel.lastEnabled = false
                    }
                }
            }
        }
    }
}