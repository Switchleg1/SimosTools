package com.app.simostools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class LoggingCustomFragment1 : LoggingCustomFragment() {
    override var TAG                    = "LoggingCustomFragment1"
    override var mFragmentName          = "Layout1"
    override var mCustomName: String    = "Layout1"
    override var mLayoutName: Int   = R.id.CustomLayoutScroll1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_custom1, container, false)
    }
}

class LoggingCustomFragment2 : LoggingCustomFragment() {
    override var TAG                    = "LoggingCustomFragment2"
    override var mFragmentName          = "Layout2"
    override var mCustomName: String    = "Layout2"
    override var mLayoutName: Int       = R.id.CustomLayoutScroll2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_custom2, container, false)
    }
}

class LoggingCustomFragment3 : LoggingCustomFragment() {
    override var TAG                    = "LoggingCustomFragment3"
    override var mFragmentName          = "Layout3"
    override var mCustomName: String    = "Layout3"
    override var mLayoutName: Int       = R.id.CustomLayoutScroll3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_custom3, container, false)
    }
}

class LoggingCustomFragment4 : LoggingCustomFragment() {
    override var TAG                    = "LoggingCustomFragment4"
    override var mFragmentName          = "Layout4"
    override var mCustomName: String    = "Layout4"
    override var mLayoutName: Int       = R.id.CustomLayoutScroll4

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_custom4, container, false)
    }
}

open class LoggingCustomFragment : LoggingBaseFragment() {
    open var mCustomName: String = "Layout1"

    override fun onGaugeClick(view: View?): Boolean {
        PIDs.resetData()
        updatePIDText()

        return true
    }

    override fun buildPIDList() {
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
    }
}