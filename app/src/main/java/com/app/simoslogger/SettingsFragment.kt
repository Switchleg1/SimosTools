package com.app.simoslogger

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingsViewModel : ViewModel() {
    var pidList = PID_LIST_A
    var logMode = UDS_LOGGING_22
    var CSVFileName = ""
}

class SettingsFragment : Fragment() {
    private val TAG = "Settings"
    private lateinit var mViewModel: SettingsViewModel

    var resultPickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                val mask = if(mViewModel.logMode == UDS_LOGGING_22) CSV_22_MASK
                    else CSV_3E_MASK
                val pidList = PIDCSVFile.readStream(activity?.contentResolver?.openInputStream(uri), mask)
                if(pidList != null) {
                    if(PIDCSVFile.write(mViewModel.CSVFileName, context, pidList, true)) {
                        PIDs.setList(mViewModel.logMode, mViewModel.pidList, pidList)

                        Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        view.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            doSave()
        }

        view.findViewById<Button>(R.id.button22ACSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 22 A CSV")
            mViewModel.logMode = UDS_LOGGING_22
            mViewModel.pidList = PID_LIST_A
            mViewModel.CSVFileName = getString(R.string.filename_22_csv, "a")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button22BCSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 22 B CSV")
            mViewModel.logMode = UDS_LOGGING_22
            mViewModel.pidList = PID_LIST_B
            mViewModel.CSVFileName = getString(R.string.filename_22_csv, "b")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button22CCSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 22 C CSV")
            mViewModel.logMode = UDS_LOGGING_22
            mViewModel.pidList = PID_LIST_C
            mViewModel.CSVFileName = getString(R.string.filename_22_csv, "c")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button3EACSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 3E A CSV")
            mViewModel.logMode = UDS_LOGGING_3E
            mViewModel.pidList = PID_LIST_A
            mViewModel.CSVFileName = getString(R.string.filename_3E_csv, "a")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button3EBCSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 3E B CSV")
            mViewModel.logMode = UDS_LOGGING_3E
            mViewModel.pidList = PID_LIST_B
            mViewModel.CSVFileName = getString(R.string.filename_3E_csv, "b")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button3ECCSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 3E C CSV")
            mViewModel.logMode = UDS_LOGGING_3E
            mViewModel.pidList = PID_LIST_C
            mViewModel.CSVFileName = getString(R.string.filename_3E_csv, "c")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.buttonSetBGNormalColor).setOnClickListener {
            ColorSettings.getColor(COLOR_BG_NORMAL)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBGWarningColor).setOnClickListener {
            ColorSettings.getColor(COLOR_BG_WARN)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetTextColor).setOnClickListener {
            ColorSettings.getColor(COLOR_TEXT)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBarNormalColor).setOnClickListener {
            ColorSettings.getColor(COLOR_BAR_NORMAL)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBarWarnColor).setOnClickListener {
            ColorSettings.getColor(COLOR_BAR_WARN)
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

        doShow()
    }

    private fun doSetColor() {
        view?.let { currentView ->
            //Set font color
            currentView.findViewById<TextView>(R.id.textViewDisplaySize).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewUpdateRate).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewPersistDelay).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewPersistQDelay).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewOutputDirectory).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewLoggingMode).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewLoggingList).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewPIDCSV).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewMiscOptions).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewCalcHPOptions).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<TextView>(R.id.textViewColorOptions).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<RadioButton>(R.id.radioButtonApplication).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<RadioButton>(R.id.radioButton3E).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<RadioButton>(R.id.radioButton22).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<RadioButton>(R.id.radioButtonA).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<RadioButton>(R.id.radioButtonB).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<RadioButton>(R.id.radioButtonC).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<CheckBox>(R.id.checkBoxInvertCruise).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<CheckBox>(R.id.checkBoxScreenOn).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<CheckBox>(R.id.checkBoxAlwaysPortrait).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<CheckBox>(R.id.checkBoxCalcHP).setTextColor(Settings.colorList[COLOR_TEXT])
            currentView.findViewById<CheckBox>(R.id.checkBoxUseAccel).setTextColor(Settings.colorList[COLOR_TEXT])

            //Set color boxes
            currentView.findViewById<Button>(R.id.buttonSetBGNormalColor).setTextColor(ColorSettings.mColorList[COLOR_BG_NORMAL].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBGNormalColor).setBackgroundColor(ColorSettings.mColorList[COLOR_BG_NORMAL])
            currentView.findViewById<Button>(R.id.buttonSetBGWarningColor).setTextColor(ColorSettings.mColorList[COLOR_BG_WARN].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBGWarningColor).setBackgroundColor(ColorSettings.mColorList[COLOR_BG_WARN])
            currentView.findViewById<Button>(R.id.buttonSetTextColor).setTextColor(ColorSettings.mColorList[COLOR_TEXT].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetTextColor).setBackgroundColor(ColorSettings.mColorList[COLOR_TEXT])
            currentView.findViewById<Button>(R.id.buttonSetBarNormalColor).setTextColor(ColorSettings.mColorList[COLOR_BAR_NORMAL].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBarNormalColor).setBackgroundColor(ColorSettings.mColorList[COLOR_BAR_NORMAL])
            currentView.findViewById<Button>(R.id.buttonSetBarWarnColor).setTextColor(ColorSettings.mColorList[COLOR_BAR_WARN].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBarWarnColor).setBackgroundColor(ColorSettings.mColorList[COLOR_BAR_WARN])
            currentView.findViewById<Button>(R.id.buttonSetErrorColor).setTextColor(ColorSettings.mColorList[COLOR_ST_ERROR].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetErrorColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_ERROR])
            currentView.findViewById<Button>(R.id.buttonSetNoneColor).setTextColor(ColorSettings.mColorList[COLOR_ST_NONE].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetNoneColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_NONE])
            currentView.findViewById<Button>(R.id.buttonSetConnectingColor).setTextColor(ColorSettings.mColorList[COLOR_ST_CONNECTING].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetConnectingColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_CONNECTING])
            currentView.findViewById<Button>(R.id.buttonSetConnectedColor).setTextColor(ColorSettings.mColorList[COLOR_ST_CONNECTED].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetConnectedColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_CONNECTED])
            currentView.findViewById<Button>(R.id.buttonSetLoggingColor).setTextColor(ColorSettings.mColorList[COLOR_ST_LOGGING].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetLoggingColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_LOGGING])
            currentView.findViewById<Button>(R.id.buttonSetWritingColor).setTextColor(ColorSettings.mColorList[COLOR_ST_WRITING].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetWritingColor).setBackgroundColor(ColorSettings.mColorList[COLOR_ST_WRITING])

            //Set background color
            currentView.setBackgroundColor(Settings.colorList[COLOR_BG_NORMAL])
        }
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
            when(UDSLogger.getMode()) {
                UDS_LOGGING_22 -> currentView.findViewById<RadioButton>(R.id.radioButton22).isChecked = true
                else -> currentView.findViewById<RadioButton>(R.id.radioButton3E).isChecked = true
            }

            //Get list index
            when(PIDs.getIndex()) {
                PID_LIST_A -> currentView.findViewById<RadioButton>(R.id.radioButtonA).isChecked = true
                PID_LIST_B -> currentView.findViewById<RadioButton>(R.id.radioButtonB).isChecked = true
                else -> currentView.findViewById<RadioButton>(R.id.radioButtonC).isChecked = true
            }

            //Get output directory
            when (Settings.outputDirectory) {
                Environment.DIRECTORY_DOWNLOADS -> currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isChecked = true
                Environment.DIRECTORY_DOCUMENTS -> currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isChecked = true
                else -> currentView.findViewById<RadioButton>(R.id.radioButtonApplication).isChecked = true
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

            //Set colors
            doSetColor()
        }
    }

    private fun doSave() {
        view?.let { currentView ->
            // Set display size
            val f = currentView.findViewById<SeekBar>(R.id.seekBarDisplaySize).progress.toFloat()
            ConfigFile.set("DisplaySize", (f / 100f).toString())

            // Set update rate
            ConfigFile.set("UpdateRate", currentView.findViewById<SeekBar>(R.id.seekBarUpdateRate).progress.toString())

            // Set persist delay
            ConfigFile.set("PersistDelay", currentView.findViewById<SeekBar>(R.id.seekBarPersistDelay).progress.toString())

            // Set persist delay
            ConfigFile.set("PersistQDelay", currentView.findViewById<SeekBar>(R.id.seekBarPersistQDelay).progress.toString())

            // Set logging mode
            when(currentView.findViewById<RadioButton>(R.id.radioButton22).isChecked) {
                true -> ConfigFile.set("Mode", "22")
                false -> ConfigFile.set("Mode", "3E")
            }

            // Set list index
            when {
                currentView.findViewById<RadioButton>(R.id.radioButtonA).isChecked -> ConfigFile.set("List", "A")
                currentView.findViewById<RadioButton>(R.id.radioButtonB).isChecked -> ConfigFile.set("List", "B")
                else -> ConfigFile.set("List", "C")
            }

            // Set default output folder
            when {
                currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isChecked -> ConfigFile.set("OutputDirectory", "Downloads")
                currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isChecked -> ConfigFile.set("OutputDirectory", "Documents")
                else -> ConfigFile.set("OutputDirectory", "App")
            }

            //Set invert cruise
            ConfigFile.set("InvertCruise", currentView.findViewById<CheckBox>(R.id.checkBoxInvertCruise).isChecked.toString())

            //Set screen on
            ConfigFile.set("KeepScreenOn", currentView.findViewById<CheckBox>(R.id.checkBoxScreenOn).isChecked.toString())

            //Calculate HP
            ConfigFile.set("CalculateHP", currentView.findViewById<CheckBox>(R.id.checkBoxCalcHP).isChecked.toString())

            //Calculate HP
            ConfigFile.set("UseMS2Torque", currentView.findViewById<CheckBox>(R.id.checkBoxUseAccel).isChecked.toString())

            //Always use portrait view
            ConfigFile.set("AlwaysPortrait", currentView.findViewById<CheckBox>(R.id.checkBoxAlwaysPortrait).isChecked.toString())

            //Set Colors
            ConfigFile.set("ColorBGNormal", ColorSettings.mColorList[COLOR_BG_NORMAL].toColorHex())
            ConfigFile.set("ColorBGWarn", ColorSettings.mColorList[COLOR_BG_WARN].toColorHex())
            ConfigFile.set("ColorText", ColorSettings.mColorList[COLOR_TEXT].toColorHex())
            ConfigFile.set("ColorBarNormal", ColorSettings.mColorList[COLOR_BAR_NORMAL].toColorHex())
            ConfigFile.set("ColorBarWarn", ColorSettings.mColorList[COLOR_BAR_WARN].toColorHex())
            ConfigFile.set("ColorStateError", ColorSettings.mColorList[COLOR_ST_ERROR].toColorHex())
            ConfigFile.set("ColorStateNone", ColorSettings.mColorList[COLOR_ST_NONE].toColorHex())
            ConfigFile.set("ColorStateConnecting", ColorSettings.mColorList[COLOR_ST_CONNECTING].toColorHex())
            ConfigFile.set("ColorStateConnected", ColorSettings.mColorList[COLOR_ST_CONNECTED].toColorHex())
            ConfigFile.set("ColorStateLogging", ColorSettings.mColorList[COLOR_ST_LOGGING].toColorHex())
            ConfigFile.set("ColorStateWriting", ColorSettings.mColorList[COLOR_ST_WRITING].toColorHex())

            //Stop logging
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_DO_STOP_PID.toString()
            ContextCompat.startForegroundService(this.requireContext(), serviceIntent)

            // Write config
            ConfigFile.write(CFG_FILENAME, context)
            ConfigFile.read(CFG_FILENAME, context)

            //Reset colors
            ColorSettings.resetColors()

            //Set colors
            doSetColor()
        }
    }
}