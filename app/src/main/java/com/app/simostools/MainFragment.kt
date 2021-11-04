package com.app.simostools

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class MainFragment : Fragment() {
    private val TAG = "MainFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loggingButton = view.findViewById<SwitchButton>(R.id.buttonMainLogging)
        loggingButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                sendServiceMessage(BTServiceTask.DO_START_LOG.toString())
                findNavController().navigate(R.id.action_MainFragment_to_LoggingFragment)
            }
        }

        val flashingButton = view.findViewById<SwitchButton>(R.id.buttonMainFlashing)
        flashingButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                findNavController().navigate(R.id.action_MainFragment_to_FlashingFragment)
            }
        }

        val settingsButton = view.findViewById<SwitchButton>(R.id.buttonMainSettings)
        settingsButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                ColorSettings.resetColors()
                findNavController().navigate(R.id.action_MainFragment_to_SettingsFragment)
            }
        }

        val exitButton = view.findViewById<SwitchButton>(R.id.buttonMainExit)
        exitButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                //Write pid default files
                UDSLoggingMode.values().forEach { mode ->
                    //write current PID list
                    PIDCSVFile.write(
                        getString(R.string.filename_pid_csv, mode.cfgName),
                        requireActivity(),
                        PIDs.getList(mode),
                        true
                    )
                }

                //Stop our BT Service
                val serviceIntent = Intent(requireActivity(), BTService::class.java)
                serviceIntent.action = BTServiceTask.STOP_SERVICE.toString()
                ContextCompat.startForegroundService(requireActivity(), serviceIntent)

                requireActivity().finish()
            }
        }

        //Set background color
        view.setBackgroundColor(ColorList.BG_NORMAL.value)
        view.findViewById<ImageView>(R.id.imageMainLogo).setBackgroundColor(ColorList.BG_NORMAL.value)
    }

    override fun onResume() {
        super.onResume()

        sendServiceMessage(BTServiceTask.DO_STOP_TASK.toString())
    }

    private fun sendServiceMessage(type: String) {
        val serviceIntent = Intent(requireActivity(), BTService::class.java)
        serviceIntent.action = type
        ContextCompat.startForegroundService(requireActivity(), serviceIntent)
    }
}