package com.app.simostools

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import java.lang.Exception

class LoggingViewModel : ViewModel() {
    var currentTask: UDSTask = UDSTask.NONE
}

class LoggingFullFragment : LoggingBaseFragment() {
    override var TAG                    = "LoggingFragment"
    override var mFragmentName          = "All"
    private var mLastEnabled            = false
    private var mPackCount: TextView?   = null
    private lateinit var mViewModel: LoggingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_full, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(this).get(LoggingViewModel::class.java)

        val backButton = view.findViewById<SwitchButton>(R.id.buttonLoggingBack)
        backButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                findNavController().navigateUp()
            }
        }

        val resetButton = view.findViewById<SwitchButton>(R.id.buttonLoggingReset)
        resetButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                if (mViewModel.currentTask == UDSTask.NONE) {
                    sendServiceMessage(BTServiceTask.DO_START_LOG.toString())
                } else {
                    sendServiceMessage(BTServiceTask.DO_STOP_TASK.toString())
                }

                //update GUI
                PIDs.resetData()
                updatePIDText()
            }
        }

        //Set packet textview
        mPackCount = view.findViewById(R.id.textViewPackCount)
        mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)

        setLoggingButton()
    }

    private fun setLoggingButton() {
        view?.findViewById<SwitchButton>(R.id.buttonLoggingReset)?.let {
            it.text = when(mViewModel.currentTask) {
                UDSTask.LOGGING -> "Stop"
                else            -> "Start"
            }
        }
    }

    override fun onSetFilter(filter: IntentFilter) {
        filter.addAction(GUIMessage.STATE_CONNECTION.toString())
        filter.addAction(GUIMessage.STATE_TASK.toString())
    }

    override fun onNewMessage(intent: Intent) {
        when(intent.action) {
            GUIMessage.STATE_TASK.toString()       -> mViewModel.currentTask = intent.getSerializableExtra(GUIMessage.STATE_TASK.toString()) as UDSTask
            GUIMessage.STATE_CONNECTION.toString() -> mViewModel.currentTask = UDSTask.NONE
        }
        setLoggingButton()
    }

    override fun buildPIDList() {
        super.buildPIDList()

        //Build our list of PIDS in this layout
        PIDs.getList()?.let { list ->
            //get list of all PIDS
            var logList = byteArrayOf()
            for (i in 0 until list.count()) {
                logList += i.toByte()
            }
            mPIDList = logList
        }
    }

    override fun onGaugeClick(view: View?): Boolean {
        try {
            val gauge = (view as SwitchGauge)
            val index = gauge.getIndex()
            PIDs.getList()?.let {
                val isEnabled = it[index]?.enabled == false
                it[index]?.enabled = isEnabled
                gauge.setEnable(isEnabled)
            }
            if(mViewModel.currentTask == UDSTask.LOGGING)
                sendServiceMessage(BTServiceTask.DO_START_LOG.toString())

        } catch (e: Exception) {
            DebugLog.e(TAG, "Unable to change PID status", e)
        }

        return super.onGaugeClick(view)
    }

    override fun doUpdate(readCount: Int, readTime: Long) {
        //Clear stats are startup
        if(readCount < 50) {
            PIDs.resetData()
        }

        //Update fps
        val fps = readCount.toFloat() / (readTime.toFloat() / 1000.0f)
        mPackCount?.text = getString(R.string.textview_fps, "%03.1f".format(fps))
        if (UDSLogger.isEnabled()) {
            //Highlight packet count in red since we are logging
            if(!mLastEnabled) {
                mPackCount?.setTextColor(ColorList.GAUGE_WARN.value)
            }
        } else {
            //Not logging set packet count to black
            if(mLastEnabled) {
                mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)
            }
        }
        mLastEnabled = UDSLogger.isEnabled()
    }

    private fun sendServiceMessage(type: String) {
        val serviceIntent = Intent(requireActivity(), BTService::class.java)
        serviceIntent.action = type
        ContextCompat.startForegroundService(requireActivity(), serviceIntent)
    }
}