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
import androidx.core.view.forEach
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2

class SettingsViewModel : ViewModel() {
    var logMode = UDSLoggingMode.MODE_22
}

class SettingsFragment : Fragment() {
    private val TAG = "Settings"
    private lateinit var mViewModel: SettingsViewModel

    var resultPickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                val addMin = mViewModel.logMode.addressMin
                val addMax = mViewModel.logMode.addressMax
                val pidList = PIDCSVFile.readStream(activity?.contentResolver?.openInputStream(uri), addMin, addMax)
                if(pidList != null) {
                    val CSVFileName = getString(R.string.filename_pid_csv, mViewModel.logMode.cfgName)
                    if(PIDCSVFile.write(CSVFileName, context, pidList, true)) {
                        PIDs.setList(mViewModel.logMode, pidList)

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

        view.findViewById<Button>(R.id.buttonSettingsReset).setOnClickListener {
            doReset()
        }

        view.findViewById<Button>(R.id.buttonSettingsSave).setOnClickListener {
            doSave()
        }

        view.findViewById<Button>(R.id.button22CSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 22 A CSV")
            mViewModel.logMode = UDSLoggingMode.MODE_22
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.button3ECSV).setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "text/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a 3E A CSV")
            mViewModel.logMode = UDSLoggingMode.MODE_3E
            resultPickLauncher.launch(chooseFile)
        }

