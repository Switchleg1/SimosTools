package com.app.simostools

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startForegroundService
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

var gMsgList: Array<String>? = null

class FlashViewModel : ViewModel() {
    var connectionState: BLEConnectionState = BLEConnectionState.NONE
}

class FlashingFragment : Fragment() {
    private val TAG = "FlashingFragment"
    private var mArrayAdapter: ArrayAdapter<String>? = null
    private lateinit var mViewModel: FlashViewModel
    private var flashConfirmationHoldTime: Long = 0L


    var resultPickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                UDSFlasher.setBinFile(requireActivity().contentResolver.openInputStream(uri)!!)


                // Tell the service to start flashing
                sendServiceMessage(BTServiceTask.DO_START_FLASH.toString())

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
        mViewModel = ViewModelProvider(this).get(FlashViewModel::class.java)

        mArrayAdapter = ArrayAdapter(requireContext(), R.layout.flashing_message)
        mArrayAdapter?.let { adapter ->
            gMsgList?.forEach {
                adapter.add(it)
            }
        }

        view.findViewById<ListView>(R.id.listViewMessage)?.let { messageBox ->
            messageBox.adapter = mArrayAdapter
            messageBox.setBackgroundColor(Color.WHITE)
        }

        val flashButton = view.findViewById<SwitchButton>(R.id.buttonFlashCAL)
        flashButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                if (mViewModel.connectionState == BLEConnectionState.CONNECTED) {
                    var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                    chooseFile.type = "*/*"
                    chooseFile = Intent.createChooser(chooseFile, "Choose a CAL file")
                    resultPickLauncher.launch(chooseFile)
                } else {
                    doWriteMessage("Not connected")
                }
            }
        }

        val ecuInfoButton = view.findViewById<SwitchButton>(R.id.buttonFlashECUInfo)
        ecuInfoButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                if (mViewModel.connectionState == BLEConnectionState.CONNECTED) {
                    sendServiceMessage(BTServiceTask.DO_GET_INFO.toString())
                } else {
                    doWriteMessage("Not connected")
                }
            }
        }

        val clearDTCButton = view.findViewById<SwitchButton>(R.id.buttonFlashClearDTC)
        clearDTCButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                if (mViewModel.connectionState == BLEConnectionState.CONNECTED) {
                    sendServiceMessage(BTServiceTask.DO_CLEAR_DTC.toString())
                } else {
                    doWriteMessage("Not connected")
                }
            }
        }

        val backButton = view.findViewById<SwitchButton>(R.id.buttonFlashBack)
        backButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                findNavController().navigateUp()
            }
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
        filter.addAction(GUIMessage.STATE_CONNECTION.toString())
        filter.addAction(GUIMessage.STATE_TASK.toString())
        filter.addAction(GUIMessage.FLASH_INFO.toString())
        filter.addAction(GUIMessage.FLASH_PROGRESS.toString())
        filter.addAction(GUIMessage.FLASH_PROGRESS_SHOW.toString())
        filter.addAction(GUIMessage.FLASH_INFO_CLEAR.toString())
        filter.addAction(GUIMessage.FLASH_CONFIRM.toString())
        filter.addAction(GUIMessage.FLASH_BUTTON_RESET.toString())
        context?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        context?.unregisterReceiver(mBroadcastReceiver)
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            DebugLog.d(TAG, "Flashing Fragment received action: " + intent.action.toString())
            when (intent.action) {
                GUIMessage.STATE_CONNECTION.toString()    -> mViewModel.connectionState = intent.getSerializableExtra(GUIMessage.STATE_CONNECTION.toString()) as BLEConnectionState
                GUIMessage.STATE_TASK.toString()          -> mViewModel.connectionState = BLEConnectionState.CONNECTED
                GUIMessage.FLASH_INFO.toString()          -> doWriteMessage(intent.getStringExtra(GUIMessage.FLASH_INFO.toString())?: "")
                GUIMessage.FLASH_PROGRESS.toString()      -> setProgressBar(intent.getIntExtra(GUIMessage.FLASH_PROGRESS.toString(), 0))
                GUIMessage.FLASH_PROGRESS_MAX.toString()  -> setProgressBarMax(intent.getIntExtra(GUIMessage.FLASH_PROGRESS_MAX.toString(), 0))
                GUIMessage.FLASH_PROGRESS_SHOW.toString() -> setProgressBarShow(intent.getBooleanExtra(GUIMessage.FLASH_PROGRESS_SHOW.toString(), false))
                GUIMessage.FLASH_CONFIRM.toString()       -> promptUserConfirmation()
                GUIMessage.FLASH_BUTTON_RESET.toString()  -> resetFlashButton()
                GUIMessage.FLASH_INFO_CLEAR.toString()    -> doClearMessages()
            }
        }
    }

    private fun resetFlashButton() {
        val flashButton = requireView().findViewById<SwitchButton>(R.id.buttonFlashCAL)

        flashButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            text = "Flash CAL"
            setOnClickListener {
                if (mViewModel.connectionState == BLEConnectionState.CONNECTED) {
                    var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                    chooseFile.type = "*/*"
                    chooseFile = Intent.createChooser(chooseFile, "Choose a CAL file")
                    resultPickLauncher.launch(chooseFile)
                } else {
                    doWriteMessage("Not connected")
                }
            }
        }
    }

    private fun promptUserConfirmation() {
        val flashButton = requireView().findViewById<SwitchButton>(R.id.buttonFlashCAL)
        flashButton.apply {
            paintBG.color = ColorList.BT_BG_ALERT.value
            paintRim.color = ColorList.BT_RIM_ALERT.value
            setTextColor(ColorList.BT_TEXT.value)
            text = "Press to cancel, Hold to confirm"
            setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN ->{
                            flashConfirmationHoldTime = SystemClock.uptimeMillis()
                            //time the button press
                        }
                        MotionEvent.ACTION_UP -> {
                            var now = SystemClock.uptimeMillis()
                            if(now - flashConfirmationHoldTime > 1000){

                                sendServiceMessage(BTServiceTask.FLASH_CONFIRMED.toString())

                            }
                            else{

                                sendServiceMessage(BTServiceTask.FLASH_CANCELED.toString())

                            }

                        }
                    }

                    return v?.onTouchEvent(event) ?: true
                }
            })
            setOnClickListener {

            }
        }
    }

    private fun doClearMessages() {
        gMsgList = arrayOf()
        mArrayAdapter?.let {
            val btMessage = view?.findViewById<ListView>(R.id.listViewMessage)
            btMessage?.setSelection(0)
        }
    }

    private fun doWriteMessage(message: String) {
        // construct a string from the valid bytes in the buffer
        val value = gMsgList?: arrayOf()
        gMsgList = value + message
        mArrayAdapter?.let {
            it.add(message)

            val btMessage = view?.findViewById<ListView>(R.id.listViewMessage)
            btMessage?.setSelection(it.count - 1)
        }
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

    private fun sendServiceMessage(type: String) {
        context?.let {
            val serviceIntent = Intent(it, BTService::class.java)
            serviceIntent.action = type
            startForegroundService(it, serviceIntent)
        }
    }
}
