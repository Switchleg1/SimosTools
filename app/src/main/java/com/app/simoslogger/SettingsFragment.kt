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
    var pidList = PIDIndex.A
    var logMode = UDSLoggingMode.MODE_22
    var CSVFileName = ""
}

class SettingsFragment : Fragment() {
    private val TAG = "Settings"
    private lateinit var mViewModel: SettingsViewModel

    var resultPickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                val addMin = if(mViewModel.logMode == UDSLoggingMode.MODE_22) CSV_22_ADD_MIN
                    else CSV_3E_ADD_MIN
                val addMax = if(mViewModel.logMode == UDSLoggingMode.MODE_22) CSV_22_ADD_MAX
                    else CSV_3E_ADD_MAX
                val pidList = PIDCSVFile.readStream(activity?.contentResolver?.openInputStream(uri), addMin, addMax)
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
            mViewModel.logMode = UDSLoggingMode.MODE_22
            mViewModel.pidList = PIDIndex.A
            mViewModel.CSVFileName = getString(R.string.filename_22_csv, "a")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button22BCSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 22 B CSV")
            mViewModel.logMode = UDSLoggingMode.MODE_22
            mViewModel.pidList = PIDIndex.B
            mViewModel.CSVFileName = getString(R.string.filename_22_csv, "b")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button22CCSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 22 C CSV")
            mViewModel.logMode = UDSLoggingMode.MODE_22
            mViewModel.pidList = PIDIndex.C
            mViewModel.CSVFileName = getString(R.string.filename_22_csv, "c")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button3EACSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 3E A CSV")
            mViewModel.logMode = UDSLoggingMode.MODE_3E
            mViewModel.pidList = PIDIndex.A
            mViewModel.CSVFileName = getString(R.string.filename_3E_csv, "a")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button3EBCSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 3E B CSV")
            mViewModel.logMode = UDSLoggingMode.MODE_3E
            mViewModel.pidList = PIDIndex.B
            mViewModel.CSVFileName = getString(R.string.filename_3E_csv, "b")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button3ECCSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 3E C CSV")
            mViewModel.logMode = UDSLoggingMode.MODE_3E
            mViewModel.pidList = PIDIndex.C
            mViewModel.CSVFileName = getString(R.string.filename_3E_csv, "c")
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.buttonSetBGNormalColor).setOnClickListener {
            ColorSettings.getColor(ColorList.BG_NORMAL)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBGWarningColor).setOnClickListener {
            ColorSettings.getColor(ColorList.BG_WARN)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetTextColor).setOnClickListener {
            ColorSettings.getColor(ColorList.TEXT)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBarNormalColor).setOnClickListener {
            ColorSettings.getColor(ColorList.BAR_NORMAL)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBarWarnColor).setOnClickListener {
            ColorSettings.getColor(ColorList.BAR_WARN)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetErrorColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_ERROR)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetNoneColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_NONE)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetConnectingColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_CONNECTING)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetConnectedColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_CONNECTED)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetLoggingColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_LOGGING)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetWritingColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_WRITING)
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
            val color = ColorList.TEXT.value
            currentView.findViewById<TextView>(R.id.textViewDisplaySize).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewUpdateRate).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewPersistDelay).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewPersistQDelay).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewOutputDirectory).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewLoggingMode).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewLoggingList).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewPIDCSV).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewMiscOptions).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewCalcHPOptions).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewColorOptions).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonApplication).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButton3E).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButton22).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonA).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonB).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonC).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxInvertCruise).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxScreenOn).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxAlwaysPortrait).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxCalcHP).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxUseAccel).setTextColor(color)

            //Set color boxes
            currentView.findViewById<Button>(R.id.buttonSetBGNormalColor).setTextColor(ColorSettings.mColorList[ColorList.BG_NORMAL.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBGNormalColor).setBackgroundColor(ColorSettings.mColorList[ColorList.BG_NORMAL.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetBGWarningColor).setTextColor(ColorSettings.mColorList[ColorList.BG_WARN.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBGWarningColor).setBackgroundColor(ColorSettings.mColorList[ColorList.BG_WARN.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetTextColor).setTextColor(ColorSettings.mColorList[ColorList.TEXT.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetTextColor).setBackgroundColor(ColorSettings.mColorList[ColorList.TEXT.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetBarNormalColor).setTextColor(ColorSettings.mColorList[ColorList.BAR_NORMAL.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBarNormalColor).setBackgroundColor(ColorSettings.mColorList[ColorList.BAR_NORMAL.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetBarWarnColor).setTextColor(ColorSettings.mColorList[ColorList.BAR_WARN.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBarWarnColor).setBackgroundColor(ColorSettings.mColorList[ColorList.BAR_WARN.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetErrorColor).setTextColor(ColorSettings.mColorList[ColorList.ST_ERROR.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetErrorColor).setBackgroundColor(ColorSettings.mColorList[ColorList.ST_ERROR.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetNoneColor).setTextColor(ColorSettings.mColorList[ColorList.ST_NONE.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetNoneColor).setBackgroundColor(ColorSettings.mColorList[ColorList.ST_NONE.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetConnectingColor).setTextColor(ColorSettings.mColorList[ColorList.ST_CONNECTING.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetConnectingColor).setBackgroundColor(ColorSettings.mColorList[ColorList.ST_CONNECTING.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetConnectedColor).setTextColor(ColorSettings.mColorList[ColorList.ST_CONNECTED.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetConnectedColor).setBackgroundColor(ColorSettings.mColorList[ColorList.ST_CONNECTED.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetLoggingColor).setTextColor(ColorSettings.mColorList[ColorList.ST_LOGGING.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetLoggingColor).setBackgroundColor(ColorSettings.mColorList[ColorList.ST_LOGGING.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetWritingColor).setTextColor(ColorSettings.mColorList[ColorList.ST_WRITING.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetWritingColor).setBackgroundColor(ColorSettings.mColorList[ColorList.ST_WRITING.ordinal])

            //Set background color
            currentView.setBackgroundColor(ColorList.BG_NORMAL.value)
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
                UDSLoggingMode.MODE_22 -> currentView.findViewById<RadioButton>(R.id.radioButton22).isChecked = true
                else -> currentView.findViewById<RadioButton>(R.id.radioButton3E).isChecked = true
            }

            //Get list index
            when(PIDs.getIndex()) {
                PIDIndex.A -> currentView.findViewById<RadioButton>(R.id.radioButtonA).isChecked = true
                PIDIndex.B -> currentView.findViewById<RadioButton>(R.id.radioButtonB).isChecked = true
                PIDIndex.C -> currentView.findViewById<RadioButton>(R.id.radioButtonC).isChecked = true
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
            ColorList.values().forEachIndexed { i, color ->
                ConfigFile.set("Color.${color.name}", ColorSettings.mColorList[i].toColorHex())
            }

            //Stop all tasks
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BTServiceTask.DO_STOP_TASK.toString()
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