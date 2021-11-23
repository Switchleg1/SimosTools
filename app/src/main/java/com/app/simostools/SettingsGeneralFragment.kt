package com.app.simostools

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.widget.Toast
import androidx.core.content.ContextCompat

class SettingsViewModel : ViewModel() {
    var logMode         = UDSLoggingMode.MODE_22
    var adapterName     = ""
}

class SettingsGeneralFragment : Fragment() {
    private val TAG                                     = "SettingsGeneral"
    private var mLoadCallback: (() -> Unit)?            = null
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
                        TempPIDS.reset(context)
                        mLoadCallback?.invoke()
                        Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        DebugLog.d(TAG, "resultPickLauncher")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DebugLog.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_general, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mLoadCallback = null

        DebugLog.d(TAG, "onDestroyView")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        val csv22Button = view.findViewById<SwitchButton>(R.id.button22CSV)
        csv22Button.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                chooseFile.type = "text/*"
                chooseFile = Intent.createChooser(chooseFile, "Choose a 22 A CSV")
                mViewModel.logMode = UDSLoggingMode.MODE_22
                resultPickLauncher.launch(chooseFile)
            }
        }

        val csv3EButton = view.findViewById<SwitchButton>(R.id.button3ECSV)
        csv3EButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                chooseFile.type = "text/*"
                chooseFile = Intent.createChooser(chooseFile, "Choose a 3E A CSV")
                mViewModel.logMode = UDSLoggingMode.MODE_3E
                resultPickLauncher.launch(chooseFile)
            }
        }

        val csv22ButtonReset = view.findViewById<SwitchButton>(R.id.button22CSVReset)
        csv22ButtonReset.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                val builder = AlertDialog.Builder(context)
                //Setting message manually and performing action on button click
                builder.setMessage("Do you want to load mode 22 csv defaults?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ ->
                        PIDs.loadDefaultPIDS(UDSLoggingMode.MODE_22)
                        TempPIDS.reset(context)
                        TempPIDS.save(context)
                        mLoadCallback?.invoke()
                    }
                    .setNegativeButton("No") { _, _ -> }
                //Creating dialog box
                val alert: AlertDialog = builder.create()

                //Setting the title manually
                alert.setTitle("Reset mode 22 CSV")
                alert.show()
            }
        }

        val csv3EButtonReset = view.findViewById<SwitchButton>(R.id.button3ECSVReset)
        csv3EButtonReset.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                val builder = AlertDialog.Builder(context)
                //Setting message manually and performing action on button click
                builder.setMessage("Do you want to load mode 3E csv defaults?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ ->
                        PIDs.loadDefaultPIDS(UDSLoggingMode.MODE_3E)
                        TempPIDS.reset(context)
                        TempPIDS.save(context)
                        mLoadCallback?.invoke()
                    }
                    .setNegativeButton("No") { _, _ -> }
                //Creating dialog box
                val alert: AlertDialog = builder.create()

                //Setting the title manually
                alert.setTitle("Reset mode 3E CSV")
                alert.show()
            }
        }

        val setAdapterButton = view.findViewById<SwitchButton>(R.id.buttonAdapterName)
        setAdapterButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                val builder = AlertDialog.Builder(context)
                var adapterName = view.findViewById<EditText>(R.id.editTextAdapterName).text.toString()
                if(adapterName.length > MAX_GAP_LENGTH)
                    adapterName = adapterName.subSequence(0, MAX_GAP_LENGTH).toString()

                if(adapterName.isNotEmpty()) {

                    //Setting message manually and performing action on button click
                    builder.setMessage("Are you sure you would like to set the name of the adapter to $adapterName?")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { _, _ ->
                            mViewModel.adapterName = adapterName
                            ConfigSettings.ADAPTER_NAME.value = adapterName
                            view.findViewById<EditText>(R.id.editTextAdapterName)
                                .setText(adapterName)
                            sendServiceMessage(BTServiceTask.DO_SET_ADAPTER.toString())
                        }
                        .setNegativeButton("No") { _, _ -> }
                    //Creating dialog box
                    val alert: AlertDialog = builder.create()

                    //Setting the title manually
                    alert.setTitle("Set adapter name")
                    alert.show()
                }
            }
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

        view.findViewById<Button>(R.id.buttonSetGaugeNormalColor).setOnClickListener {
            ColorSettings.getColor(ColorList.GAUGE_NORMAL)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetGaugeWarnColor).setOnClickListener {
            ColorSettings.getColor(ColorList.GAUGE_WARN)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetGaugeBGColor).setOnClickListener {
            ColorSettings.getColor(ColorList.GAUGE_BG)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetGaugeValueColor).setOnClickListener {
            ColorSettings.getColor(ColorList.GAUGE_VALUE)
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

        view.findViewById<Button>(R.id.buttonSetBTTextColor).setOnClickListener {
            ColorSettings.getColor(ColorList.BT_TEXT)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBTBGColor).setOnClickListener {
            ColorSettings.getColor(ColorList.BT_BG)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<Button>(R.id.buttonSetBTRimColor).setOnClickListener {
            ColorSettings.getColor(ColorList.BT_RIM)
            findNavController().navigate(R.id.action_SettingsFragment_to_ColorFragment)
        }

        view.findViewById<SeekBar>(R.id.seekBarDisplayRate).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewDisplayRate).text = getString(R.string.textview_settings_display_rate, view.findViewById<SeekBar>(R.id.seekBarDisplayRate).progress)
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarLoggingRate).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewLoggingRate).text = getString(R.string.textview_settings_logging_rate, view.findViewById<SeekBar>(R.id.seekBarLoggingRate).progress)
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarQCorrection).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewQCorrection).text = getString(R.string.textview_settings_q_correction, view.findViewById<SeekBar>(R.id.seekBarQCorrection).progress)
            }
        })

        doReset()

        DebugLog.d(TAG, "onViewCreated")
    }

    private fun doReset() {
        view?.let { currentView ->
            //Get update rate
            currentView.findViewById<SeekBar>(R.id.seekBarDisplayRate)?.let { updateRate ->
                updateRate.min = 5
                updateRate.max = 30
                updateRate.progress = ConfigSettings.DISPLAY_RATE.toInt()
                updateRate.callOnClick()
            }

            //Get Logging rate
            currentView.findViewById<SeekBar>(R.id.seekBarLoggingRate)?.let { persistDelay ->
                persistDelay.min = 10
                persistDelay.max = 150
                persistDelay.progress = ConfigSettings.LOGGING_RATE.toInt()
                persistDelay.callOnClick()
            }

            //Get queue delay
            currentView.findViewById<SeekBar>(R.id.seekBarQCorrection)?.let { persistQDelay ->
                persistQDelay.min = 1
                persistQDelay.max = 25
                persistQDelay.progress = ConfigSettings.Q_CORRECTION.toInt()
                persistQDelay.callOnClick()
            }

            //Get gauge mode
            when(ConfigSettings.GAUGE_TYPE.toGaugeType()) {
                GaugeType.BAR_H -> currentView.findViewById<RadioButton>(R.id.radioButtonBARH).isChecked    = true
                GaugeType.BAR_V -> currentView.findViewById<RadioButton>(R.id.radioButtonBARV).isChecked    = true
                GaugeType.BASIC -> currentView.findViewById<RadioButton>(R.id.radioButtonBASIC).isChecked   = true
                GaugeType.ROUND -> currentView.findViewById<RadioButton>(R.id.radioButtonROUND).isChecked   = true
            }

            //Get logging mode
            when(UDSLogger.getMode()) {
                UDSLoggingMode.MODE_22  -> currentView.findViewById<RadioButton>(R.id.radioButton22).isChecked  = true
                else                    -> currentView.findViewById<RadioButton>(R.id.radioButton3E).isChecked  = true
            }

            //Get output directory
            if(RequiredPermissions.READ_STORAGE.result == PackageManager.PERMISSION_DENIED) {
                currentView.findViewById<RadioButton>(R.id.radioButtonApplication).isChecked = true
                currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isEnabled = false
                currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isEnabled = false
            } else {
                currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isEnabled = true
                currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isEnabled = true
                when (ConfigSettings.OUT_DIRECTORY.toDirectory()) {
                    DirectoryList.DOWNLOADS -> currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isChecked = true
                    DirectoryList.DOCUMENTS -> currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isChecked = true
                    DirectoryList.APP -> currentView.findViewById<RadioButton>(R.id.radioButtonApplication).isChecked = true
                }
            }

            //Get draw minmax
            currentView.findViewById<CheckBox>(R.id.checkBoxDrawMinMax).isChecked = ConfigSettings.DRAW_MIN_MAX.toBoolean()

            //Get draw graduations
            currentView.findViewById<CheckBox>(R.id.checkBoxDrawGrad).isChecked = ConfigSettings.DRAW_GRADUATIONS.toBoolean()

            //Get cruise invert
            currentView.findViewById<CheckBox>(R.id.checkBoxInvertCruise).isChecked = ConfigSettings.INVERT_CRUISE.toBoolean()

            //Get keep screen on
            currentView.findViewById<CheckBox>(R.id.checkBoxScreenOn).isChecked = ConfigSettings.KEEP_SCREEN_ON.toBoolean()

            //Get calculate HP
            currentView.findViewById<CheckBox>(R.id.checkBoxCalcHP).isChecked = ConfigSettings.CALCULATE_HP.toBoolean()

            //Get use MS2
            currentView.findViewById<CheckBox>(R.id.checkBoxUseAccel).isChecked = ConfigSettings.USE_MS2.toBoolean()

            //Get always use portrait
            currentView.findViewById<CheckBox>(R.id.checkBoxAlwaysPortrait).isChecked = ConfigSettings.ALWAYS_PORTRAIT.toBoolean()

            //Get auto log
            currentView.findViewById<CheckBox>(R.id.checkBoxAutoLog).isChecked = ConfigSettings.AUTO_LOG.toBoolean()

            //Get log name
            currentView.findViewById<EditText>(R.id.editTextLogName).setText(ConfigSettings.LOG_NAME.toString())

            //Get adapter name
            mViewModel.adapterName = ConfigSettings.ADAPTER_NAME.toString()
            currentView.findViewById<EditText>(R.id.editTextAdapterName).setText(mViewModel.adapterName)

            //Set colors
            doSetColor()
        }

        DebugLog.d(TAG, "doReset")
    }

    fun doSetColor() {
        view?.let { currentView ->
            //Set font color
            val color = ColorList.TEXT.value
            currentView.findViewById<TextView>(R.id.textViewDisplayRate).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewLoggingRate).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewQCorrection).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewPIDCSV).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewOutputDirectory).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewDisplayType).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewLoggingMode).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewMiscOptions).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewLogName).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewAdapterName).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewCalcHPOptions).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewColorOptions).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonApplication).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonBARH).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonBARV).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonBASIC).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButtonROUND).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButton3E).setTextColor(color)
            currentView.findViewById<RadioButton>(R.id.radioButton22).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxDrawMinMax).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxDrawGrad).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxInvertCruise).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxScreenOn).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxAlwaysPortrait).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxAutoLog).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxCalcHP).setTextColor(color)
            currentView.findViewById<CheckBox>(R.id.checkBoxUseAccel).setTextColor(color)

            //Set color edit
            currentView.findViewById<EditText>(R.id.editTextLogName).setTextColor(color)
            currentView.findViewById<EditText>(R.id.editTextAdapterName).setTextColor(color)

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
            currentView.findViewById<Button>(R.id.buttonSetGaugeValueColor).setTextColor(ColorSettings.mColorList[ColorList.GAUGE_VALUE.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetGaugeValueColor).setBackgroundColor(ColorSettings.mColorList[ColorList.GAUGE_VALUE.ordinal])
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
            currentView.findViewById<Button>(R.id.buttonSetBTTextColor).setTextColor(ColorSettings.mColorList[ColorList.BT_TEXT.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBTTextColor).setBackgroundColor(ColorSettings.mColorList[ColorList.BT_TEXT.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetBTRimColor).setTextColor(ColorSettings.mColorList[ColorList.BT_RIM.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBTRimColor).setBackgroundColor(ColorSettings.mColorList[ColorList.BT_RIM.ordinal])
            currentView.findViewById<Button>(R.id.buttonSetBTBGColor).setTextColor(ColorSettings.mColorList[ColorList.BT_BG.ordinal].toColorInverse())
            currentView.findViewById<Button>(R.id.buttonSetBTBGColor).setBackgroundColor(ColorSettings.mColorList[ColorList.BT_BG.ordinal])

            //Set background color
            currentView.setBackgroundColor(ColorList.BG_NORMAL.value)
        }
        DebugLog.d(TAG, "doSetColor")
    }

    fun setLoadCallback(callback: (() -> Unit)?) {
        mLoadCallback = callback
    }

    fun doSave() {
        view?.let { currentView ->
            // Set update rate
            ConfigFile.set(ConfigSettings.DISPLAY_RATE.cfgName, currentView.findViewById<SeekBar>(R.id.seekBarDisplayRate).progress.toString())

            // Set persist delay
            ConfigFile.set(ConfigSettings.LOGGING_RATE.cfgName, currentView.findViewById<SeekBar>(R.id.seekBarLoggingRate).progress.toString())

            // Set persist delay
            ConfigFile.set(ConfigSettings.Q_CORRECTION.cfgName, currentView.findViewById<SeekBar>(R.id.seekBarQCorrection).progress.toString())

            // Set gauge type
            when {
                currentView.findViewById<RadioButton>(R.id.radioButtonBARH).isChecked -> ConfigFile.set(
                    ConfigSettings.GAUGE_TYPE.cfgName,
                    GaugeType.BAR_H.cfgName
                )
                currentView.findViewById<RadioButton>(R.id.radioButtonBARV).isChecked -> ConfigFile.set(
                    ConfigSettings.GAUGE_TYPE.cfgName,
                    GaugeType.BAR_V.cfgName
                )
                currentView.findViewById<RadioButton>(R.id.radioButtonBASIC).isChecked -> ConfigFile.set(
                    ConfigSettings.GAUGE_TYPE.cfgName,
                    GaugeType.BASIC.cfgName
                )
                currentView.findViewById<RadioButton>(R.id.radioButtonROUND).isChecked -> ConfigFile.set(
                    ConfigSettings.GAUGE_TYPE.cfgName,
                    GaugeType.ROUND.cfgName
                )
            }

            // Set logging mode
            when (currentView.findViewById<RadioButton>(R.id.radioButton22).isChecked) {
                true -> ConfigFile.set(UDSLoggingMode.MODE_22.key, UDSLoggingMode.MODE_22.cfgName)
                false -> ConfigFile.set(UDSLoggingMode.MODE_3E.key, UDSLoggingMode.MODE_3E.cfgName)
            }

            // Set default output folder
            when {
                currentView.findViewById<RadioButton>(R.id.radioButtonDownloads).isChecked -> ConfigFile.set(
                    ConfigSettings.OUT_DIRECTORY.cfgName,
                    DirectoryList.DOWNLOADS.cfgName
                )
                currentView.findViewById<RadioButton>(R.id.radioButtonDocuments).isChecked -> ConfigFile.set(
                    ConfigSettings.OUT_DIRECTORY.cfgName,
                    DirectoryList.DOCUMENTS.cfgName
                )
                currentView.findViewById<RadioButton>(R.id.radioButtonApplication).isChecked -> ConfigFile.set(
                    ConfigSettings.OUT_DIRECTORY.cfgName,
                    DirectoryList.APP.cfgName
                )
            }

            //Set draw min/max
            ConfigFile.set(ConfigSettings.DRAW_MIN_MAX.cfgName, currentView.findViewById<CheckBox>(R.id.checkBoxDrawMinMax).isChecked.toString())

            //Set draw min/max
            ConfigFile.set(ConfigSettings.DRAW_GRADUATIONS.cfgName, currentView.findViewById<CheckBox>(R.id.checkBoxDrawGrad).isChecked.toString())

            //Set invert cruise
            ConfigFile.set(ConfigSettings.INVERT_CRUISE.cfgName, currentView.findViewById<CheckBox>(R.id.checkBoxInvertCruise).isChecked.toString())

            //Set screen on
            ConfigFile.set(ConfigSettings.KEEP_SCREEN_ON.cfgName, currentView.findViewById<CheckBox>(R.id.checkBoxScreenOn).isChecked.toString())

            //Calculate HP
            ConfigFile.set(ConfigSettings.CALCULATE_HP.cfgName, currentView.findViewById<CheckBox>(R.id.checkBoxCalcHP).isChecked.toString())

            //Calculate HP
            ConfigFile.set(ConfigSettings.USE_MS2.cfgName, currentView.findViewById<CheckBox>(R.id.checkBoxUseAccel).isChecked.toString())

            //Always use portrait view
            ConfigFile.set(ConfigSettings.ALWAYS_PORTRAIT.cfgName, currentView.findViewById<CheckBox>(R.id.checkBoxAlwaysPortrait).isChecked.toString())

            //Auto log when idle
            ConfigFile.set(ConfigSettings.AUTO_LOG.cfgName, currentView.findViewById<CheckBox>(R.id.checkBoxAutoLog).isChecked.toString())

            //Set log name
            ConfigFile.set(ConfigSettings.LOG_NAME.cfgName, currentView.findViewById<EditText>(R.id.editTextLogName).text.toString())

            //Set adapter name
            ConfigFile.set(ConfigSettings.ADAPTER_NAME.cfgName, mViewModel.adapterName)

            //Set Colors
            ColorList.values().forEachIndexed { i, color ->
                ConfigFile.set(
                    "${color.key}.${color.cfgName}",
                    ColorSettings.mColorList[i].toColorHex()
                )
            }
        }
        DebugLog.d(TAG, "doSave")
    }

    private fun sendServiceMessage(type: String) {
        activity?.let {
            val serviceIntent = Intent(it, BTService::class.java)
            serviceIntent.action = type
            ContextCompat.startForegroundService(it, serviceIntent)
        }
    }
}