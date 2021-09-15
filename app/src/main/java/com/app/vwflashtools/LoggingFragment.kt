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
                    if(!bleHeader.isValid()) {
                        val textViewPackCount = view?.findViewById<TextView>(R.id.textViewPackCount)!!
                        textViewPackCount.text = "Invalid Header " + bleHeader.hdID.toString()
                        textViewPackCount.setTextColor(Color.RED)
                    }

                    if(buff.size != 39) {
                        val textViewPackCount = view?.findViewById<TextView>(R.id.textViewPackCount)!!
                        textViewPackCount.text = "Invalid buffer size " + buff.size.toString()
                        textViewPackCount.setTextColor(Color.RED)
                    }

                    val bData = Arrays.copyOfRange(buff, 8, buff.size);
                    if(bData[0].toInt() == 0x62) {
                        val newPIDS = FloatArray(8)
                        newPIDS[0] = (((bData[3] and 0xFF) shl 8) + (bData[4] and 0xFF)).toFloat() / 4.0f
                        newPIDS[1] = (((bData[7] and 0xFF) shl 8) + (bData[8] and 0xFF)).toFloat() / 1000.0f
                        newPIDS[2] = (((bData[11] and 0xFF) shl 8) + (bData[12] and 0xFF)).toFloat() / 100.0f
                        newPIDS[3] = (((bData[15] and 0xFF) shl 8) + (bData[16] and 0xFF)).toFloat() * 0.0078125f
                        newPIDS[4] = (bData[19] and 0xFF).toFloat() * 0.75f - 48.0f
                        newPIDS[5] = (((bData[22] and 0xFF) shl 8) + (bData[23] and 0xFF)).toFloat() * 6.103515f
                        newPIDS[6] = (bData[26] and 0xFF).toFloat() / 1.28f - 100.0f
                        newPIDS[7] = (((bData[29] and 0xFF) shl 8) + (bData[30] and 0xFF)).toFloat()

                        view?.findViewById<TextView>(R.id.textViewPID1)!!.text = getString(R.string.textPID1, newPIDS[0].toString())
                        view?.findViewById<TextView>(R.id.textViewPID2)!!.text = getString(R.string.textPID2, newPIDS[1].toString())
                        view?.findViewById<TextView>(R.id.textViewPID3)!!.text = getString(R.string.textPID3, newPIDS[2].toString())
                        view?.findViewById<TextView>(R.id.textViewPID4)!!.text = getString(R.string.textPID4, newPIDS[3].toString())
                        view?.findViewById<TextView>(R.id.textViewPID5)!!.text = getString(R.string.textPID5, newPIDS[4].toString())
                        view?.findViewById<TextView>(R.id.textViewPID6)!!.text = getString(R.string.textPID6, newPIDS[5].toString())
                        view?.findViewById<TextView>(R.id.textViewPID7)!!.text = getString(R.string.textPID7, newPIDS[6].toString())
                        view?.findViewById<TextView>(R.id.textViewPID8)!!.text = getString(R.string.textPID8, newPIDS[7].toString())

                        view?.findViewById<ProgressBar>(R.id.progressBar1)!!.progress = newPIDS[0].toInt()
                        view?.findViewById<ProgressBar>(R.id.progressBar2)!!.progress = (newPIDS[1]*1000.0f).toInt()
                        view?.findViewById<ProgressBar>(R.id.progressBar3)!!.progress = newPIDS[2].toInt()
                        view?.findViewById<ProgressBar>(R.id.progressBar4)!!.progress = newPIDS[3].toInt()
                        view?.findViewById<ProgressBar>(R.id.progressBar5)!!.progress = newPIDS[4].toInt()
                        view?.findViewById<ProgressBar>(R.id.progressBar6)!!.progress = newPIDS[5].toInt()
                        view?.findViewById<ProgressBar>(R.id.progressBar7)!!.progress = newPIDS[6].toInt()

                        if(newPIDS[7] != 0.0.toFloat()) {
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
                            LogFile.add("${actualTime},${newPIDS[0]},${newPIDS[1]},${newPIDS[2]},${newPIDS[3]},${newPIDS[4]},${newPIDS[5]},${newPIDS[6]}")
                        } else {
                            if(mLastState == 1) {
                                LogFile.close()
                            }
                            mLastState = 0
                            val textViewPackCount = view?.findViewById<TextView>(R.id.textViewPackCount)!!
                            textViewPackCount.text = logCount.toString()
                            textViewPackCount.setTextColor(Color.BLACK)
                        }
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