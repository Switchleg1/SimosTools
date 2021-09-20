package com.app.vwflashtools

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.fragment.app.viewModels


class FlashingViewModel : ViewModel() {
    var mConversationArrayAdapter: ArrayAdapter<String>? = null
    var mConnectedDeviceName: String? = null
}

class FlashingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_flashing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mViewModel: FlashingViewModel by viewModels()
        if(mViewModel.mConversationArrayAdapter == null)
            mViewModel.mConversationArrayAdapter = ArrayAdapter(requireActivity(), R.layout.message)
        view.findViewById<ListView>(R.id.bt_message).adapter = mViewModel.mConversationArrayAdapter


        view.findViewById<Button>(R.id.button_back2).setOnClickListener {
            findNavController().navigate(R.id.action_FlashingFragment_to_FirstFragment)
        }

        view.findViewById<Button>(R.id.button_send).setOnClickListener {
            // Get the message bytes and tell the BluetoothChatService to write
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_DO_CHECK_VIN.toString()
            startForegroundService(this.requireContext(), serviceIntent)
        }

        view.findViewById<Button>(R.id.button_send_file).setOnClickListener {
            //if(BTService.getState() == STATE_CONNECTED) {
            //    var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            //    chooseFile.type = "*/*"
            //    chooseFile = Intent.createChooser(chooseFile, "Choose a bin")
           //     resultPickLauncher.launch(intent)
            //}
        }
    }

    var resultPickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            /*val uri: Uri? = data?.data
            var fileStream: InputStream? = null

            if(uri != null)
                fileStream = activity?.contentResolver?.openInputStream(uri);

            if(uri != null && BTService.getState() == STATE_CONNECTED) {
                Toast.makeText(activity, "Upload started", Toast.LENGTH_SHORT).show()
                mConversationArrayAdapter?.add("Upload started.")
                //BTService.upload(fileStream)
            }*/
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(MESSAGE_STATE_CHANGE.toString())
        filter.addAction(MESSAGE_WRITE.toString())
        filter.addAction(MESSAGE_READ.toString())
        filter.addAction(MESSAGE_READ_VIN.toString())
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
                MESSAGE_STATE_CHANGE.toString() -> {
                    when (intent.getIntExtra("newState", -1)) {
                        STATE_CONNECTED -> {
                            val cDevice = intent.getStringExtra("cDevice")
                            val mViewModel: FlashingViewModel by viewModels()
                            mViewModel.mConnectedDeviceName = cDevice
                        }
                    }
                }
                MESSAGE_WRITE.toString() -> {
                    val writeBuff = intent.getByteArrayExtra("writeBuffer")

                    // construct a string from the buffer
                    if(writeBuff != null) {
                        val writeString = String(writeBuff)

                        val mViewModel: FlashingViewModel by viewModels()
                        mViewModel.mConversationArrayAdapter?.add("Me:  $writeString")
                        val btMessage = view?.findViewById<ListView>(R.id.bt_message)!!
                        btMessage.setSelection(btMessage.adapter.count - 1);
                    }
                }
                MESSAGE_READ.toString() -> {
                    val readBuff = intent.getByteArrayExtra("readBuffer")

                    // construct a string from the valid bytes in the buffer
                    if(readBuff != null) {
                        val mViewModel: FlashingViewModel by viewModels()
                        mViewModel.mConversationArrayAdapter?.add(mViewModel.mConnectedDeviceName.toString() + ":  $readBuff")

                        val btMessage = view?.findViewById<ListView>(R.id.bt_message)!!
                        btMessage.setSelection(btMessage.adapter.count - 1);
                    }
                }
                MESSAGE_READ_VIN.toString() -> {
                    val readBuff = intent.getByteArrayExtra("readBuffer")

                    // construct a string from the valid bytes in the buffer
                    if(readBuff != null) {
                        val mViewModel: FlashingViewModel by viewModels()
                        mViewModel.mConversationArrayAdapter?.add(mViewModel.mConnectedDeviceName.toString() + ":  $readBuff")

                        val btMessage = view?.findViewById<ListView>(R.id.bt_message)!!
                        btMessage.setSelection(btMessage.adapter.count - 1);
                    }
                }
                MESSAGE_TOAST.toString() -> {
                    val nToast = intent.getStringExtra("newToast")
                    Toast.makeText(activity, nToast, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}