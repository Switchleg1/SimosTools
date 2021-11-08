package com.app.simostools

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper

object TempPIDS {
    var list22: Array<PIDStruct?>?  = null
    var list3E: Array<PIDStruct?>?  = null

    fun reset(context: Context?) {
        context?.let {
            list22 = PIDs.list22?.clone()
            list3E = PIDs.list3E?.clone()
        }
    }

    fun clear() {
        list22 = arrayOfNulls(0)
        list3E = arrayOfNulls(0)
    }

    fun save(context: Context?) {
        context?.let {
            //write current PID list
            PIDCSVFile.write(
                context.getString(
                    R.string.filename_pid_csv,
                    UDSLoggingMode.MODE_22.cfgName
                ), context, list22, true
            )
            PIDCSVFile.write(
                context.getString(
                    R.string.filename_pid_csv,
                    UDSLoggingMode.MODE_3E.cfgName
                ), context, list3E, true
            )
        }
    }
}

class SettingsMode22Fragment: SettingsLoggingFragment() {
    override val TAG        = "Settings22"
    override val mMode      = UDSLoggingMode.MODE_22
    override val mLayout    = R.layout.fragment_settings_mode22
}

class SettingsMode3EFragment: SettingsLoggingFragment() {
    override val TAG        = "Settings3E"
    override val mMode      = UDSLoggingMode.MODE_3E
    override val mLayout    = R.layout.fragment_settings_mode3e
}

open class SettingsLoggingFragment : Fragment() {
    open val TAG                                    = "Settings"
    open val mMode                                  = UDSLoggingMode.MODE_22
    open val mLayout                                = R.layout.fragment_settings_mode22
    open val mPIDLayout                             = R.id.recycleViewPID
    private var mPIDAdapter: SettingsViewAdapter?   = null
    private var mPIDLayouts: Array<View?>?          = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(mLayout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        doLoad()
    }

    override fun onPause() {
        super.onPause()

        doSave()
    }

    override fun onStart() {
        super.onStart()

        doLoad()
    }

    private fun doSetColor() {
        view?.let { currentView ->
            //Set background color
            currentView.setBackgroundColor(ColorList.BG_NORMAL.value)
        }
    }

    fun doSave() {
        mPIDAdapter?.let { adapter ->
            for (i in 0 until adapter.itemCount-1) {
                adapter.saveData()
            }
        }

        //Set colors
        doSetColor()
    }

    fun doLoad() {
        view?.let { currentView ->
            // set up the RecyclerView
            val recyclerView: RecyclerView = currentView.findViewById(R.id.recycleViewPID)
            recyclerView.layoutManager = LinearLayoutManager(context)
            mPIDAdapter = when (mMode) {
                UDSLoggingMode.MODE_22 -> SettingsViewAdapter(context, TempPIDS.list22)
                UDSLoggingMode.MODE_3E -> SettingsViewAdapter(context, TempPIDS.list3E)
            }
            mPIDAdapter!!.loadData()
            val callback = ItemMoveCallback(mPIDAdapter!!)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(recyclerView)
            recyclerView.adapter = mPIDAdapter

            //Set colors
            doSetColor()
        }
    }
}