package com.app.vwflashtools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class LoggingFragment : Fragment() {
    private var mLastEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_back).setOnClickListener {
            findNavController().navigate(R.id.action_LoggingFragment_to_FirstFragment)
        }

        view.findViewById<Button>(R.id.buttonStartLog).setOnClickListener {
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_DO_CHECK_PID.toString()
            startForegroundService(this.requireContext(), serviceIntent)
        }

        view.findViewById<Button>(R.id.buttonStopLog).setOnClickListener {
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_DO_STOP_PID.toString()
            startForegroundService(this.requireContext(), serviceIntent)
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(MESSAGE_READ_LOG.toString())
        filter.addAction(MESSAGE_TOAST.toString())
        this.activity?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        this.activity?.unregisterReceiver(mBroadcastReceiver)
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                MESSAGE_READ_VIN.toString() -> {
                    val buff = intent.getByteArrayExtra("readBuffer")
                    view?.findViewById<TextView>(R.id.textViewPID1)!!.text = getString(R.string.textVIN, buff.toString())
                }
                MESSAGE_READ_LOG.toString() -> {
                    //val readBuff = intent.getByteArrayExtra("readBuffer") ?: return
                    val readCount = intent.getIntExtra("readCount", 0)
                    //val readTime = intent.getIntExtra("readTime", 0)
                    val readResult = intent.getIntExtra("readResult", UDS_ERROR_NULL)

                    val textViewPackCount = view?.findViewById<TextView>(R.id.textViewPackCount)!!
                    textViewPackCount.text = readResult.toString()

                    if(readResult != UDS_OK)
                        return

                    UDS22Logger.didList?.let { dList ->
                        if(dList.count() == 0)
                            return

                        if(dList.count() >= 1) {
                            view?.findViewById<TextView>(R.id.textViewPID1)!!.text = getString(R.string.textPID, DIDList[dList[0].toInt()].name, DIDList[dList[0].toInt()].value, DIDList[dList[0].toInt()].unit)
                            view?.findViewById<ProgressBar>(R.id.progressBar1)!!.progress = DIDList[dList[0].toInt()].value.toInt()
                        }
                        if(dList.count() >= 2) {
                            view?.findViewById<TextView>(R.id.textViewPID2)!!.text = getString(R.string.textPID, DIDList[dList[1].toInt()].name, DIDList[dList[1].toInt()].value, DIDList[dList[1].toInt()].unit)
                            view?.findViewById<ProgressBar>(R.id.progressBar2)!!.progress = DIDList[dList[1].toInt()].value.toInt()
                        }
                        if(dList.count() >= 3) {
                            view?.findViewById<TextView>(R.id.textViewPID3)!!.text = getString(R.string.textPID, DIDList[dList[2].toInt()].name, DIDList[dList[2].toInt()].value, DIDList[dList[2].toInt()].unit)
                            view?.findViewById<ProgressBar>(R.id.progressBar3)!!.progress = DIDList[dList[2].toInt()].value.toInt()
                        }
                        if(dList.count() >= 4) {
                            view?.findViewById<TextView>(R.id.textViewPID4)!!.text = getString(R.string.textPID, DIDList[dList[3].toInt()].name, DIDList[dList[3].toInt()].value, DIDList[dList[3].toInt()].unit)
                            view?.findViewById<ProgressBar>(R.id.progressBar4)!!.progress = DIDList[dList[3].toInt()].value.toInt()
                        }
                        if(dList.count() >= 5) {
                            view?.findViewById<TextView>(R.id.textViewPID5)!!.text = getString(R.string.textPID, DIDList[dList[4].toInt()].name, DIDList[dList[4].toInt()].value, DIDList[dList[4].toInt()].unit)
                            view?.findViewById<ProgressBar>(R.id.progressBar5)!!.progress = DIDList[dList[4].toInt()].value.toInt()
                        }
                        if(dList.count() >= 6) {
                            view?.findViewById<TextView>(R.id.textViewPID6)!!.text = getString(R.string.textPID, DIDList[dList[5].toInt()].name, DIDList[dList[5].toInt()].value, DIDList[dList[5].toInt()].unit)
                            view?.findViewById<ProgressBar>(R.id.progressBar6)!!.progress = DIDList[dList[5].toInt()].value.toInt()
                        }
                        if(dList.count() >= 7) {
                            view?.findViewById<TextView>(R.id.textViewPID7)!!.text = getString(R.string.textPID, DIDList[dList[6].toInt()].name, DIDList[dList[6].toInt()].value, DIDList[dList[6].toInt()].unit)
                            view?.findViewById<ProgressBar>(R.id.progressBar7)!!.progress = DIDList[dList[6].toInt()].value.toInt()
                        }
                        if(dList.count() >= 8) {
                            view?.findViewById<TextView>(R.id.textViewPID8)!!.text = getString(R.string.textPID, DIDList[dList[7].toInt()].name, DIDList[dList[7].toInt()].value, DIDList[dList[7].toInt()].unit)
                            view?.findViewById<ProgressBar>(R.id.progressBar8)!!.progress = DIDList[dList[7].toInt()].value.toInt()
                        }

                        //Write packet count
                        textViewPackCount.text = readCount.toString()

                        UDS22Logger.didEnable?.let { dEnable ->
                            if(dEnable.value != 0f) {
                                //If we were not enabled before we must open a log to start writing
                                if(!mLastEnabled) {
                                    val currentDateTime = LocalDateTime.now()
                                    LogFile.create("vwflashtools-${currentDateTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss"))}.csv", context)
                                    var strItems: String? = DIDList[dList[0].toInt()].name
                                    for(i in 1 until dList.count()) {
                                        strItems += ",${DIDList[dList[i].toInt()].name}"
                                    }
                                    LogFile.add(strItems)
                                }
                                mLastEnabled = true

                                //Write new values to log
                                var strItems: String? = "${DIDList[dList[0].toInt()].value}"
                                for(i in 1 until dList.count()) {
                                    strItems += ",${DIDList[dList[i].toInt()].value}"
                                }
                                LogFile.add(strItems)

                                //Highlight packet count in red since we are logging
                                textViewPackCount.setTextColor(Color.RED)
                            } else {
                                if(mLastEnabled) {
                                    LogFile.close()
                                }
                                mLastEnabled = false

                                //Not logging set packet count to black
                                textViewPackCount.setTextColor(Color.BLACK)
                            }
                        }
                    }
                }
                MESSAGE_TOAST.toString() -> {
                    val nToast = intent.getStringExtra("newToast")
                    Toast.makeText(activity, nToast, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}