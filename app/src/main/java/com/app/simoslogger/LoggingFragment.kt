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

class LoggingViewModel : ViewModel() {
    var lastWarning = false
    var lastEnabled = false
}

class LoggingFragment : Fragment() {
    private var TAG = "LoggingFragment"
    private var mPackCount: TextView? = null
    private var mLayouts: Array<View?>? = null
    private var mGauges: Array<SwitchGauge?>? = null
    private var mTextViews: Array<TextView?>? = null
    private lateinit var mViewModel: LoggingViewModel

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
            //Write pid default files
            UDSLoggingMode.values().forEach { mode ->
                //write current PID list
                PIDCSVFile.write(getString(R.string.filename_pid_csv, mode.cfgName), requireActivity(), PIDs.getList(mode), true)
            }

            //Stop our BT Service
            val serviceIntent = Intent(requireActivity(), BTService::class.java)
            serviceIntent.action = BTServiceTask.STOP_SERVICE.toString()
            ContextCompat.startForegroundService(requireActivity(), serviceIntent)

            requireActivity().finish()
        }

        view.findViewById<Button>(R.id.buttonReset).setOnClickListener {
            //Restart logging
            var serviceIntent = Intent(requireActivity(), BTService::class.java)
            serviceIntent.action = BTServiceTask.DO_STOP_TASK.toString()
            ContextCompat.startForegroundService(requireActivity(), serviceIntent)

            serviceIntent = Intent(requireActivity(), BTService::class.java)
            serviceIntent.action = BTServiceTask.DO_START_LOG.toString()
            ContextCompat.startForegroundService(requireActivity(), serviceIntent)

            PIDs.resetData()
            updatePIDText()
        }

        //check orientation and type
        var pidsPerLayout = 1
        var layoutType = R.layout.pid_portrait
        var currentOrientation = resources.configuration.orientation

        if (Settings.alwaysPortrait)
            currentOrientation = Configuration.ORIENTATION_PORTRAIT

        when(currentOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                layoutType = R.layout.pid_land
                pidsPerLayout = 3
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                layoutType = R.layout.pid_portrait
                pidsPerLayout = 2
            }
        }

        //Set packet textview
        mPackCount = view.findViewById(R.id.textViewPackCount)
        mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)

        try {
            //Build layout
            PIDs.getList()?.let { list ->
                var layoutCount = list.count() / pidsPerLayout
                if(list.count() % pidsPerLayout != 0)
                    layoutCount++

                mLayouts = arrayOfNulls(layoutCount)
                mGauges = arrayOfNulls(list.count())
                mTextViews = arrayOfNulls(list.count())
                for (i in 0 until list.count()) {
                    //build child layout
                    var progID = 0
                    var txtID = 0
                    when(i % pidsPerLayout) {
                        0 -> {
                            val pidLayout = layoutInflater.inflate(layoutType, null)
                            val lLayout = view.findViewById<LinearLayout>(R.id.loggingLayoutScroll)
                            lLayout.addView(pidLayout)
                            mLayouts!![i / pidsPerLayout] = pidLayout
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
                    mGauges!![i] = mLayouts!![i / pidsPerLayout]?.findViewById(progID)
                    mTextViews!![i] = mLayouts!![i / pidsPerLayout]?.findViewById(txtID)

                    //make visible
                    mGauges!![i]?.isVisible = true
                    mTextViews!![i]?.isVisible = true

                    //get current did and data
                    val data = PIDs.getData()!![i]!!
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
                        val progressBar = mGauges!![i]!!
                        progressBar.setProgressColor(ColorList.GAUGE_NORMAL.value, false)
                        val prog = when (data.inverted) {
                            true -> (0 - (did.value - did.progMin)) * data.multiplier
                            false -> (did.value - did.progMin) * data.multiplier
                        }
                        progressBar.setProgress(prog, false)
                        progressBar.setRounded(true, false)
                        progressBar.setProgressBackgroundColor(ColorList.GAUGE_BG.value, false)
                        progressBar.setStyle(Settings.displayType, false)
                        when(Settings.displayType) {
                            DisplayType.BAR   -> progressBar.setProgressWidth(250f, false)
                            DisplayType.ROUND -> progressBar.setProgressWidth(50f, false)
                        }
                        progressBar.setIndex(i)
                        progressBar.setOnLongClickListener {
                            onGaugeClick(it)
                        }
                        progressBar.setEnable(did.enabled)
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
                    PIDs.getData()?.let { datalist ->
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
        try {
            val gauge = (view as SwitchGauge)
            val index = gauge.getIndex()
            PIDs.getList()?.let {
                val isEnabled = it[index]?.enabled == false
                it[index]?.enabled = isEnabled
                gauge.setEnable(isEnabled)
            }
        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to change PID status", e)
        }

        return true
    }

    private fun setColor() {
        try {
            //Build layout
            mGauges?.let { prog ->
                mTextViews?.let { text ->
                    PIDs.getData()?.let { data ->
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
                        mPackCount?.text = readResult.toString()
                        return
                    }

                    //Clear stats are startup
                    if(readCount < 50) {
                        PIDs.resetData()
                    }

                    //Update PID Text
                    updatePIDText()

                    //Set the UI values
                    var anyWarning = false
                    try {
                        for (i in 0 until PIDs.getList()!!.count()) {
                            //get the current did
                            val did = PIDs.getList()!![i]!!
                            val data = PIDs.getData()!![i]!!
                            val progressBar = mGauges!![i]!!

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