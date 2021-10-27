package com.app.simoslogger

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.lang.Exception

class LoggingFragment : BaseLoggingFragment() {
    override var TAG = "LoggingFragment"
    private var mLastEnabled = false
    private var mPackCount: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            PIDs.resetData()
            updatePIDText()
        }

        //Set packet textview
        mPackCount = view.findViewById(R.id.textViewPackCount)
        mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)
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

            //Restart logging
            var serviceIntent = Intent(requireActivity(), BTService::class.java)
            serviceIntent.action = BTServiceTask.DO_STOP_TASK.toString()
            ContextCompat.startForegroundService(requireActivity(), serviceIntent)

            serviceIntent = Intent(requireActivity(), BTService::class.java)
            serviceIntent.action = BTServiceTask.DO_START_LOG.toString()
            ContextCompat.startForegroundService(requireActivity(), serviceIntent)
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
}