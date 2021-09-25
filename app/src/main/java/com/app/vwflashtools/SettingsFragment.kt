package com.app.vwflashtools

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            findNavController().navigate(R.id.action_SettingsFragment_to_FirstFragment)
        }

        view.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            if(view.findViewById<RadioButton>(R.id.radioButton3E).isChecked) {
                ConfigFile.set("Config.Mode", "3E")
            } else {
                ConfigFile.set("Config.Mode", "22")
            }

            //Stop logging
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_DO_STOP_PID.toString()
            ContextCompat.startForegroundService(this.requireContext(), serviceIntent)

            ConfigFile.write(LOG_FILENAME, context)
            ConfigFile.read(LOG_FILENAME, context)
        }

        if(UDSLogger.getMode() == UDS_LOGGING_3E) {
            view.findViewById<RadioButton>(R.id.radioButton3E).isChecked = true
        } else {
            view.findViewById<RadioButton>(R.id.radioButton22).isChecked = true
        }
    }
}