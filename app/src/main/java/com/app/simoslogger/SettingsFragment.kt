package com.app.simoslogger

import android.content.Intent
import android.graphics.Color
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
            findNavController().navigateUp()
        }

        view.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            doSave()
        }

        view.findViewById<Button>(R.id.buttonSetNormalColor).setOnClickListener {
            ColorSettings.getColor(COLOR_NORMAL)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetWarningColor).setOnClickListener {
            ColorSettings.getColor(COLOR_WARNING)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetTextColor).setOnClickListener {
            ColorSettings.getColor(COLOR_TEXT)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBarColor).setOnClickListener {
            ColorSettings.getColor(COLOR_BAR)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetErrorColor).setOnClickListener {
            ColorSettings.getColor(COLOR_ST_ERROR)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetNoneColor).setOnClickListener {
            ColorSettings.getColor(COLOR_ST_NONE)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetConnectingColor).setOnClickListener {
            ColorSettings.getColor(COLOR_ST_CONNECTING)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetConnectedColor).setOnClickListener {
            ColorSettings.getColor(COLOR_ST_CONNECTED)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetLoggingColor).setOnClickListener {
            ColorSettings.getColor(COLOR_ST_LOGGING)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetWritingColor).setOnClickListener {
            ColorSettings.getColor(COLOR_ST_WRITING)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<SeekBar>(R.id.seekBarDisplaySize).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewDisplaySize).text = getString(R.string.textview_settings_displaysize, view.findViewById<SeekBar>(R.id.seekBarDisplaySize).progress)
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarUpdateRate).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewUpdateRate).text = getString(R.string.textview_settings_updaterate, view.findViewById<SeekBar>(R.id.seekBarUpdateRate).progress)
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarPersistDelay).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewPersistDelay).text = getString(R.string.textview_settings_persist_delay, view.findViewById<SeekBar>(R.id.seekBarPersistDelay).progress)
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarPersistQDelay).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewPersistQDelay).text = getString(R.string.textview_settings_persist_q_delay, view.findViewById<SeekBar>(R.id.seekBarPersistQDelay).progress)
            }
        })

        //Set background color
        view.setBackgroundColor(Settings.colorList[COLOR_NORMAL])

        doShow()
    }

    private fun doShow() {
        view?.let { currentView ->
            //Get display size
            currentView.findViewById<SeekBar>(R.id.seekBarDisplaySize)?.let { displaySize ->
                displaySize.min = 1
                displaySize.max = 200
                displaySize.progress = (Settings.displaySize * 100f).toInt()
                displaySize.callOnClick()
            }

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

            //Get calculate HP
            currentView.findViewById<CheckBox>(R.id.checkBoxCalcHP).isChecked = Settings.calculateHP

            //Get use MS2
            currentView.findViewById<CheckBox>(R.id.checkBoxUseAccel).isChecked = Settings.useMS2Torque

            //Get always use portrait
            currentView.findViewById<CheckBox>(R.id.checkBoxAlwaysPortrait).isChecked = Settings.alwaysPortrait

            //Set Colors
            currentView.findViewById<Button>(R.id.buttonSetNormalColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_NORMAL] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetNormalColor).setBackgroundColor(ColorSettings.mColorList[COLOR_NORMAL])
            currentView.findViewById<Button>(R.id.buttonSetWarningColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_WARNING] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetWarningColor).setBackgroundColor(ColorSettings.mColorList[COLOR_WARNING])
            currentView.findViewById<Button>(R.id.buttonSetTextColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_TEXT] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetTextColor).setBackgroundColor(ColorSettings.mColorList[COLOR_TEXT])
            currentView.findViewById<Button>(R.id.buttonSetBarColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_BAR] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetBarColor).setBackgroundColor(ColorSettings.mColorList[COLOR_BAR])
            currentView.findViewById<Button>(R.id.buttonSetErrorColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_ST_ERROR] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetErrorColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_ERROR])
            currentView.findViewById<Button>(R.id.buttonSetNoneColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_ST_NONE] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetNoneColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_NONE])
            currentView.findViewById<Button>(R.id.buttonSetConnectingColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_ST_CONNECTING] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetConnectingColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_CONNECTING])
            currentView.findViewById<Button>(R.id.buttonSetConnectedColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_ST_CONNECTED] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetConnectedColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_CONNECTED])
            currentView.findViewById<Button>(R.id.buttonSetLoggingColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_ST_LOGGING] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetLoggingColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_LOGGING])
            currentView.findViewById<Button>(R.id.buttonSetWritingColor).setTextColor(Color.WHITE xor ColorSettings.mColorList[COLOR_ST_WRITING] or 0xFF000000.toInt())
            currentView.findViewById<Button>(R.id.buttonSetWritingColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_WRITING])
        }
    }

    private fun doSave() {
        view?.let { currentView ->
            // Set display size
            val f = currentView.findViewById<SeekBar>(R.id.seekBarDisplaySize).progress.toFloat()
            ConfigFile.set("Config.DisplaySize", (f / 100f).toString())

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

            //Calculate HP
            ConfigFile.set("Config.CalculateHP", currentView.findViewById<CheckBox>(R.id.checkBoxCalcHP).isChecked.toString())

            //Calculate HP
            ConfigFile.set("Config.UseMS2Torque", currentView.findViewById<CheckBox>(R.id.checkBoxUseAccel).isChecked.toString())

            //Always use portrait view
            ConfigFile.set("Config.AlwaysPortrait", currentView.findViewById<CheckBox>(R.id.checkBoxAlwaysPortrait).isChecked.toString())

            //Set Colors
            ConfigFile.set("Config.ColorNormal", ColorSettings.mColorList[COLOR_NORMAL].toHex())
            ConfigFile.set("Config.ColorWarn", ColorSettings.mColorList[COLOR_WARNING].toHex())
            ConfigFile.set("Config.ColorText", ColorSettings.mColorList[COLOR_TEXT].toHex())
            ConfigFile.set("Config.ColorBar", ColorSettings.mColorList[COLOR_BAR].toHex())
            ConfigFile.set("Config.ColorStateError", ColorSettings.mColorList[COLOR_ST_ERROR].toHex())
            ConfigFile.set("Config.ColorStateNone", ColorSettings.mColorList[COLOR_ST_NONE].toHex())
            ConfigFile.set("Config.ColorStateConnecting", ColorSettings.mColorList[COLOR_ST_CONNECTING].toHex())
            ConfigFile.set("Config.ColorStateConnected", ColorSettings.mColorList[COLOR_ST_CONNECTED].toHex())
            ConfigFile.set("Config.ColorStateLogging", ColorSettings.mColorList[COLOR_ST_LOGGING].toHex())
            ConfigFile.set("Config.ColorStateWriting", ColorSettings.mColorList[COLOR_ST_WRITING].toHex())

            //Stop logging
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_DO_STOP_PID.toString()
            ContextCompat.startForegroundService(this.requireContext(), serviceIntent)

            // Write config
            ConfigFile.write(LOG_FILENAME, context)
            ConfigFile.read(LOG_FILENAME, context)

            //Set background color
            currentView.setBackgroundColor(Settings.colorList[COLOR_NORMAL])
        }
    }
}