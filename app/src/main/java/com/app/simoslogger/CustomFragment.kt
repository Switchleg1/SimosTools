package com.app.simoslogger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class CustomFragment1 : CustomFragment() {
    override var TAG = "LayoutFragment1"
    override var mCustomName: String = "Layout1"
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
    override var TAG = "LayoutFragment2"
    override var mCustomName: String = "Layout2"
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
    override var TAG = "LayoutFragment3"
    override var mCustomName: String = "Layout3"
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
    override var TAG = "LayoutFragment4"
    override var mCustomName: String = "Layout4"
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