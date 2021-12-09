package com.app.simostools

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.lang.Math.random

var gLogViewerData: Array<LogViewerDataStruct?>?  = null

class LogViewerViewModel : ViewModel() {
    var fullScreen: Boolean = false
}

data class LogViewerDataStruct(var name: String,
                                var tabs: String,
                                var min: Float,
                                var max: Float,
                                var enabled: Boolean,
                                var color: Int,
                                var format: String,
                                var data: FloatArray)

class LogViewerFragment: Fragment() {
    private var TAG                     = "LogViewerFragment"
    private var mGraph:SwitchGraph?     = null
    private var mButtons:LinearLayout?  = null
    private var mPortrait:Boolean       = false
    private lateinit var mViewModel: LogViewerViewModel

    var resultPickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                loadPlaybackCSV(uri)
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
            }?: Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        DebugLog.d(TAG, "onDestroy")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_log_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(LogViewerViewModel::class.java)

        val drawerButton = view.findViewById<SwitchButton>(R.id.buttonButtonDrawer)
        drawerButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                mButtons?.let {
                    if (it.isVisible) {
                        it.isVisible = false
                        text = if(mPortrait) getString(R.string.button_arrow_up)
                        else getString(R.string.button_arrow_left)
                    } else {
                        it.isVisible = true
                        text = if(mPortrait) getString(R.string.button_arrow_down)
                        else getString(R.string.button_arrow_right)
                    }

                    mViewModel.fullScreen = !it.isVisible
                }
            }
        }

        val setEnabledButton = view.findViewById<SwitchButton>(R.id.buttonSetPIDS)
        setEnabledButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                gLogViewerData?.let {
                    findNavController().navigate(R.id.action_PlaybackFragment_to_PlaybackEnabledFragment)
                }
            }
        }

        val setTabsButton = view.findViewById<SwitchButton>(R.id.buttonSetTabs)
        setTabsButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                gLogViewerData?.let {
                    findNavController().navigate(R.id.action_PlaybackFragment_to_PlaybackTabsFragment)
                }
            }
        }

        val loadButton = view.findViewById<SwitchButton>(R.id.buttonLoad)
        loadButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                var chooseFile = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                }
                chooseFile = Intent.createChooser(chooseFile, "Choose a CSV")
                resultPickLauncher.launch(chooseFile)
            }

            setOnLongClickListener {
                if(LogFile.getLastUri() != null) loadPlaybackCSV(LogFile.getLastUri())
                else loadPlaybackCSV(LogFile.getLastFile())

                mGraph?.invalidate()

                return@setOnLongClickListener true
            }
        }

        val backButton = view.findViewById<SwitchButton>(R.id.buttonBack)
        backButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                findNavController().navigateUp()
            }
        }

        mButtons = view.findViewById(R.id.layoutButtons)
        mButtons?.isVisible = !mViewModel.fullScreen

        mGraph = view.findViewById(R.id.switchGraph)
        mGraph?.setData(gLogViewerData)
        mGraph?.setTextBGColor(ColorList.BG_NORMAL.value)
        checkOrientation()

        DebugLog.d(TAG, "onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        DebugLog.d(TAG, "onDestroyView")
    }

    override fun onResume() {
        super.onResume()

        //Do we keep the screen on?
        view?.keepScreenOn = ConfigSettings.KEEP_SCREEN_ON.toBoolean()

        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)

        DebugLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()

        //Do we keep the screen on?
        view?.keepScreenOn = false

        DebugLog.d(TAG, "onPause")
    }

    override fun onStart() {
        super.onStart()

        DebugLog.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()

        DebugLog.d(TAG, "onStop")
    }

    private fun checkOrientation() {
        //check orientation and type
        var currentOrientation = resources.configuration.orientation

        if (ConfigSettings.ALWAYS_PORTRAIT.toBoolean())
            currentOrientation = Configuration.ORIENTATION_PORTRAIT

        when(currentOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                mGraph?.setTextPerLine(3)
                mPortrait = false
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                mGraph?.setTextPerLine(2)
                mPortrait = true
            }
        }
    }

    private fun loadPlaybackCSV(csvFile: File?) {
        csvFile?.let {
            if (!csvFile.exists())
                return

            val inputStream = FileInputStream(csvFile)
            var fileData = inputStream.readBytes()
            inputStream.close()

            if(fileData.count() > MAX_LOG_SIZE)
                fileData = fileData.copyOfRange(0, MAX_LOG_SIZE)

            loadPlaybackCSV(String(fileData))
        }
    }

    private fun loadPlaybackCSV(uri: Uri?) {
        uri?.let {
            val inputStream = activity?.contentResolver?.openInputStream(uri)
            var fileData = inputStream?.readBytes() ?: byteArrayOf()
            inputStream?.close()

            if (fileData.count() > MAX_LOG_SIZE)
                fileData = fileData.copyOfRange(0, MAX_LOG_SIZE)

            loadPlaybackCSV(String(fileData))
        }
    }

    private fun loadPlaybackCSV(data: String?) {
        var fileData = data ?: ""
        var readHeader = false
        var readFirst = false
        do {
            //get data line by line
            var dataLine = fileData.substringBefore("\n", "")
            fileData = fileData.substringAfter("\n", "")

            //get items from the current line
            var lineItems: Array<String> = arrayOf()
            do {
                val lineItem = dataLine.substringBefore(",", "")
                dataLine = dataLine.substringAfter(",", "")
                if(lineItem.isNotEmpty())
                    lineItems += lineItem
            } while (dataLine.isNotEmpty())

            //Are we reading the header?
            if(!readHeader) {
                readHeader = true
                gLogViewerData = null

                gLogViewerData = arrayOfNulls(lineItems.count())
                gLogViewerData?.let { playbackData ->
                    for (i in 0 until lineItems.count()) {
                        val r = (random() * 255).toFloat()
                        val g = (random() * 255).toFloat()
                        val b = (random() * 255).toFloat()
                        var foundPID = false
                        playbackData[i] = LogViewerDataStruct(lineItems[i], "", 0f, 0f, false, Color.rgb(r, g, b), "%01.1f", floatArrayOf())
                        PIDs.getList()?.let { pidList ->
                            pidList.forEach { pid ->
                                if(!foundPID) {
                                    pid?.let {
                                        if (lineItems[i].contains(pid.name) && lineItems[i].substringBefore(pid.name) == "") {
                                            playbackData[i]?.min = pid.progMin
                                            playbackData[i]?.max = pid.progMax
                                            playbackData[i]?.enabled = true
                                            playbackData[i]?.tabs = pid.tabs
                                            playbackData[i]?.format = pid.format
                                            foundPID = true
                                        }
                                    }
                                }
                            }
                        }
                        //Look through dsg list
                        if(!foundPID) {
                            PIDs.listDSG?.let { pidList ->
                                pidList.forEach { pid ->
                                    if(!foundPID) {
                                        pid?.let {
                                            if (lineItems[i].contains(pid.name)) {
                                                playbackData[i]?.min = pid.progMin
                                                playbackData[i]?.max = pid.progMax
                                                playbackData[i]?.enabled = true
                                                playbackData[i]?.tabs = "DSG"
                                                playbackData[i]?.format = pid.format
                                                foundPID = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        DebugLog.d(TAG, "Data item: ${lineItems[i]}")
                    }
                }
            } else {
                //Read data
                gLogViewerData?.let { pidList ->
                    for (i in 0 until lineItems.count()) {
                        if (pidList.count() > i) {
                            val newValue = try {
                                lineItems[i].toFloat()
                            } catch (e: Exception) {
                                0f
                            }

                            val pidItem = pidList[i]
                            pidItem?.let {
                                it.data = it.data.plus(newValue)

                                if (!it.enabled) {
                                    if (!readFirst) {
                                        it.min = newValue
                                        it.max = newValue
                                    } else {
                                        if (newValue > it.max)
                                            it.max = newValue

                                        if (newValue < it.min)
                                            it.min = newValue
                                    }
                                }
                            }
                        }
                    }
                }
                readFirst = true
            }
        } while(fileData.isNotEmpty())

        gLogViewerData?.let { playbackData ->
            playbackData.forEach {
                it?.enabled = (it?.tabs?.contains("Default")?:false)
            }
        }

        mGraph?.setData(gLogViewerData)
    }
}