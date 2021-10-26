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

class CustomFragment1 : CustomFragment() {
    override var TAG = "CustomFragment1"
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
    override var TAG = "CustomFragment2"
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
    override var TAG = "CustomFragment3"
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
    override var TAG = "CustomFragment4"
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

open class CustomFragment : BaseLoggingFragment() {
    open var mCustomName: String = "Custom1"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //check orientation and type
        checkOrientation()

        //Build our list of PIDS in this layout
        PIDs.getList()?.let { list ->
            //get list of custom PIDS
            var customList = byteArrayOf()
            for (i in 0 until list.count()) {
                val pid = list[i]!!
                if (pid.enabled && pid.tabs.contains(mCustomName)) {
                    customList += i.toByte()
                }
            }
            mPIDList = customList
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
        PIDs.resetData()
        updatePIDText()

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
                    if(readResult != UDSReturn.OK) {
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
                }
            }
        }
    }
}