package com.app.simostools

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.Math.random

var gPlaybackData: Array<PlayBackDataStruct?>?  = null

class PlaybackViewModel : ViewModel() {
}

data class PlayBackDataStruct(var name: String,
                                var tabs: String,
                                var min: Float,
                                var max: Float,
                                var enabled: Boolean,
                                var color: Int,
                                var data: FloatArray)

class PlaybackFragment: Fragment() {
    private var TAG                     = "PlaybackFragment"
    private var mGraph:SwitchGraph?     = null
    private var mButtons:LinearLayout?  = null
    private lateinit var mViewModel: PlaybackViewModel

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

        return inflater.inflate(R.layout.fragment_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(PlaybackViewModel::class.java)

        val setEnabledButton = view.findViewById<SwitchButton>(R.id.buttonSetPIDS)
        setEnabledButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                gPlaybackData?.let {
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
                gPlaybackData?.let {
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
                    type = "*/*"
                }
                chooseFile = Intent.createChooser(chooseFile, "Choose a CSV")
                resultPickLauncher.launch(chooseFile)
            }

            setOnLongClickListener {
                if(LogFile.getLastUri() != null) loadPlaybackCSV(LogFile.getLastUri()!!)
                else if(LogFile.getLastFileName() != "") loadPlaybackCSV(LogFile.getLastFileName(), LogFile.getLastFileDir())

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

        mGraph = view.findViewById(R.id.switchGraph)
        mGraph?.data = gPlaybackData

        DebugLog.d(TAG, "onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        DebugLog.d(TAG, "onDestroyView")
    }

    override fun onResume() {
        super.onResume()

        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)

        DebugLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()

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

    private fun loadPlaybackCSV(fileName: String, subFolder: String) {
        val path = context?.getExternalFilesDir(subFolder)
        path?.let {
            if (!path.exists())
                return

            val logFile = File(path, "/$fileName")
            if (!logFile.exists())
                return

            val inputStream = FileInputStream(logFile)
            val fileData = String(inputStream.readBytes())
            inputStream.close()

            loadPlaybackCSV(fileData)
        }
    }

    private fun loadPlaybackCSV(uri: Uri) {
        val inputStream = activity?.contentResolver?.openInputStream(uri)
        val fileData = String(inputStream?.readBytes() ?: byteArrayOf())
        inputStream?.close()

        loadPlaybackCSV(fileData)
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
                gPlaybackData = null

                gPlaybackData = arrayOfNulls(lineItems.count())
                gPlaybackData?.let { playbackData ->
                    for (i in 0 until lineItems.count()) {
                        val r = (random() * 255).toFloat()
                        val g = (random() * 255).toFloat()
                        val b = (random() * 255).toFloat()
                        playbackData[i] = PlayBackDataStruct(lineItems[i], "", 0f, 0f, false, Color.rgb(r, g, b), floatArrayOf())
                        PIDs.getList()?.let { pidList ->
                            pidList.forEach { pid ->
                                pid?.let {
                                    if (lineItems[i].contains(pid.name)) {
                                        playbackData[i]?.min = pid.progMin
                                        playbackData[i]?.max = pid.progMax
                                        playbackData[i]?.enabled = true
                                        playbackData[i]?.tabs = pid.tabs
                                    }
                                }
                            }
                        }
                        DebugLog.d(TAG, "Data item: ${lineItems[i]}")
                    }
                }
            } else {
                //Read data
                gPlaybackData?.let { pidList ->
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

        gPlaybackData?.let { playbackData ->
            playbackData.forEach {
                it?.enabled = false
            }
        }

        mGraph?.data = gPlaybackData
    }
}