package com.app.simostools

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.core.content.ContextCompat

class SettingsCarViewModel : ViewModel() {
    var logMode         = UDSLoggingMode.MODE_22
}

class SettingsCarFragment : Fragment() {
    private val TAG                                     = "SettingsCar"
    private lateinit var mViewModel: SettingsCarViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DebugLog.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_car, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        DebugLog.d(TAG, "onDestroyView")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(this).get(SettingsCarViewModel::class.java)

        view.findViewById<SeekBar>(R.id.seekBarCurbWeight).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewCurbWeight).text = getString(R.string.textview_settings_curb_weight, view.findViewById<SeekBar>(R.id.seekBarCurbWeight).progress)
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarTireDiameter).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewTireDiameter).text = getString(R.string.textview_settings_tire_diameter, view.findViewById<SeekBar>(R.id.seekBarTireDiameter).progress.toFloat()/1000f)
            }
        })

        view.findViewById<SeekBar>(R.id.seekBarDragCoefficient).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.findViewById<TextView>(R.id.textViewDragCoefficient).text = getString(R.string.textview_settings_drag_coefficient, view.findViewById<SeekBar>(R.id.seekBarDragCoefficient).progress.toFloat()/100.0f)
            }
        })

        doReset()

        DebugLog.d(TAG, "onViewCreated")
    }

    private fun doReset() {
        view?.let { currentView ->
            currentView.findViewById<SeekBar>(R.id.seekBarCurbWeight)?.apply {
                min = 1300
                max = 1800
                progress = ConfigSettings.CURB_WEIGHT.toFloat().toInt()
                callOnClick()
            }

            currentView.findViewById<SeekBar>(R.id.seekBarTireDiameter)?.apply {
                min = 1
                max = 800
                progress = (ConfigSettings.TIRE_DIAMETER.toFloat() * 1000f).toInt()
                callOnClick()
            }

            currentView.findViewById<SeekBar>(R.id.seekBarDragCoefficient)?.apply {
                min = 1
                max = 200
                progress = (ConfigSettings.DRAG_COEFFICIENT.toDouble() * 20000000.0).toInt()
                callOnClick()
            }

            //Get gear ratios
            currentView.findViewById<EditText>(R.id.editTextGear1).setText(GearRatios.GEAR1.ratio.toString())
            currentView.findViewById<EditText>(R.id.editTextGear2).setText(GearRatios.GEAR2.ratio.toString())
            currentView.findViewById<EditText>(R.id.editTextGear3).setText(GearRatios.GEAR3.ratio.toString())
            currentView.findViewById<EditText>(R.id.editTextGear4).setText(GearRatios.GEAR4.ratio.toString())
            currentView.findViewById<EditText>(R.id.editTextGear5).setText(GearRatios.GEAR5.ratio.toString())
            currentView.findViewById<EditText>(R.id.editTextGear6).setText(GearRatios.GEAR6.ratio.toString())
            currentView.findViewById<EditText>(R.id.editTextGear7).setText(GearRatios.GEAR7.ratio.toString())
            currentView.findViewById<EditText>(R.id.editTextGearFinal).setText(GearRatios.FINAL.ratio.toString())

            //Set colors
            doSetColor()
        }

        DebugLog.d(TAG, "doReset")
    }

    fun doSetColor() {
        view?.let { currentView ->
            //Set font color
            val color = ColorList.TEXT.value
            currentView.findViewById<TextView>(R.id.textViewCurbWeight).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewTireDiameter).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewDragCoefficient).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewGear1).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewGear2).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewGear3).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewGear4).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewGear5).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewGear6).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewGear7).setTextColor(color)
            currentView.findViewById<TextView>(R.id.textViewGearFinal).setTextColor(color)

            //Set color edit
            currentView.findViewById<EditText>(R.id.editTextGear1).setTextColor(color)
            currentView.findViewById<EditText>(R.id.editTextGear2).setTextColor(color)
            currentView.findViewById<EditText>(R.id.editTextGear3).setTextColor(color)
            currentView.findViewById<EditText>(R.id.editTextGear4).setTextColor(color)
            currentView.findViewById<EditText>(R.id.editTextGear5).setTextColor(color)
            currentView.findViewById<EditText>(R.id.editTextGear6).setTextColor(color)
            currentView.findViewById<EditText>(R.id.editTextGear7).setTextColor(color)
            currentView.findViewById<EditText>(R.id.editTextGearFinal).setTextColor(color)

            //Set background color
            currentView.setBackgroundColor(ColorList.BG_NORMAL.value)
        }
        DebugLog.d(TAG, "doSetColor")
    }

    fun doSave() {
        view?.let { currentView ->
            // Set update rate
            ConfigFile.set(ConfigSettings.CURB_WEIGHT.cfgName, currentView.findViewById<SeekBar>(R.id.seekBarCurbWeight).progress.toString())

            // Set persist delay
            ConfigFile.set(ConfigSettings.TIRE_DIAMETER.cfgName, (currentView.findViewById<SeekBar>(R.id.seekBarTireDiameter).progress.toFloat()/1000.0f).toString())

            // Set persist delay
            ConfigFile.set(ConfigSettings.DRAG_COEFFICIENT.cfgName, (currentView.findViewById<SeekBar>(R.id.seekBarDragCoefficient).progress.toDouble()/20000000.0).toString())

            //Save Gear Ratios
            ConfigFile.set("${GearRatios.GEAR1.key}.${GearRatios.GEAR1.gear}", currentView.findViewById<EditText>(R.id.editTextGear1).text.toString())
            ConfigFile.set("${GearRatios.GEAR2.key}.${GearRatios.GEAR2.gear}", currentView.findViewById<EditText>(R.id.editTextGear2).text.toString())
            ConfigFile.set("${GearRatios.GEAR3.key}.${GearRatios.GEAR3.gear}", currentView.findViewById<EditText>(R.id.editTextGear3).text.toString())
            ConfigFile.set("${GearRatios.GEAR4.key}.${GearRatios.GEAR4.gear}", currentView.findViewById<EditText>(R.id.editTextGear4).text.toString())
            ConfigFile.set("${GearRatios.GEAR5.key}.${GearRatios.GEAR5.gear}", currentView.findViewById<EditText>(R.id.editTextGear5).text.toString())
            ConfigFile.set("${GearRatios.GEAR6.key}.${GearRatios.GEAR6.gear}", currentView.findViewById<EditText>(R.id.editTextGear6).text.toString())
            ConfigFile.set("${GearRatios.GEAR7.key}.${GearRatios.GEAR7.gear}", currentView.findViewById<EditText>(R.id.editTextGear7).text.toString())
            ConfigFile.set("${GearRatios.FINAL.key}.${GearRatios.FINAL.gear}", currentView.findViewById<EditText>(R.id.editTextGearFinal).text.toString())
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