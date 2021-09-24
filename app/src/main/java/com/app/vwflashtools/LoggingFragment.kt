package com.app.vwflashtools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.IOException
import android.R.attr.name
import android.content.res.Configuration


data class DATAStruct(var text: TextView?,
                        var progress: ProgressBar?,
                        var min: Float,
                        var max: Float,
                        var lastColor: Boolean,
                        var multiplier: Float)

class LoggingFragment : Fragment() {
    private var TAG = "LoggingFragment"
    private var mDisplayMode = DISPLAY_BARS
    private var mPackCount: TextView? = null
    private val mColorWarn = Color.rgb(127, 127, 255)
    private val mColorNormal = Color.rgb(255, 255, 255)
    private var mLastWarning = false
    private var mLastEnabled = false
    private var mGraph: SwitchGraph? = null
    private var mDataList: List<DATAStruct> = listOf(DATAStruct(null, null, 0f, 0f, false, 0f),
                                                    DATAStruct(null, null, 0f, 0f, false, 0f),
                                                    DATAStruct(null, null, 0f, 0f, false, 0f),
                                                    DATAStruct(null, null, 0f, 0f, false, 0f),
                                                    DATAStruct(null, null, 0f, 0f, false, 0f),
                                                    DATAStruct(null, null, 0f, 0f, false, 0f),
                                                    DATAStruct(null, null, 0f, 0f, false, 0f),
                                                    DATAStruct(null, null, 0f, 0f, false, 0f))


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
            if(view.findViewById<Button>(R.id.buttonStartLog).text == getString(R.string.button_start)) {
                val serviceIntent = Intent(context, BTService::class.java)
                serviceIntent.action = BT_DO_CHECK_PID.toString()
                startForegroundService(this.requireContext(), serviceIntent)

                view.findViewById<Button>(R.id.buttonStartLog).text = getString(R.string.button_stop)
            } else {
                val serviceIntent = Intent(context, BTService::class.java)
                serviceIntent.action = BT_DO_STOP_PID.toString()
                startForegroundService(this.requireContext(), serviceIntent)

                view.findViewById<Button>(R.id.buttonStartLog).text = getString(R.string.button_start)
            }
        }

        view.findViewById<Button>(R.id.buttonReset).setOnClickListener {
            for(i in 0.. 7) {
                val data = mDataList[i]
                val did = DIDs.list()[i]

                data.max = 0f
                data.min = 0f

                //Update text
                val currentOrientation = resources.configuration.orientation
                if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    data.text?.text = getString(R.string.textPIDL, did.name, did.format.format(did.value), did.unit, did.format.format(data.min), did.format.format(data.max))
                } else {
                    // Portrait
                    data.text?.text = getString(R.string.textPIDP, did.name, did.format.format(did.value), did.unit, did.format.format(data.min), did.format.format(data.max))
                }
            }
        }

        //mDisplayMode = DISPLAY_GRAPH
        mDataList[0].text = view.findViewById<TextView>(R.id.textViewPID1)
        mDataList[1].text = view.findViewById<TextView>(R.id.textViewPID2)
        mDataList[2].text = view.findViewById<TextView>(R.id.textViewPID3)
        mDataList[3].text = view.findViewById<TextView>(R.id.textViewPID4)
        mDataList[4].text = view.findViewById<TextView>(R.id.textViewPID5)
        mDataList[5].text = view.findViewById<TextView>(R.id.textViewPID6)
        mDataList[6].text = view.findViewById<TextView>(R.id.textViewPID7)
        mDataList[7].text = view.findViewById<TextView>(R.id.textViewPID8)
        mPackCount = view.findViewById<TextView>(R.id.textViewPackCount)

        mDataList[0].progress = view.findViewById<ProgressBar>(R.id.progressBar1)
        mDataList[1].progress = view.findViewById<ProgressBar>(R.id.progressBar2)
        mDataList[2].progress = view.findViewById<ProgressBar>(R.id.progressBar3)
        mDataList[3].progress = view.findViewById<ProgressBar>(R.id.progressBar4)
        mDataList[4].progress = view.findViewById<ProgressBar>(R.id.progressBar5)
        mDataList[5].progress = view.findViewById<ProgressBar>(R.id.progressBar6)
        mDataList[6].progress = view.findViewById<ProgressBar>(R.id.progressBar7)
        mDataList[7].progress = view.findViewById<ProgressBar>(R.id.progressBar8)

        when(mDisplayMode) {
            DISPLAY_BARS -> {

            }
            DISPLAY_GRAPH -> {
                for(i in 0..7) {
                    mDataList[i].progress?.visibility = View.INVISIBLE
                }

                try {
                    val lay = view.findViewById<ConstraintLayout>(R.id.LoggingLayout)!!
                    mGraph = SwitchGraph(view.context)
                    mGraph!!.layout(0, 0, lay.maxWidth, lay.maxHeight)
                    mGraph!!.contentDescription = getString(R.string.app_name)
                    mGraph!!.setImageDrawable(getDrawable(requireContext(), R.drawable.graph))
                    lay.addView(mGraph)

                    for(i in 0 until 8) {
                        mGraph!!.setColor(i, Color.rgb(i*20.0f, i*2.0f+100f, 150f-i*10f))
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Exception when trying to setup graphview",e)
                    return
                }
            }
        }

        //Set the UI values
        for(i in 0..7) {
            //get the current did
            val did = DIDs.list()[i]
            val data = mDataList[i]

            //set min/max
            if(did.value > data.max)
                data.max = did.value

            if(did.value < data.min)
                data.min = did.value

            //Update text
            val currentOrientation = resources.configuration.orientation
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                data.text?.text = getString(R.string.textPIDL, did.name, did.format.format(did.value), did.unit, did.format.format(data.min), did.format.format(data.max))
            } else {
                // Portrait
                data.text?.text = getString(R.string.textPIDP, did.name, did.format.format(did.value), did.unit, did.format.format(data.min), did.format.format(data.max))
            }

            //Check for low value PIDS
            if((did.progMax - did.progMin) < 100.0f) {
                data.multiplier = 100.0f / (did.progMax - did.progMin)
            } else {
                data.multiplier = 1.0f
            }

            //Update the progress bar
            data.progress?.progress = (did.value * data.multiplier).toInt()
            data.progress?.min = (did.progMin * data.multiplier).toInt()
            data.progress?.max = (did.progMax * data.multiplier).toInt()
            data.progress?.progressTintList = ColorStateList.valueOf(Color.GREEN)
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
                    mDataList[1].text?.text = getString(R.string.textVIN, buff.toString())
                }
                MESSAGE_READ_LOG.toString() -> {
                    //val readBuff = intent.getByteArrayExtra("readBuffer") ?: return
                    val readCount = intent.getIntExtra("readCount", 0)
                    val readTime = intent.getLongExtra("readTime", 0)
                    val readResult = intent.getIntExtra("readResult", UDS_ERROR_NULL)

                    //Make sure we received an ok
                    if(readResult != UDS_OK) {
                        mPackCount?.text = readResult.toString()
                        return
                    }

                    //Set the UI values
                    var anyWarning = false
                    for(i in 0..7) {
                        //get the current did
                        val did = DIDs.list()[i]
                        val data = mDataList[i]

                        //set min/max
                        if(did.value > data.max)
                            data.max = did.value

                        if(did.value < data.min)
                            data.min = did.value

                        //Update text
                        val currentOrientation = resources.configuration.orientation
                        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                            data.text?.text = getString(R.string.textPIDL, did.name, did.format.format(did.value), did.unit, did.format.format(data.min), did.format.format(data.max))
                        } else {
                            // Portrait
                            data.text?.text = getString(R.string.textPIDP, did.name, did.format.format(did.value), did.unit, did.format.format(data.min), did.format.format(data.max))
                        }

                        //Update progress is the value is different
                        val newProgress = (did.value * data.multiplier).toInt()
                        if(newProgress != data.progress?.progress) {
                            data.progress?.progress = newProgress
                        }

                        //Check to see if we should be warning user
                        if((did.value > did.warnMax) or (did.value < did.warnMin)) {

                            if(!data.lastColor) {
                                data.progress?.progressTintList = ColorStateList.valueOf(Color.RED)
                            }

                            data.lastColor = true
                            anyWarning = true
                        } else {
                            if(data.lastColor) {
                                data.progress?.progressTintList = ColorStateList.valueOf(Color.GREEN)
                            }

                            data.lastColor = false
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
                    val fps = readCount.toFloat() / (readTime.toFloat() / 1000.0f)
                    val dEnable = DIDs.list()[DIDs.list().count()-1]
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