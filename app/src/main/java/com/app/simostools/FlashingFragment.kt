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

var gFlashMsgList: Array<String>? = arrayOf()

class FlashViewModel : ViewModel() {
    var connectionState: BLEConnectionState = BLEConnectionState.NONE
    var flashFull: Boolean = false
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
                UDSFlasher.setFullFlash(mViewModel.flashFull)

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

        mArrayAdapter = SwitchArrayAdapter(requireContext(), R.layout.fragment_message, gFlashMsgList?: arrayOf())
        mArrayAdapter?.let { adapter ->
            gFlashMsgList?.forEach {
                adapter.add(it)
            }
        }

        view.findViewById<ListView>(R.id.listViewMessage)?.let { messageBox ->
            messageBox.adapter = mArrayAdapter
            messageBox.setBackgroundColor(Color.WHITE)
        }

        val flashCalButton = view.findViewById<SwitchButton>(R.id.buttonFlashCAL)
        flashCalButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                clickFlash(false)
            }
        }

        val flashFullButton = view.findViewById<SwitchButton>(R.id.buttonFlashFull)
        flashFullButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                clickFlash(true)
            }
        }

        val tuneInfoButton = view.findViewById<SwitchButton>(R.id.buttonTuneInfo)
        tuneInfoButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                if (mViewModel.connectionState == BLEConnectionState.CONNECTED) {
                    sendServiceMessage(BTServiceTask.DO_GET_TUNE_INFO.toString())
                } else {
                    doWriteMessage("Tune Info\n---------------\nNot connected.")
                }
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

        view.findViewById<ProgressBar>(R.id.progressBarFlash)?.apply {
            progress = 0
            isVisible = false
            max = 100
            min = 0
        }

        setColor()
    }

    override fun onResume() {
        super.onResume()

        setColor()

        //Do we keep the screen on?
        view?.keepScreenOn = ConfigSettings.KEEP_SCREEN_ON.toBoolean()

        //register broadcast receiver
        val filter = IntentFilter()
        filter.addAction(GUIMessage.STATE_CONNECTION.toString())
        filter.addAction(GUIMessage.STATE_TASK.toString())
        filter.addAction(GUIMessage.FLASH_INFO.toString())
        filter.addAction(GUIMessage.FLASH_INFO_CLEAR.toString())
        filter.addAction(GUIMessage.FLASH_PROGRESS.toString())
        filter.addAction(GUIMessage.FLASH_PROGRESS_MAX.toString())
        filter.addAction(GUIMessage.FLASH_PROGRESS_SHOW.toString())
        filter.addAction(GUIMessage.FLASH_CONFIRM.toString())
        filter.addAction(GUIMessage.FLASH_BUTTON_RESET.toString())
        activity?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        //Do we keep the screen on?
        view?.keepScreenOn = false

        //unregister broadcast receiver
        activity?.unregisterReceiver(mBroadcastReceiver)
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            DebugLog.d(TAG, "Flashing Fragment received action: " + intent.action.toString())
            when (intent.action) {
                GUIMessage.STATE_CONNECTION.toString()    -> mViewModel.connectionState = intent.getSerializableExtra(GUIMessage.STATE_CONNECTION.toString()) as BLEConnectionState
                GUIMessage.STATE_TASK.toString()          -> mViewModel.connectionState = BLEConnectionState.CONNECTED
                GUIMessage.FLASH_INFO.toString()          -> doWriteMessage(intent.getStringExtra(GUIMessage.FLASH_INFO.toString())?: "")
                GUIMessage.FLASH_INFO_CLEAR.toString()    -> doClearMessages()
                GUIMessage.FLASH_PROGRESS.toString()      -> setProgressBar(intent.getIntExtra(GUIMessage.FLASH_PROGRESS.toString(), 0))
                GUIMessage.FLASH_PROGRESS_MAX.toString()  -> setProgressBarMax(intent.getIntExtra(GUIMessage.FLASH_PROGRESS_MAX.toString(), 0))
                GUIMessage.FLASH_PROGRESS_SHOW.toString() -> setProgressBarShow(intent.getBooleanExtra(GUIMessage.FLASH_PROGRESS_SHOW.toString(), false))
                GUIMessage.FLASH_CONFIRM.toString()       -> promptUserConfirmation()
                GUIMessage.FLASH_BUTTON_RESET.toString()  -> resetFlashButton(false)
            }
        }
    }

    private fun clickFlash(full: Boolean) {
        var flashString = if(full) "Flash Full\n---------------"
        else "Flash CAL\n---------------"

        if (mViewModel.connectionState == BLEConnectionState.CONNECTED) {
            mViewModel.flashFull = full
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "*/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a Fullbin")
            resultPickLauncher.launch(chooseFile)
        } else {
            flashString += "\nNot connected."
        }

        doWriteMessage(flashString)
    }

    private fun resetFlashButton(full: Boolean) {
        val flashButton = if(full) requireView().findViewById<SwitchButton>(R.id.buttonFlashFull)
            else requireView().findViewById<SwitchButton>(R.id.buttonFlashCAL)

        flashButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            text = if(full) "Flash Full"
                else "Flash CAL"
            setOnClickListener {
                clickFlash(full)
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
        gFlashMsgList = arrayOf()
        mArrayAdapter?.let {
            it.clear()

            val btMessage = view?.findViewById<ListView>(R.id.listViewMessage)
            btMessage?.setSelection(0)
        }
    }

    private fun doWriteMessage(message: String) {
        // construct a string from the valid bytes in the buffer
        val value = gFlashMsgList?: arrayOf()
        gFlashMsgList = value + message
        mArrayAdapter?.let {
            it.add(message)

            val btMessage = view?.findViewById<ListView>(R.id.listViewMessage)
            btMessage?.setSelection(it.count - 1)
        }
    }

    private fun setColor() {
        val btMessage = view?.findViewById<ListView>(R.id.listViewMessage)
        btMessage?.setBackgroundColor(ColorList.BG_NORMAL.value)
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
        activity?.let {
            val serviceIntent = Intent(it, BTService::class.java)
            serviceIntent.action = type
            startForegroundService(it, serviceIntent)
        }
    }
}
