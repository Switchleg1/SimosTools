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
import android.content.res.Configuration
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel


data class DATAStruct(var min: Float,
                        var max: Float,
                        var lastColor: Boolean,
                        var multiplier: Float)

class LoggingViewModel : ViewModel() {
    var displayMode = DISPLAY_BARS
    val colorWarn = Color.rgb(127, 127, 255)
    val colorNormal = Color.rgb(255, 255, 255)
    var lastWarning = false
    var lastEnabled = false
    var dataList: List<DATAStruct> = listOf(DATAStruct(0f, 0f, false, 0f),
                                            DATAStruct(0f, 0f, false, 0f),
                                            DATAStruct(0f, 0f, false, 0f),
                                            DATAStruct(0f, 0f, false, 0f),
                                            DATAStruct(0f, 0f, false, 0f),
                                            DATAStruct(0f, 0f, false, 0f),
                                            DATAStruct(0f, 0f, false, 0f),
                                            DATAStruct(0f, 0f, false, 0f))
}

class LoggingFragment : Fragment() {
    private var TAG = "LoggingFragment"
    private var mGraph: SwitchGraph? = null
    private var mPackCount: TextView? = null
    private var mTextView: Array<TextView?> = arrayOf(null, null, null, null, null, null, null, null)
    private var mProgressBar: Array<ProgressBar?> = arrayOf(null, null, null, null, null, null, null, null)


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
                val mViewModel: LoggingViewModel by viewModels()
                val data = mViewModel.dataList[i]
                val did = DIDs.list()[i]

                data.max = did.value
                data.min = did.value
            }
            updatePIDText()
        }

        //mDisplayMode = DISPLAY_GRAPH
        mTextView[0] = view.findViewById(R.id.textViewPID1)
        mTextView[1] = view.findViewById(R.id.textViewPID2)
        mTextView[2] = view.findViewById(R.id.textViewPID3)
        mTextView[3] = view.findViewById(R.id.textViewPID4)
        mTextView[4] = view.findViewById(R.id.textViewPID5)
        mTextView[5] = view.findViewById(R.id.textViewPID6)
        mTextView[6] = view.findViewById(R.id.textViewPID7)
        mTextView[7] = view.findViewById(R.id.textViewPID8)
        mPackCount = view.findViewById(R.id.textViewPackCount)

        mProgressBar[0] = view.findViewById(R.id.progressBar1)
        mProgressBar[1] = view.findViewById(R.id.progressBar2)
        mProgressBar[2] = view.findViewById(R.id.progressBar3)
        mProgressBar[3] = view.findViewById(R.id.progressBar4)
        mProgressBar[4] = view.findViewById(R.id.progressBar5)
        mProgressBar[5] = view.findViewById(R.id.progressBar6)
        mProgressBar[6] = view.findViewById(R.id.progressBar7)
        mProgressBar[7] = view.findViewById(R.id.progressBar8)

        val mViewModel: LoggingViewModel by viewModels()
        when(mViewModel.displayMode) {
            DISPLAY_BARS -> {

            }
            DISPLAY_GRAPH -> {
                for(i in 0..7) {
                    mProgressBar[i]?.visibility = View.INVISIBLE
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
            val data = mViewModel.dataList[i]
            val progressBar = mProgressBar[i]

            //set min/max
            if(did.value > data.max)
                data.max = did.value

            if(did.value < data.min)
                data.min = did.value

            //Check for low value PIDS
            if((did.progMax - did.progMin) < 100.0f) {
                data.multiplier = 100.0f / (did.progMax - did.progMin)
            } else {
                data.multiplier = 1.0f
            }

            //Setup the progress bar
            progressBar?.min = (did.progMin * data.multiplier).toInt()
            progressBar?.max = (did.progMax * data.multiplier).toInt()
            progressBar?.progress = (did.value * data.multiplier).toInt()
            progressBar?.progressTintList = ColorStateList.valueOf(Color.GREEN)
        }

        //update PID text
        updatePIDText()

        //Set background color
        view.setBackgroundColor(mViewModel.colorNormal)
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

    private fun updatePIDText() {
        //check orientation
        var textVal = R.string.textPIDP
        val currentOrientation = resources.configuration.orientation
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            textVal = R.string.textPIDL
        }

        //Update text
        val mViewModel: LoggingViewModel by viewModels()
        for(i in 0..7) {
            val did = DIDs.list()[i]
            val textView = mTextView[i]
            val data = mViewModel.dataList[i]

            textView?.text = getString(textVal, did.name, did.format.format(did.value), did.unit, did.format.format(data.min), did.format.format(data.max))
        }
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                MESSAGE_READ_VIN.toString() -> {
                    val buff = intent.getByteArrayExtra("readBuffer")
                    mPackCount?.text = getString(R.string.textVIN, buff.toString())
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

                    //Update PID Text
                    updatePIDText()

                    //Set the UI values
                    val mViewModel: LoggingViewModel by viewModels()
                    var anyWarning = false
                    for(i in 0..7) {
                        //get the current did
                        val did = DIDs.list()[i]
                        val data = mViewModel.dataList[i]
                        val progressBar = mProgressBar[i]

                        //set min/max
                        if(did.value > data.max)
                            data.max = did.value

                        if(did.value < data.min)
                            data.min = did.value


                        //Update progress is the value is different
                        val newProgress = (did.value * data.multiplier).toInt()
                        if(newProgress != progressBar?.progress) {
                            progressBar?.progress = newProgress
                        }

                        //Check to see if we should be warning user
                        if((did.value > did.warnMax) or (did.value < did.warnMin)) {

                            if(!data.lastColor) {
                                progressBar?.progressTintList = ColorStateList.valueOf(Color.RED)
                            }

                            data.lastColor = true
                            anyWarning = true
                        } else {
                            if(data.lastColor) {
                                progressBar?.progressTintList = ColorStateList.valueOf(Color.GREEN)
                            }

                            data.lastColor = false
                        }
                    }

                    //If any visible PIDS are in warning state set background color to warn
                    if(anyWarning) {
                        if(!mViewModel.lastWarning) {
                            view?.setBackgroundColor(mViewModel.colorWarn)
                        }

                        mViewModel.lastWarning = true
                    } else {
                        if(mViewModel.lastWarning) {
                            view?.setBackgroundColor(mViewModel.colorNormal)
                        }

                        mViewModel.lastWarning = false
                    }

                    //Update fps
                    val fps = readCount.toFloat() / (readTime.toFloat() / 1000.0f)
                    val dEnable = DIDs.list()[DIDs.list().count()-1]
                    mPackCount?.text = "${fps}fps"
                    if (dEnable.value != 0.0f) {
                        //Highlight packet count in red since we are logging
                        if(!mViewModel.lastEnabled) {
                            mPackCount?.setTextColor(Color.RED)
                        }
                        mViewModel.lastEnabled = true
                    } else {
                        //Not logging set packet count to black
                        if(mViewModel.lastEnabled) {
                            mPackCount?.setTextColor(Color.BLACK)
                        }
                        mViewModel.lastEnabled = false
                    }
                }
            }
        }
    }
}