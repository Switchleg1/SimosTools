package com.app.simoslogger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.content.res.Configuration
import android.widget.LinearLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.Exception

data class DATAStruct(var min: Float,
                        var max: Float,
                        var lastColor: Boolean,
                        var multiplier: Float,
                        var inverted: Boolean)

class LoggingViewModel : ViewModel() {
    var lastWarning = false
    var lastEnabled = false
    var dataList: Array<DATAStruct?>? = null
}

class LoggingFragment : Fragment() {
    private var TAG = "LoggingFragment"
    private var mPackCount: TextView? = null
    private var mPIDS: Array<View?>? = null
    private lateinit var mViewModel: LoggingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get view model
        mViewModel = ViewModelProvider(this).get(LoggingViewModel::class.java)

        view.findViewById<Button>(R.id.button_back).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<Button>(R.id.buttonReset).setOnClickListener {
            try {
                mViewModel.dataList?.let { dataList ->
                    for (i in 0 until dataList.count()) {
                        val data = dataList[i]
                        data?.let {
                            val did = PIDs.getList()!![i]
                            did?.let {
                                data.max = did.value
                                data.min = did.value
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                DebugLog.e(TAG, "Unable to reset min/max list.", e)
            }

            updatePIDText()
        }

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
        mPackCount?.setTextColor(Settings.colorList[COLOR_BAR_NORMAL])

        //Build layout
        PIDs.getList()?.let { list ->
            if (mViewModel.dataList == null || mViewModel.dataList?.count() != list.count()) {
                mViewModel.dataList = arrayOfNulls(list.count())
            }

            mPIDS = arrayOfNulls(list.count())
            for (i in 0 until list.count()) {
                //build child layout
                val pidLayout = layoutInflater.inflate(layoutType, null)
                mPIDS!![i] = pidLayout

                //build new data structure
                val data = DATAStruct(0.0f, 0.0f, false, 1.0f, false)
                mViewModel.dataList!![i] = data

                //get current did
                val did = list[i]
                did?.let {
                    //Check for low value PIDS
                    var progMax = did.progMax
                    var progMin = did.progMin

                    //if progress bar is flipped
                    if(did.progMin > did.progMax) {
                        progMax = did.progMin
                        progMin = did.progMax
                        data.inverted = true
                    }

                    //if progress bar value is small
                    if ((progMax - progMin) < 100.0f) {
                        data.multiplier = 100.0f / (progMax - progMin)
                    } else {
                        data.multiplier = 1.0f
                    }

                    //find text view and set text
                    val textView = pidLayout.findViewById<TextView>(R.id.pid_land_text)
                    textView.text = getString(
                        textVal,
                        did.name,
                        did.format.format(did.value),
                        did.unit,
                        did.format.format(data.min),
                        did.format.format(data.max)
                    )
                    textView.textSize = 18 * Settings.displaySize
                    textView.setTextColor(Settings.colorList[COLOR_TEXT])

                    //Setup the progress bar
                    val progBar = pidLayout.findViewById<ProgressBar>(R.id.pid_land_progress)
                    progBar.min = (progMin * data.multiplier).toInt()
                    progBar.max = (progMax * data.multiplier).toInt()
                    progBar.scaleY *= Settings.displaySize
                    progBar.progressTintList = ColorStateList.valueOf(Settings.colorList[COLOR_BAR_NORMAL])
                    progBar.progress = when(data.inverted) {
                        true -> ((did.progMax - (did.value - did.progMin)) * data.multiplier).toInt()
                        false -> (did.value * data.multiplier).toInt()
                    }

                    val lLayout = view.findViewById<LinearLayout>(R.id.loggingLayoutScroll)
                    lLayout.addView(pidLayout)
                }
            }
        }

        //Do we keep the screen on?
        view.keepScreenOn = Settings.keepScreenOn

        //update PID text
        updatePIDText()

        //Set background color
        if(mViewModel.lastWarning) view.setBackgroundColor(Settings.colorList[COLOR_BG_WARN])
            else view.setBackgroundColor(Settings.colorList[COLOR_BG_NORMAL])
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(MESSAGE_READ_LOG.toString())
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
        try {
            for (i in 0 until PIDs.getList()!!.count()) {
                val did = PIDs.getList()!![i]
                val layout = mPIDS!![i]
                val textView = layout!!.findViewById<TextView>(R.id.pid_land_text)
                mViewModel.dataList?.let { datalist ->
                    val data = datalist[i]

                    textView?.text = getString(
                        textVal,
                        did!!.name,
                        did.format.format(did.value),
                        did.unit,
                        did.format.format(data?.min),
                        did.format.format(data?.max)
                    )
                }
            }
        } catch(e: Exception) {
            DebugLog.e(TAG, "Unable to update text", e)
        }
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
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
                    var anyWarning = false
                    try {
                        for (i in 0 until PIDs.getList()!!.count()) {
                            //get the current did
                            val did = PIDs.getList()!![i]!!
                            val layout = mPIDS!![i]
                            val data = mViewModel.dataList!![i]!!
                            val progressBar = layout!!.findViewById<ProgressBar>(R.id.pid_land_progress)

                            //set min/max
                            if (did.value > data.max)
                                data.max = did.value

                            if (did.value < data.min)
                                data.min = did.value


                            //Update progress is the value is different
                            val newProgress = when(data.inverted) {
                                true -> ((did.progMax - (did.value - did.progMin)) * data.multiplier).toInt()
                                false -> (did.value * data.multiplier).toInt()
                            }

                            if (newProgress != progressBar?.progress) {
                                progressBar?.progress = newProgress
                            }

                            //Check to see if we should be warning user
                            if ((did.value > did.warnMax) or (did.value < did.warnMin)) {

                                if (!data.lastColor) {
                                    progressBar?.progressTintList = ColorStateList.valueOf(Settings.colorList[COLOR_BAR_WARN])
                                }

                                data.lastColor = true
                                anyWarning = true
                            } else {
                                if (data.lastColor) {
                                    progressBar?.progressTintList = ColorStateList.valueOf(Settings.colorList[COLOR_BAR_NORMAL])
                                }

                                data.lastColor = false
                            }
                        }
                    } catch (e: Exception) {
                        DebugLog.e(TAG, "Unable to update display", e)
                    }

                    //If any visible PIDS are in warning state set background color to warn
                    if(anyWarning) {
                        if(!mViewModel.lastWarning) {
                            view?.setBackgroundColor(Settings.colorList[COLOR_BG_WARN])
                        }

                        mViewModel.lastWarning = true
                    } else {
                        if(mViewModel.lastWarning) {
                            view?.setBackgroundColor(Settings.colorList[COLOR_BG_NORMAL])
                        }

                        mViewModel.lastWarning = false
                    }

                    //Update fps
                    val fps = readCount.toFloat() / (readTime.toFloat() / 1000.0f)
                    mPackCount?.text = getString(R.string.textview_fps) + "%03.1f".format(fps)
                    if (UDSLogger.isEnabled()) {
                        //Highlight packet count in red since we are logging
                        if(!mViewModel.lastEnabled) {
                            mPackCount?.setTextColor(Settings.colorList[COLOR_BAR_WARN])
                        }
                        mViewModel.lastEnabled = true
                    } else {
                        //Not logging set packet count to black
                        if(mViewModel.lastEnabled) {
                            mPackCount?.setTextColor(Settings.colorList[COLOR_BAR_NORMAL])
                        }
                        mViewModel.lastEnabled = false
                    }
                }
            }
        }
    }
}