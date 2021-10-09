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
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.content.res.Configuration
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel

data class DATAStruct(var min: Float,
                        var max: Float,
                        var lastColor: Boolean,
                        var multiplier: Float)

class LoggingViewModel : ViewModel() {
    var lastWarning = false
    var lastEnabled = false
    var dataList: Array<DATAStruct?>? = null
}

class LoggingFragment : Fragment() {
    private var TAG = "LoggingFragment"
    private var mPackCount: TextView? = null
    private var mPIDS: Array<View?>? = null

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

        view.findViewById<Button>(R.id.buttonReset).setOnClickListener {
            val mViewModel: LoggingViewModel by viewModels()
            for(i in 0 until mViewModel.dataList!!.count()) {
                val data = mViewModel.dataList!![i]!!
                val did = DIDs.list()[i]

                data.max = did.value
                data.min = did.value
            }
            updatePIDText()
        }

        //get view model
        val mViewModel: LoggingViewModel by viewModels()

        //check orientation
        var textVal = R.string.textPIDP
        var layoutType = R.layout.pid_portrait
        var currentOrientation = resources.configuration.orientation

        if(Settings.alwaysPortrait)
            currentOrientation = Configuration.ORIENTATION_PORTRAIT

        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            textVal = R.string.textPIDL
            layoutType = R.layout.pid_land
        }

        //Set packet textview
        mPackCount = view.findViewById<TextView>(R.id.textViewPackCount)

        //Build layout
        if(mViewModel.dataList == null || mViewModel.dataList?.count() != DIDs.list().count()) {
            mViewModel.dataList = arrayOfNulls(DIDs.list().count())
        }
        mPIDS = arrayOfNulls(DIDs.list().count())
        for(i in 0 until DIDs.list().count()) {
            //build child layout
            val pidLayout = layoutInflater.inflate(layoutType, null)
            mPIDS!![i] = pidLayout

            //build new data structure
            val data = DATAStruct(0.0f, 0.0f, false, 1.0f)
            mViewModel.dataList!![i] = data

            //get current did
            val did = DIDs.list()[i]

            //Check for low value PIDS
            if((did.progMax - did.progMin) < 100.0f) {
                data.multiplier = 100.0f / (did.progMax - did.progMin)
            } else {
                data.multiplier = 1.0f
            }

            //find text view and set text
            val textView = pidLayout.findViewById<TextView>(R.id.pid_land_text)
            textView.text = getString(textVal, did.name, did.format.format(did.value), did.unit, did.format.format(data.min), did.format.format(data.max))
            textView.textSize = 18 * Settings.displaySize

            //Setup the progress bar
            val progBar = pidLayout.findViewById<ProgressBar>(R.id.pid_land_progress)
            progBar.min = (did.progMin * data.multiplier).toInt()
            progBar.max = (did.progMax * data.multiplier).toInt()
            progBar.progress = (did.value * data.multiplier).toInt()
            progBar.progressTintList = ColorStateList.valueOf(Color.GREEN)
            progBar.scaleY *= Settings.displaySize

            val lLayout = view.findViewById<LinearLayout>(R.id.loggingLayoutScroll)
            lLayout.addView(pidLayout)
        }

        //Start Logging
        val serviceIntent = Intent(context, BTService::class.java)
        serviceIntent.action = BT_DO_CHECK_PID.toString()
        startForegroundService(this.requireContext(), serviceIntent)

        //Do we keep the screen on?
        view.keepScreenOn = Settings.keepScreenOn

        //update PID text
        updatePIDText()

        //Set background color
        view.setBackgroundColor(Settings.colorNormal)
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
        var currentOrientation = resources.configuration.orientation

        if(Settings.alwaysPortrait)
            currentOrientation = Configuration.ORIENTATION_PORTRAIT

        if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            textVal = R.string.textPIDL
        }

        //Update text
        val mViewModel: LoggingViewModel by viewModels()
        for(i in 0 until DIDs.list().count()) {
            val did = DIDs.list()[i]

            val layout = mPIDS!![i]
            val textView = layout!!.findViewById<TextView>(R.id.pid_land_text)
            val data = mViewModel.dataList!![i]!!

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
                    for(i in 0 until DIDs.list().count()) {
                        //get the current did
                        val did = DIDs.list()[i]
                        val layout = mPIDS!![i]
                        val data = mViewModel.dataList!![i]!!
                        val progressBar = layout!!.findViewById<ProgressBar>(R.id.pid_land_progress)

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
                            view?.setBackgroundColor(Settings.colorWarn)
                        }

                        mViewModel.lastWarning = true
                    } else {
                        if(mViewModel.lastWarning) {
                            view?.setBackgroundColor(Settings.colorNormal)
                        }

                        mViewModel.lastWarning = false
                    }

                    //Update fps
                    val fps = readCount.toFloat() / (readTime.toFloat() / 1000.0f)
                    mPackCount?.text = getString(R.string.textview_fps) + "%03.1f".format(fps)
                    if (UDSLogger.isEnabled()) {
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