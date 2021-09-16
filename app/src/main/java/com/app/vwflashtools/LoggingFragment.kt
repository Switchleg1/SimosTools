package com.app.vwflashtools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class LoggingFragment : Fragment() {
    var mLastState: Int = 0
    var mLogCurrentTime: Int = 0
    var mLogLastTime: Int = 0

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

        view.findViewById<Button>(R.id.buttonStartLog).setOnClickListener {
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_DO_CHECK_PID.toString()
            startForegroundService(this.requireContext(), serviceIntent)
        }

        view.findViewById<Button>(R.id.buttonStopLog).setOnClickListener {
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_DO_STOP_PID.toString()
            startForegroundService(this.requireContext(), serviceIntent)
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(MESSAGE_READ_VIN.toString())
        filter.addAction(MESSAGE_READ_LOG.toString())
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
                MESSAGE_READ_VIN.toString() -> {
                    val buff = intent.getByteArrayExtra("readBuffer")
                    view?.findViewById<TextView>(R.id.textViewPID1)!!.text = getString(R.string.textVIN, buff.toString())
                }
                MESSAGE_READ_LOG.toString() -> {
                    val buff = intent.getByteArrayExtra("readBuffer")
                    val logCount = intent.getIntExtra("readCount", 0)

                    if(buff == null)
                        return

                    val bleHeader = BLEHeader()
                    bleHeader.fromByteArray(buff)
                    val bData = Arrays.copyOfRange(buff, 8, buff.size);
                    if(!bleHeader.isValid() || bleHeader.cmdSize != bData.size || bData[0] != 0x62.toByte() || bData[1] != 0x22.toByte()) {
                        val textViewPackCount = view?.findViewById<TextView>(R.id.textViewPackCount)!!
                        textViewPackCount.text = "Invalid Header ${bleHeader.hdID} [${bleHeader.cmdSize}:${bData.size}] ${bData[0]} ${bData[1]}"
                        textViewPackCount.setTextColor(Color.RED)
                        return
                    }

                    var i = 2
                    val newPIDS = FloatArray(8)
                    val newStrs = arrayOfNulls<String>(8)
                    while(i < bleHeader.cmdSize) {
                        val did: DIDStruct? = DIDClass.getDID(((bData[i] and 0xFF) shl 8) + (bData[i+1] and 0xFF))
                        if(did == null) {
                            val textViewPackCount = view?.findViewById<TextView>(R.id.textViewPackCount)!!
                            textViewPackCount.text = "Invalid DID " + (((bData[i] and 0xFF) shl 8) + (bData[i+1] and 0xFF))
                            textViewPackCount.setTextColor(Color.RED)
                            i = bleHeader.cmdSize
                            break
                        }
                        var f = 0f
                        if(did.length == 1) {
                            f = (bData[i+2] and 0xFF).toFloat()
                            i += 3
                        } else if(did.length == 2) {
                            f = ((bData[i+2] and 0xFF) shl 8 + (bData[i+3] and 0xFF)).toFloat()
                            i += 4
                        }
                        newPIDS[i] = DIDClass.getValue(did, f)
                        newStrs[i] = "${did.name}: $f ${did.unit}"
                    }

                    view?.findViewById<TextView>(R.id.textViewPID1)!!.text = newStrs[0]
                    view?.findViewById<TextView>(R.id.textViewPID2)!!.text = newStrs[1]
                    view?.findViewById<TextView>(R.id.textViewPID3)!!.text = newStrs[2]
                    view?.findViewById<TextView>(R.id.textViewPID4)!!.text = newStrs[3]
                    view?.findViewById<TextView>(R.id.textViewPID5)!!.text = newStrs[4]
                    view?.findViewById<TextView>(R.id.textViewPID6)!!.text = newStrs[5]
                    view?.findViewById<TextView>(R.id.textViewPID7)!!.text = newStrs[6]
                    view?.findViewById<TextView>(R.id.textViewPID8)!!.text = newStrs[7]

                    view?.findViewById<ProgressBar>(R.id.progressBar1)!!.progress = newPIDS[0].toInt()
                    view?.findViewById<ProgressBar>(R.id.progressBar2)!!.progress = newPIDS[1].toInt()
                    view?.findViewById<ProgressBar>(R.id.progressBar3)!!.progress = newPIDS[2].toInt()
                    view?.findViewById<ProgressBar>(R.id.progressBar4)!!.progress = newPIDS[3].toInt()
                    view?.findViewById<ProgressBar>(R.id.progressBar5)!!.progress = newPIDS[4].toInt()
                    view?.findViewById<ProgressBar>(R.id.progressBar6)!!.progress = newPIDS[5].toInt()
                    view?.findViewById<ProgressBar>(R.id.progressBar7)!!.progress = newPIDS[6].toInt()

                    /*if(newPIDS[7] != 0.0.toFloat()) {
                        if(mLastState == 0) {
                            val currentDateTime = LocalDateTime.now()

                            LogFile.create("vwflashtools-${currentDateTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss"))}.csv", context)
                            LogFile.add("Time,Engine Speed,MAP,Ignition Timing Average,Vehicle Speed,IAT,Turbo,STFT")
                        }
                        mLastState = 1
                        val textViewPackCount = view?.findViewById<TextView>(R.id.textViewPackCount)!!
                        textViewPackCount.text = logCount.toString()
                        textViewPackCount.setTextColor(Color.RED)

                        val actualTime = (bleHeader.tickCount).toFloat() / 1000.0f
                        LogFile.add("${actualTime},${newPIDS[0]},${newPIDS[1]},${newPIDS[2]},${newPIDS[3]},${newPIDS[4]},${newPIDS[5]},${newPIDS[6]},${newPIDS[7]}")
                    } else {
                        if(mLastState == 1) {
                            LogFile.close()
                        }
                        mLastState = 0
                        val textViewPackCount = view?.findViewById<TextView>(R.id.textViewPackCount)!!
                        textViewPackCount.text = logCount.toString()
                        textViewPackCount.setTextColor(Color.BLACK)
                    }*/
                }
                MESSAGE_TOAST.toString() -> {
                    val nToast = intent.getStringExtra("newToast")
                    Toast.makeText(activity, nToast, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}