        view.findViewById<Button>(R.id.buttonSetBGNormalColor).setOnClickListener {
            ColorSettings.getColor(ColorList.BG_NORMAL)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBGWarningColor).setOnClickListener {
            ColorSettings.getColor(ColorList.BG_WARN)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetTextColor).setOnClickListener {
            ColorSettings.getColor(ColorList.TEXT)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetGaugeNormalColor).setOnClickListener {
            ColorSettings.getColor(ColorList.GAUGE_NORMAL)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetGaugeWarnColor).setOnClickListener {
            ColorSettings.getColor(ColorList.GAUGE_WARN)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetGaugeBGColor).setOnClickListener {
            ColorSettings.getColor(ColorList.GAUGE_BG)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetErrorColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_ERROR)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetNoneColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_NONE)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetConnectingColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_CONNECTING)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetConnectedColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_CONNECTED)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetLoggingColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_LOGGING)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetWritingColor).setOnClickListener {
            ColorSettings.getColor(ColorList.ST_WRITING)
            findNavController().navigate(R.id.action_MainFragment_to_ColorFragment)
        }



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

        doReset()
    }

    private fun doSetColor() {
        view?.let { currentView ->
            //Set font color
            val color = ColorList.TEXT.value
            currentView.findViewById<TextView>(R.id.textViewUpdateRate).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewPersistDelay).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewPersistQDelay).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewOutputDirectory).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewDisplayType).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewLoggingMode).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewPIDCSV).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewMiscOptions).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewCalcHPOptions).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewColorOptions).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonApplication).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonBAR).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonROUND).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButton3E).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButton22).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxDrawMinMax).setTextColor(color)
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
            currentView.findViewById<Button>(R.id.buttonSetGaugeNormalColor).setTextColor(ColorSettings.mColorList[ColorList.GAUGE_NORMAL.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetGaugeNormalColor).setBackgroundColor(ColorSettings.mColorList[ColorList.GAUGE_NORMAL.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetGaugeWarnColor).setTextColor(ColorSettings.mColorList[ColorList.GAUGE_WARN.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetGaugeWarnColor).setBackgroundColor(ColorSettings.mColorList[ColorList.GAUGE_WARN.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetGaugeBGColor).setTextColor(ColorSettings.mColorList[ColorList.GAUGE_BG.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetGaugeBGColor).setBackgroundColor(ColorSettings.mColorList[ColorList.GAUGE_BG.ordinal])
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

    private fun doReset() {
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

            //Get gauge mode
            when(Settings.displayType) {
                DisplayType.BAR -> currentView.findViewById<RadioButton>(R.id.radioButtonBAR).isChecked = true
                DisplayType.ROUND -> currentView.findViewById<RadioButton>(R.id.radioButtonROUND).isChecked = true
            }

            //Get logging mode
            when(UDSLogger.getMode()) {
                UDSLoggingMode.MODE_22 -> currentView.findViewById<RadioButton>(R.id.radioButton22).isChecked = true
                else -> currentView.findViewById<RadioButton>(R.id.radioButton3E).isChecked = true
            }

            //Get output directory
            when (Settings.outputDirectory) {
                DirectoryList.DOWNLOADS -> currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isChecked = true
                DirectoryList.DOCUMENTS -> currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isChecked = true
                DirectoryList.APP -> currentView.findViewById<RadioButton>(R.id.radioButtonApplication).isChecked = true
            }

            //Get draw minmax
            currentView.findViewById<CheckBox>(R.id.checkBoxDrawMinMax).isChecked = Settings.drawMinMax

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
            ColorSettings.resetColors()
            doSetColor()
        }
    }

    private fun doSave() {
        view?.let { currentView ->
            // Set update rate
            ConfigFile.set(
                "UpdateRate",
                currentView.findViewById<SeekBar>(R.id.seekBarUpdateRate).progress.toString()
            )

            // Set persist delay
            ConfigFile.set(
                "PersistDelay",
                currentView.findViewById<SeekBar>(R.id.seekBarPersistDelay).progress.toString()
            )

            // Set persist delay
            ConfigFile.set(
                "PersistQDelay",
                currentView.findViewById<SeekBar>(R.id.seekBarPersistQDelay).progress.toString()
            )

            // Set gauge mode
            when (currentView.findViewById<RadioButton>(R.id.radioButtonBAR).isChecked) {
                true -> ConfigFile.set(DisplayType.BAR.key, DisplayType.BAR.cfgName)
                false -> ConfigFile.set(DisplayType.ROUND.key, DisplayType.ROUND.cfgName)
            }

            // Set logging mode
            when (currentView.findViewById<RadioButton>(R.id.radioButton22).isChecked) {
                true -> ConfigFile.set(UDSLoggingMode.MODE_22.key, UDSLoggingMode.MODE_22.cfgName)
                false -> ConfigFile.set(UDSLoggingMode.MODE_3E.key, UDSLoggingMode.MODE_3E.cfgName)
            }

            // Set default output folder
            when {
                currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isChecked -> ConfigFile.set(
                    DirectoryList.DOWNLOADS.key,
                    DirectoryList.DOWNLOADS.cfgName
                )
                currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isChecked -> ConfigFile.set(
                    DirectoryList.DOCUMENTS.key,
                    DirectoryList.DOCUMENTS.cfgName
                )
                currentView.findViewById<RadioButton>(R.id.radioButtonApplication).isChecked -> ConfigFile.set(
                    DirectoryList.APP.key,
                    DirectoryList.APP.cfgName
                )
            }

            //Set draw min/max
            ConfigFile.set(
                "DrawMinMax",
                currentView.findViewById<CheckBox>(R.id.checkBoxDrawMinMax).isChecked.toString()
            )

            //Set invert cruise
            ConfigFile.set(
                "InvertCruise",
                currentView.findViewById<CheckBox>(R.id.checkBoxInvertCruise).isChecked.toString()
            )

            //Set screen on
            ConfigFile.set(
                "KeepScreenOn",
                currentView.findViewById<CheckBox>(R.id.checkBoxScreenOn).isChecked.toString()
            )

            //Calculate HP
            ConfigFile.set(
                "CalculateHP",
                currentView.findViewById<CheckBox>(R.id.checkBoxCalcHP).isChecked.toString()
            )

            //Calculate HP
            ConfigFile.set(
                "UseMS2Torque",
                currentView.findViewById<CheckBox>(R.id.checkBoxUseAccel).isChecked.toString()
            )

            //Always use portrait view
            ConfigFile.set(
                "AlwaysPortrait",
                currentView.findViewById<CheckBox>(R.id.checkBoxAlwaysPortrait).isChecked.toString()
            )

            //Set Colors
            ColorList.values().forEachIndexed { i, color ->
                ConfigFile.set(
                    "${color.key}.${color.cfgName}",
                    ColorSettings.mColorList[i].toColorHex()
                )
            }

            //Stop all tasks
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BTServiceTask.DO_STOP_TASK.toString()
            ContextCompat.startForegroundService(this.requireContext(), serviceIntent)

            // Write config
            ConfigFile.write(getString(R.string.filename_config), context)
            ConfigFile.read(getString(R.string.filename_config), context)

            //Reset colors
            ColorSettings.resetColors()

            //Set colors
            doSetColor()
        }
    }
}