package com.app.vwflashtools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
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
    private var mPackCount: TextView? = null
    private var mPIDText: Array<TextView?> = arrayOfNulls(8)
    private var mPIDProgress: Array<ProgressBar?> = arrayOfNulls(8)
    private var mPIDMultiplier: Array<Float> = arrayOf(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)

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
            if(view.findViewById<Button>(R.id.buttonStartLog).text == getString(R.string.start_log)) {
                val serviceIntent = Intent(context, BTService::class.java)
                serviceIntent.action = BT_DO_CHECK_PID.toString()
                startForegroundService(this.requireContext(), serviceIntent)

                view.findViewById<Button>(R.id.buttonStartLog).text = getString(R.string.stop_log)
            } else {
                val serviceIntent = Intent(context, BTService::class.java)
                serviceIntent.action = BT_DO_STOP_PID.toString()
                startForegroundService(this.requireContext(), serviceIntent)

                view.findViewById<Button>(R.id.buttonStartLog).text = getString(R.string.start_log)
            }
        }

        mPIDText[0] = view.findViewById<TextView>(R.id.textViewPID1)!!
        mPIDText[1] = view.findViewById<TextView>(R.id.textViewPID2)!!
        mPIDText[2] = view.findViewById<TextView>(R.id.textViewPID3)!!
        mPIDText[3] = view.findViewById<TextView>(R.id.textViewPID4)!!
        mPIDText[4] = view.findViewById<TextView>(R.id.textViewPID5)!!
        mPIDText[5] = view.findViewById<TextView>(R.id.textViewPID6)!!
        mPIDText[6] = view.findViewById<TextView>(R.id.textViewPID7)!!
        mPIDText[7] = view.findViewById<TextView>(R.id.textViewPID8)!!
        mPackCount = view.findViewById<TextView>(R.id.textViewPackCount)!!

        mPIDProgress[0] = view.findViewById<ProgressBar>(R.id.progressBar1)!!
        mPIDProgress[1] = view.findViewById<ProgressBar>(R.id.progressBar2)!!
        mPIDProgress[2] = view.findViewById<ProgressBar>(R.id.progressBar3)!!
        mPIDProgress[3] = view.findViewById<ProgressBar>(R.id.progressBar4)!!
        mPIDProgress[4] = view.findViewById<ProgressBar>(R.id.progressBar5)!!
        mPIDProgress[5] = view.findViewById<ProgressBar>(R.id.progressBar6)!!
        mPIDProgress[6] = view.findViewById<ProgressBar>(R.id.progressBar7)!!
        mPIDProgress[7] = view.findViewById<ProgressBar>(R.id.progressBar8)!!

        //Set the UI values
        for(i in 0..7) {
            mPIDText[i]?.text = getString(R.string.textPID, DIDList[i].name, DIDList[i].format.format(DIDList[i].value), DIDList[i].unit)

            //Check for low value PIDS
            if ((DIDList[i].max - DIDList[i].min) < 100.0f) {
                mPIDMultiplier[i] = 100.0f / (DIDList[i].max - DIDList[i].min)
            }

            mPIDProgress[i]?.progress = (DIDList[i].value * mPIDMultiplier[i]).toInt()
            mPIDProgress[i]?.min = (DIDList[i].min * mPIDMultiplier[i]).toInt()
            mPIDProgress[i]?.max = (DIDList[i].max * mPIDMultiplier[i]).toInt()
            mPIDProgress[i]?.progressTintList = ColorStateList.valueOf(Color.GREEN)
        }

        view.setBackgroundColor(Color.rgb(255, 255, 255))
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
                    mPIDText[1]?.text = getString(R.string.textVIN, buff.toString())
                }
                MESSAGE_READ_LOG.toString() -> {
                    val readBuff = intent.getByteArrayExtra("readBuffer") ?: return
                    val readCount = intent.getIntExtra("readCount", 0)
                    //val readTime = intent.getIntExtra("readTime", 0)
                    val readResult = intent.getIntExtra("readResult", UDS_ERROR_NULL)

                    if(readResult != UDS_OK) {
                        mPackCount?.text = readResult.toString()
                        return
                    }

                    UDS22Logger.didList?.let { dList ->
                        if(dList.count() == 0)
                            return

                        //Update UI every 4th tick
                        if(readCount % 4 == 0) {
                            var anyWarning = false

                            //Set the UI values
                            for(i in 0..7) {
                                mPIDProgress[i]?.progress = (DIDList[i].value * mPIDMultiplier[i]!!).toInt()
                                if((DIDList[i].value > DIDList[i].warnMax) or (DIDList[i].value < DIDList[i].warnMin)) {
                                    mPIDProgress[i]?.progressTintList = ColorStateList.valueOf(Color.RED)
                                    anyWarning = true
                                } else {
                                    mPIDProgress[i]?.progressTintList = ColorStateList.valueOf(Color.GREEN)
                                }
                            }

                            if(anyWarning) {
                                view?.setBackgroundColor(Color.rgb(127, 127, 255))
                            } else {
                                view?.setBackgroundColor(Color.rgb(255, 255, 255))
                            }
                        }

                        //Update Log every 2nd tick
                        if(readCount % 2 == 0) {
                            UDS22Logger.didEnable?.let { dEnable ->
                                mPackCount?.text = dEnable.value.toString() + " " + mPackCount?.text
                                if (dEnable.value != 0.0f) {
                                    //If we were not enabled before we must open a log to start writing
                                    if (!mLastEnabled) {
                                        val currentDateTime = LocalDateTime.now()
                                        LogFile.create(
                                            "vwflashtools-${
                                                currentDateTime.format(
                                                    DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss")
                                                )
                                            }.csv", context
                                        )
                                        var strItems: String? = "Time"
                                        for (i in 0 until dList.count()) {
                                            strItems += ",${DIDList[dList[i].toInt()].name}"
                                        }
                                        LogFile.add(strItems)
                                    }
                                    mLastEnabled = true

                                    //Write new values to log
                                    val bleHeader = BLEHeader()
                                    bleHeader.fromByteArray(readBuff)
                                    var strItems: String? = (bleHeader.tickCount.toFloat() / 1000.0f).toString()
                                    for (i in 0 until dList.count()) {
                                        strItems += ",${DIDList[dList[i].toInt()].value}"
                                    }
                                    LogFile.add(strItems)

                                    //Highlight packet count in red since we are logging
                                    mPackCount?.setTextColor(Color.RED)
                                } else {
                                    if (mLastEnabled) {
                                        LogFile.close()
                                    }
                                    mLastEnabled = false

                                    //Not logging set packet count to black
                                    mPackCount?.setTextColor(Color.BLACK)
                                }
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