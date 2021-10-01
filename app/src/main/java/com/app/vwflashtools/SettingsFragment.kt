package com.app.vwflashtools

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import android.widget.SeekBar.OnSeekBarChangeListener


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
            doSave()
        }

        view.findViewById<SeekBar>(R.id.seekBarUpdateRate).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewUpdateRate).text = getString(R.string.textview_settings_updaterate, view.findViewById<SeekBar>(R.id.seekBarUpdateRate).progress)
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarPersistDelay).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewPersistDelay).text = getString(R.string.textview_settings_persist_delay, view.findViewById<SeekBar>(R.id.seekBarPersistDelay).progress)
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarPersistQDelay).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewPersistQDelay).text = getString(R.string.textview_settings_persist_q_delay, view.findViewById<SeekBar>(R.id.seekBarPersistQDelay).progress)
            }
        })

        doShow()
    }

    private fun doShow() {
        view?.let { currentView ->
            //Get update rate
            currentView.findViewById<SeekBar>(R.id.seekBarUpdateRate)?.let { updateRate ->
                updateRate.min = 1
                updateRate.max = 10
                updateRate.progress = 11 - Settings.updateRate
                updateRate.callOnClick()
            }

            //Get persist delay
            currentView.findViewById<SeekBar>(R.id.seekBarPersistDelay)?.let { persistDelay ->
                persistDelay.min = 1
                persistDelay.max = 50
                persistDelay.progress = Settings.persistDelay
                persistDelay.callOnClick()
            }

            //Get persist queue delay
            currentView.findViewById<SeekBar>(R.id.seekBarPersistQDelay)?.let { persistQDelay ->
                persistQDelay.min = 1
                persistQDelay.max = 25
                persistQDelay.progress = Settings.persistQDelay
                persistQDelay.callOnClick()
            }

            //Get logging mode
            if (UDSLogger.getMode() == UDS_LOGGING_3E) {
                currentView.findViewById<RadioButton>(R.id.radioButton3E).isChecked = true
            } else {
                currentView.findViewById<RadioButton>(R.id.radioButton22).isChecked = true
            }

            //Get output directory
            when (Settings.outputDirectory) {
                Environment.DIRECTORY_DOWNLOADS -> {
                    currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isChecked = true
                }
                Environment.DIRECTORY_DOCUMENTS -> {
                    currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isChecked = true
                }
                else -> {
                    currentView.findViewById<RadioButton>(R.id.radioButtonApplication).isChecked = true
                }
            }

            //Get cruise invert
            currentView.findViewById<CheckBox>(R.id.checkBoxInvertCruise).isChecked = Settings.invertCruise

            //Get keep screen on
            currentView.findViewById<CheckBox>(R.id.checkBoxScreenOn).isChecked = Settings.keepScreenOn
        }
    }

    private fun doSave() {
        view?.let { currentView ->
            // Set update rate
            ConfigFile.set("Config.UpdateRate", currentView.findViewById<SeekBar>(R.id.seekBarUpdateRate).progress.toString())

            // Set persist delay
            ConfigFile.set("Config.PersistDelay", currentView.findViewById<SeekBar>(R.id.seekBarPersistDelay).progress.toString())

            // Set persist delay
            ConfigFile.set("Config.PersistQDelay", currentView.findViewById<SeekBar>(R.id.seekBarPersistQDelay).progress.toString())

            // Set logging mode
            if(currentView.findViewById<RadioButton>(R.id.radioButton3E).isChecked) {
                ConfigFile.set("Config.Mode", "3E")
            } else {
                ConfigFile.set("Config.Mode", "22")
            }

            // Set default output folder
            when {
                currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isChecked -> {
                    ConfigFile.set("Config.OutputDirectory", "Downloads")
                }
                currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isChecked -> {
                    ConfigFile.set("Config.OutputDirectory", "Documents")
                }
                else -> {
                    ConfigFile.set("Config.OutputDirectory", "App")
                }
            }

            //Set invert cruise
            ConfigFile.set("Config.InvertCruise", currentView.findViewById<CheckBox>(R.id.checkBoxInvertCruise).isChecked.toString())

            //Set screen on
            ConfigFile.set("Config.KeepScreenOn", currentView.findViewById<CheckBox>(R.id.checkBoxScreenOn).isChecked.toString())

            //Stop logging
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_DO_STOP_PID.toString()
            ContextCompat.startForegroundService(this.requireContext(), serviceIntent)

            // Write config
            ConfigFile.write(LOG_FILENAME, context)
            ConfigFile.read(LOG_FILENAME, context)
        }
    }
}