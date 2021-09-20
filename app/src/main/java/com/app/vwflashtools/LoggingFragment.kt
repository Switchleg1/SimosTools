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
    private var mPackCount: TextView? = null
    private var mPIDText: Array<TextView?> = arrayOfNulls(8)
    private var mPIDProgress: Array<ProgressBar?> = arrayOfNulls(8)
    private var mPIDMultiplier: Array<Float> = arrayOf(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)
    private val mColorWarn = Color.rgb(127, 127, 255)
    private val mColorNormal = Color.rgb(255, 255, 255)
    private var mLastColor: BooleanArray = booleanArrayOf(false, false, false, false, false, false, false, false)
    private var mLastWarning = false
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

        //Set background color
        view.setBackgroundColor(mColorNormal)
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
                    //val readBuff = intent.getByteArrayExtra("readBuffer") ?: return
                    val readCount = intent.getIntExtra("readCount", 0)
                    val readTime = intent.getIntExtra("readTime", 0)
                    val readResult = intent.getIntExtra("readResult", UDS_ERROR_NULL)

                    if(readResult != UDS_OK) {
                        mPackCount?.text = readResult.toString()
                        return
                    }

                    //Update UI every 4th tick
                    if(readCount % 4 == 0) {
                        var anyWarning = false

                        //Set the UI values
                        for(i in 0..7) {
                            //Update text
                            mPIDText[i]?.text = getString(R.string.textPID, DIDList[i].name, DIDList[i].format.format(DIDList[i].value), DIDList[i].unit)

                            //Update progress is the value is different
                            val newProgress = (DIDList[i].value * mPIDMultiplier[i]).toInt()
                            if(newProgress != mPIDProgress[i]?.progress) {
                                mPIDProgress[i]?.progress = newProgress
                            }

                            //Check to see if we should be warning user
                            if((DIDList[i].value > DIDList[i].warnMax) or (DIDList[i].value < DIDList[i].warnMin)) {

                                if(!mLastColor[i]) {
                                    mPIDProgress[i]?.progressTintList = ColorStateList.valueOf(Color.RED)
                                }

                                mLastColor[i] = true
                                anyWarning = true
                            } else {
                                if(mLastColor[i]) {
                                    mPIDProgress[i]?.progressTintList = ColorStateList.valueOf(Color.GREEN)
                                }

                                mLastColor[i] = false
                            }
                        }

                        //If any visible PIDS are in warning state set background color to warn
                        if(anyWarning) {
                            if(!mLastWarning) {
                                view?.setBackgroundColor(mColorWarn)
                            }

                            mLastWarning = true
                        } else {
                            if(mLastWarning) {
                                view?.setBackgroundColor(mColorNormal)
                            }

                            mLastWarning = false
                        }

                        //Update fps
                        val dEnable = DIDList[DIDList.count()-1]
                        val fps = readCount.toFloat() / (readTime.toFloat() / 1000.0f)
                        mPackCount?.text = "${fps}fps"
                        if (dEnable.value != 0.0f) {
                            //Highlight packet count in red since we are logging
                            if(!mLastEnabled) {
                                mPackCount?.setTextColor(Color.RED)
                            }
                            mLastEnabled = true
                        } else {
                            //Not logging set packet count to black
                            if(mLastEnabled) {
                                mPackCount?.setTextColor(Color.BLACK)
                            }
                            mLastEnabled = false
                        }
                    }
                }
            }
        }
    }
}