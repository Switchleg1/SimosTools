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


class LoggingFullFragment : LoggingBaseFragment() {
    override var TAG                    = "LoggingFragment"
    override var mFragmentName          = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_full, container, false)
    }

    override fun buildPIDList() {
        super.buildPIDList()

        //Build our list of PIDS in this layout
        PIDs.getList()?.let { list ->
            //get list of all PIDS
            var logList = byteArrayOf()
            for (i in 0 until list.count()) {
                list[i]?.let { pid ->
                    if (pid.enabled) {
                        logList += i.toByte()
                    }
                }
            }
            mPIDList = logList
        }
    }


    override fun onGaugeClick(view: View?): Boolean {
        PIDs.resetData()
        updatePIDText()

        return true
    }
}
