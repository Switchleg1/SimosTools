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
    private var mPIDText1: TextView? = null
    private var mPIDText2: TextView? = null
    private var mPIDText3: TextView? = null
    private var mPIDText4: TextView? = null
    private var mPIDText5: TextView? = null
    private var mPIDText6: TextView? = null
    private var mPIDText7: TextView? = null
    private var mPIDText8: TextView? = null
    private var mPackCount: TextView? = null
    private var mPIDProg1: ProgressBar? = null
    private var mPIDProg2: ProgressBar? = null
    private var mPIDProg3: ProgressBar? = null
    private var mPIDProg4: ProgressBar? = null
    private var mPIDProg5: ProgressBar? = null
    private var mPIDProg6: ProgressBar? = null
    private var mPIDProg7: ProgressBar? = null
    private var mPIDProg8: ProgressBar? = null

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

        mPIDText1 = view.findViewById<TextView>(R.id.textViewPID1)!!
        mPIDText2 = view.findViewById<TextView>(R.id.textViewPID2)!!
        mPIDText3 = view.findViewById<TextView>(R.id.textViewPID3)!!
        mPIDText4 = view.findViewById<TextView>(R.id.textViewPID4)!!
        mPIDText5 = view.findViewById<TextView>(R.id.textViewPID5)!!
        mPIDText6 = view.findViewById<TextView>(R.id.textViewPID6)!!
        mPIDText7 = view.findViewById<TextView>(R.id.textViewPID7)!!
        mPIDText8 = view.findViewById<TextView>(R.id.textViewPID8)!!
        mPackCount = view.findViewById<TextView>(R.id.textViewPackCount)!!

        mPIDProg1 = view.findViewById<ProgressBar>(R.id.progressBar1)!!
        mPIDProg2 = view.findViewById<ProgressBar>(R.id.progressBar2)!!
        mPIDProg3 = view.findViewById<ProgressBar>(R.id.progressBar3)!!
        mPIDProg4 = view.findViewById<ProgressBar>(R.id.progressBar4)!!
        mPIDProg5 = view.findViewById<ProgressBar>(R.id.progressBar5)!!
        mPIDProg6 = view.findViewById<ProgressBar>(R.id.progressBar6)!!
        mPIDProg7 = view.findViewById<ProgressBar>(R.id.progressBar7)!!
        mPIDProg8 = view.findViewById<ProgressBar>(R.id.progressBar8)!!

        mPIDText1?.text = getString(R.string.textPID, DIDList[0].name, DIDList[0].format.format(DIDList[0].value), DIDList[0].unit)
        mPIDProg1?.progress = 0
        mPIDProg1?.max = DIDList[0].max.toInt()
        mPIDProg1?.min = DIDList[0].min.toInt()
        mPIDText2?.text = getString(R.string.textPID, DIDList[1].name, DIDList[1].format.format(DIDList[1].value), DIDList[1].unit)
        mPIDProg2?.progress = 0
        mPIDProg2?.max = DIDList[1].max.toInt()
        mPIDProg2?.min = DIDList[1].min.toInt()
        mPIDText3?.text = getString(R.string.textPID, DIDList[2].name, DIDList[2].format.format(DIDList[2].value), DIDList[2].unit)
        mPIDProg3?.progress = 0
        mPIDProg3?.max = DIDList[2].max.toInt()
        mPIDProg3?.min = DIDList[2].min.toInt()
        mPIDText4?.text = getString(R.string.textPID, DIDList[3].name, DIDList[3].format.format(DIDList[3].value), DIDList[3].unit)
        mPIDProg4?.progress = 0
        mPIDProg4?.max = DIDList[3].max.toInt()
        mPIDProg4?.min = DIDList[3].min.toInt()
        mPIDText5?.text = getString(R.string.textPID, DIDList[4].name, DIDList[4].format.format(DIDList[4].value), DIDList[4].unit)
        mPIDProg5?.progress = 0
        mPIDProg5?.max = DIDList[4].max.toInt()
        mPIDProg5?.min = DIDList[4].min.toInt()
        mPIDText6?.text = getString(R.string.textPID, DIDList[5].name, DIDList[5].format.format(DIDList[5].value), DIDList[5].unit)
        mPIDProg6?.progress = 0
        mPIDProg6?.max = DIDList[5].max.toInt()
        mPIDProg6?.min = DIDList[5].min.toInt()
        mPIDText7?.text = getString(R.string.textPID, DIDList[6].name, DIDList[6].format.format(DIDList[6].value), DIDList[6].unit)
        mPIDProg7?.progress = 0
        mPIDProg7?.max = DIDList[6].max.toInt()
        mPIDProg7?.min = DIDList[6].min.toInt()
        mPIDText8?.text = getString(R.string.textPID, DIDList[7].name, DIDList[7].format.format(DIDList[7].value), DIDList[7].unit)
        mPIDProg8?.progress = 0
        mPIDProg8?.max = DIDList[7].max.toInt()
        mPIDProg8?.min = DIDList[7].min.toInt()
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
                    mPIDText1?.text = getString(R.string.textVIN, buff.toString())
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
                            if (dList.count() >= 1) {
                                mPIDText1?.text = getString(R.string.textPID, DIDList[dList[0].toInt()].name, DIDList[dList[0].toInt()].format.format(DIDList[dList[0].toInt()].value), DIDList[dList[0].toInt()].unit)
                                mPIDProg1?.progress = DIDList[dList[0].toInt()].value.toInt()
                            }
                            if (dList.count() >= 2) {
                                mPIDText2?.text = getString(R.string.textPID, DIDList[dList[1].toInt()].name, DIDList[dList[1].toInt()].format.format(DIDList[dList[1].toInt()].value), DIDList[dList[1].toInt()].unit)
                                mPIDProg2?.progress = DIDList[dList[1].toInt()].value.toInt()
                            }
                            if (dList.count() >= 3) {
                                mPIDText3?.text = getString(R.string.textPID, DIDList[dList[2].toInt()].name, DIDList[dList[2].toInt()].format.format(DIDList[dList[2].toInt()].value), DIDList[dList[2].toInt()].unit)
                                mPIDProg3?.progress = DIDList[dList[2].toInt()].value.toInt()
                            }
                            if (dList.count() >= 4) {
                                mPIDText4?.text = getString(R.string.textPID, DIDList[dList[3].toInt()].name, DIDList[dList[3].toInt()].format.format(DIDList[dList[3].toInt()].value), DIDList[dList[3].toInt()].unit)
                                mPIDProg4?.progress = DIDList[dList[3].toInt()].value.toInt()
                            }
                            if (dList.count() >= 5) {
                                mPIDText5?.text = getString(R.string.textPID, DIDList[dList[4].toInt()].name, DIDList[dList[4].toInt()].format.format(DIDList[dList[4].toInt()].value), DIDList[dList[4].toInt()].unit)
                                mPIDProg5?.progress = DIDList[dList[4].toInt()].value.toInt()
                            }
                            if (dList.count() >= 6) {
                                mPIDText6?.text = getString(R.string.textPID, DIDList[dList[5].toInt()].name, DIDList[dList[5].toInt()].format.format(DIDList[dList[5].toInt()].value), DIDList[dList[5].toInt()].unit)
                                mPIDProg6?.progress = DIDList[dList[5].toInt()].value.toInt()
                            }
                            if (dList.count() >= 7) {
                                mPIDText7?.text = getString(R.string.textPID, DIDList[dList[6].toInt()].name, DIDList[dList[6].toInt()].format.format(DIDList[dList[6].toInt()].value), DIDList[dList[6].toInt()].unit)
                                mPIDProg7?.progress = DIDList[dList[6].toInt()].value.toInt()
                            }
                            if (dList.count() >= 8) {
                                mPIDText8?.text = getString(R.string.textPID, DIDList[dList[7].toInt()].name, DIDList[dList[7].toInt()].format.format(DIDList[dList[7].toInt()].value), DIDList[dList[7].toInt()].unit)
                                mPIDProg8?.progress = DIDList[dList[7].toInt()].value.toInt()
                            }
                        }

                        //Write packet count
                        mPackCount?.text = readCount.toString()

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