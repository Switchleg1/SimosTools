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
import androidx.navigation.fragment.findNavController
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.net.URI

class FlashingViewModel : ViewModel() {
    var conversationArrayAdapter: ArrayAdapter<String>? = null
}

class FlashingFragment : Fragment() {
    private val TAG = "FlashingFragment"
    private lateinit var mViewModel: FlashingViewModel

    var resultPickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                UDSFlasher.setUri(uri)

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

        //Get view model
        mViewModel = ViewModelProvider(this).get(FlashingViewModel::class.java)

        if(mViewModel.conversationArrayAdapter == null)
            mViewModel.conversationArrayAdapter = ArrayAdapter(requireActivity(), R.layout.message)
        view.findViewById<ListView>(R.id.bt_message).adapter = mViewModel.conversationArrayAdapter
        view.findViewById<ListView>(R.id.bt_message).setBackgroundColor(Color.WHITE)

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

        view.findViewById<Button>(R.id.buttonBack2).setOnClickListener {
            findNavController().navigateUp()
        }

        //Set background color
        view.setBackgroundColor(ColorList.BG_NORMAL.value)
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.ECU_INFO.toString())
        filter.addAction(GUIMessage.CLEAR_DTC.toString())
        filter.addAction(GUIMessage.FLASH_INFO.toString())
        this.activity?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        this.activity?.unregisterReceiver(mBroadcastReceiver)
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                GUIMessage.ECU_INFO.toString()   -> doWriteMessage(intent.getStringExtra(GUIMessage.ECU_INFO.toString())?: "")
                GUIMessage.CLEAR_DTC.toString()  -> doWriteMessage(intent.getStringExtra(GUIMessage.CLEAR_DTC.toString())?: "")
                GUIMessage.FLASH_INFO.toString() -> doWriteMessage(intent.getStringExtra(GUIMessage.FLASH_INFO.toString())?: "")
            }
        }
    }

    private fun doWriteMessage(message: String) {
        // construct a string from the valid bytes in the buffer
        mViewModel.conversationArrayAdapter?.add(message)

        val btMessage = view?.findViewById<ListView>(R.id.bt_message)!!
        btMessage.setSelection(btMessage.adapter.count - 1)
    }
}