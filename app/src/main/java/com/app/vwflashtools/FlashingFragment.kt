package com.app.vwflashtools

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
import androidx.core.content.ContextCompat.startForegroundService

class FlashingFragment : Fragment() {

    private val PICKFILE_RESULT_CODE = 0

    private var mConversationArrayAdapter: ArrayAdapter<String>? = null
    private var mConnectedDeviceName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_flashing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(mConversationArrayAdapter == null)
            mConversationArrayAdapter = ArrayAdapter(requireActivity(), R.layout.message)

        view.findViewById<ListView>(R.id.bt_message).adapter = mConversationArrayAdapter

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
           //     startActivityForResult(chooseFile, PICKFILE_RESULT_CODE)
            //}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICKFILE_RESULT_CODE -> {
                /*if (resultCode == Activity.RESULT_OK) {
                    val uri: Uri? = data?.data
                    var fileStream: InputStream? = null

                    if(uri != null)
                        fileStream = activity?.contentResolver?.openInputStream(uri);

                    if(uri != null && BTService.getState() == STATE_CONNECTED) {
                        Toast.makeText(activity, "Upload started", Toast.LENGTH_SHORT).show()
                        mConversationArrayAdapter?.add("Upload started.")
                        //BTService.upload(fileStream)
                    }
                }*/
            }
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

    fun ByteArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                MESSAGE_STATE_CHANGE.toString() -> {
                    when (intent.getIntExtra("newState", -1)) {
                        STATE_CONNECTED -> {
                            val cDevice = intent.getStringExtra("cDevice")
                            mConnectedDeviceName = cDevice
                        }
                    }
                }

                MESSAGE_WRITE.toString() -> {
                    val writeBuff = intent.getByteArrayExtra("writeBuffer")

                    // construct a string from the buffer
                    if(writeBuff != null) {
                        val writeString = String(writeBuff)

                        mConversationArrayAdapter?.add("Me:  $writeString")
                        val btMessage = view?.findViewById<ListView>(R.id.bt_message)!!
                        btMessage.setSelection(btMessage.adapter.count - 1);
                    }
                }

                MESSAGE_READ.toString() -> {
                    val readBuff = intent.getByteArrayExtra("readBuffer")

                    // construct a string from the valid bytes in the buffer
                    if(readBuff != null) {
                        mConversationArrayAdapter?.add(mConnectedDeviceName.toString() + ":  $readBuff")

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