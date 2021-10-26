package com.app.simoslogger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
        checkOrientation()

        //Set packet textview
        mPackCount = view.findViewById(R.id.textViewPackCount)
        mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)

        //Build our list of PIDS in this layout
        PIDs.getList()?.let { list ->
            //get list of all PIDS
            var logList = byteArrayOf()
            for (i in 0 until list.count()) {
                logList += i.toByte()
            }
            mPIDList = logList
        }

        //Build the layout
        buildLayout()

        //Do we keep the screen on?
        view.keepScreenOn = Settings.keepScreenOn

        //update PID text
        updatePIDText()

        //Set background color
        view.setBackgroundColor(ColorList.BG_NORMAL.value)
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

    override fun onGaugeClick(view: View?): Boolean {
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

        return super.onGaugeClick(view)
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

                    //Update progress
                    updateProgress()

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
        }
    }
}