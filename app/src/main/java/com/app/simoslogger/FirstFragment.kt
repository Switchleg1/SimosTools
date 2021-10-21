package com.app.simoslogger

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.fragment.findNavController

class FirstFragment : Fragment() {
    private val TAG = "FirstFragment"
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.buttonFlashing).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_FlashFragment)
        }

        view.findViewById<Button>(R.id.buttonLogging).setOnClickListener {
            //Start Logging
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BTServiceTask.DO_START_LOG.toString()
            startForegroundService(this.requireContext(), serviceIntent)

            findNavController().navigate(R.id.action_FirstFragment_to_LogFragment)
        }

        view.findViewById<Button>(R.id.buttonSettings).setOnClickListener {
            //Reset colors
            ColorSettings.resetColors()

            //Navigate to settings fragment
            findNavController().navigate(R.id.action_FirstFragment_to_SettingsFragment)
        }

        view.findViewById<Button>(R.id.buttonStopService).setOnClickListener {
            //shutdown service and close app
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BTServiceTask.STOP_SERVICE.toString()
            startForegroundService(requireContext(), serviceIntent)
            requireActivity().finish()
        }

        //Set background color
        view.setBackgroundColor(ColorList.BG_NORMAL.value)
    }
}