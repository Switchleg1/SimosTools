package com.app.simostools

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        view.findViewById<Button>(R.id.buttonMainLogging).setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_LoggingFragment)
        }

        view.findViewById<Button>(R.id.buttonMainFlashing).setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_FlashingFragment)
        }

        view.findViewById<Button>(R.id.buttonMainSettings).setOnClickListener {
            ColorSettings.resetColors()
            findNavController().navigate(R.id.action_MainFragment_to_SettingsFragment)
        }

        view.findViewById<Button>(R.id.buttonMainExit).setOnClickListener {
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

        view.findViewById<ImageView>(R.id.imageMainLogo).setBackgroundColor(ColorList.BG_NORMAL.value)

        //Set background color
        view.setBackgroundColor(ColorList.BG_NORMAL.value)
    }
}