package com.app.simoslogger

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startForegroundService
import android.widget.ArrayAdapter
import androidx.core.view.isVisible

var gMsgList: Array<String>? = null

class FlashingFragment : Fragment() {
    private val TAG = "FlashingFragment"
    private var mArrayAdapter: ArrayAdapter<String>? = null

    var resultPickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                UDSFlasher.setBinFile(requireActivity().contentResolver.openInputStream(uri)!!)


                // Tell the service to start flashing
                val serviceIntent = Intent(context, BTService::class.java)
                serviceIntent.action = BTServiceTask.DO_START_FLASH.toString()
                startForegroundService(this.requireContext(), serviceIntent)

                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
            }?: Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_flashing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mArrayAdapter = ArrayAdapter(requireContext(), R.layout.message)
        mArrayAdapter?.let { adapter ->
            gMsgList?.forEach {
                adapter.add(it)
            }
        }
        view.findViewById<ListView>(R.id.bt_message)?.let { messageBox ->
            messageBox.adapter = mArrayAdapter
            messageBox.setBackgroundColor(Color.WHITE)
        }
        view.findViewById<Button>(R.id.buttonFlashCAL).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "*/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a CAL file")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.buttonECUInfo).setOnClickListener {
            // Get the message bytes and tell the BluetoothChatService to write
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BTServiceTask.DO_GET_INFO.toString()
            startForegroundService(this.requireContext(), serviceIntent)
        }

        view.findViewById<Button>(R.id.buttonClearDTC).setOnClickListener {
            // Get the message bytes and tell the BluetoothChatService to write
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BTServiceTask.DO_CLEAR_DTC.toString()
            startForegroundService(this.requireContext(), serviceIntent)
        }

        view.findViewById<ProgressBar>(R.id.progressBarFlash)?.let { progress ->
            progress.progress = 0
            progress.isVisible = false
            progress.max = 100
            progress.min = 0
            progress.setScaleY(3F)
        }

        setColor()
    }

    override fun onResume() {
        super.onResume()

        setColor()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.FLASH_INFO.toString())
        filter.addAction(GUIMessage.FLASH_PROGRESS.toString())
        filter.addAction(GUIMessage.FLASH_PROGRESS_SHOW.toString())
        filter.addAction(GUIMessage.FLASH_INFO_CLEAR.toString())
        this.activity?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        this.activity?.unregisterReceiver(mBroadcastReceiver)
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            DebugLog.d(TAG, "Flashing Fragment received action: " + intent.action.toString())
            when (intent.action) {
                GUIMessage.FLASH_INFO.toString()          -> doWriteMessage(intent.getStringExtra(GUIMessage.FLASH_INFO.toString())?: "")
                GUIMessage.FLASH_PROGRESS.toString()      -> setProgressBar(intent.getIntExtra(GUIMessage.FLASH_PROGRESS.toString(), 0))
                GUIMessage.FLASH_PROGRESS_MAX.toString()  -> setProgressBarMax(intent.getIntExtra(GUIMessage.FLASH_PROGRESS_MAX.toString(), 0))
                GUIMessage.FLASH_PROGRESS_SHOW.toString() -> setProgressBarShow(intent.getBooleanExtra(GUIMessage.FLASH_PROGRESS_SHOW.toString(), false))
                GUIMessage.FLASH_INFO_CLEAR.toString()    -> doClearMessages()
            }
        }
    }

    private fun doWriteMessage(message: String) {
        // construct a string from the valid bytes in the buffer
        val value = gMsgList?: arrayOf()
        gMsgList = value + message
        mArrayAdapter?.add(message)

        val btMessage = view?.findViewById<ListView>(R.id.bt_message)!!
        //btMessage.setSelection(btMessage.adapter.count - 1)
        btMessage.smoothScrollToPosition(btMessage.count)

    }

    private fun doClearMessages(){
        //todo
    }

    private fun setColor() {
        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)
    }

    private fun setProgressBar(amount: Int) {
        val pBar = view?.findViewById<ProgressBar>(R.id.progressBarFlash)
        pBar?.progress = amount

    }

    private fun setProgressBarMax(amount: Int) {
        val pBar = view?.findViewById<ProgressBar>(R.id.progressBarFlash)
        pBar?.max = amount
    }

    private fun setProgressBarShow(allow: Boolean) {
        val pBar = view?.findViewById<ProgressBar>(R.id.progressBarFlash)
        pBar?.isVisible = allow
    }
}
