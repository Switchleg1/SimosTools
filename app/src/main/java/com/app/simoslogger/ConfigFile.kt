package com.app.simoslogger

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Long.parseLong
import java.util.*
import java.util.TreeSet
import java.util.Collections
import java.util.Enumeration

class SProperties: Properties() {
    @Synchronized
    override fun keys(): Enumeration<Any>? {
        return Collections.enumeration(TreeSet(super.keys))
    }
}

object ConfigFile {
    private val TAG = "Config"
    private val mProperties = SProperties()

    fun write(fileName: String?, context: Context?) {
        val path = context?.getExternalFilesDir("")
        val propertiesFile = File(path, "/$fileName")
        if(!propertiesFile.exists()) {
            propertiesFile.createNewFile()
        }

        val propertiesOutputStream = FileOutputStream(propertiesFile)
        mProperties.store(propertiesOutputStream, "save to properties file")
    }

    fun read(fileName: String?, context: Context?) {
        val path = context?.getExternalFilesDir("")
        val propertiesFile = File(path, "/$fileName")
        if(!propertiesFile.exists()) {
            propertiesFile.createNewFile()
            writeDefaultConfig(fileName, context)
        }

        val inputStream = FileInputStream(propertiesFile)
        mProperties.load(inputStream)

        mProperties.forEach{(k, v) ->
            processKey(k.toString(), v.toString())
        }
    }

    fun set(key: String, value: String) {
        mProperties[key] = value
    }

    fun get(key: String): String {
        return mProperties[key].toString()
    }

    private fun processKey(key: String, value: String) {
        Log.i(TAG, "$key: $value")

        try {
            when (key) {
                "Mode" -> {
                    when (value) {
                        "22" -> UDSLogger.setMode(UDS_LOGGING_22)
                        "3E" -> UDSLogger.setMode(UDS_LOGGING_3E)
                    }
                }
                "UpdateRate" -> {
                    val i = value.toInt()
                    if(i in 1..10)
                        Settings.updateRate = 11 - i
                }
                "OutputDirectory" -> {
                    when(value) {
                        "Downloads" -> Settings.outputDirectory = Environment.DIRECTORY_DOWNLOADS
                        "Documents" -> Settings.outputDirectory = Environment.DIRECTORY_DOCUMENTS
                        "App" -> Settings.outputDirectory = "App"
                    }
                }
                "InvertCruise" -> {
                    val b = value.toBoolean()
                    Settings.invertCruise = b
                }
                "KeepScreenOn" -> {
                    val b = value.toBoolean()
                    Settings.keepScreenOn = b
                }
                "PersistDelay" -> {
                    val i = value.toInt()
                    if(i > -1 && i < 100)
                        Settings.persistDelay = i
                }
                "PersistQDelay" -> {
                    val i = value.toInt()
                    if(i > -1 && i < 100)
                        Settings.persistQDelay = i
                }
                "UseMS2Torque" -> {
                    val b = value.toBoolean()
                    Settings.useMS2Torque = b
                }
                "TireDiameter" -> {
                    val f = value.toFloat()
                    Settings.tireDiameter = f
                }
                "GearRatio.1" -> {
                    val f = value.toFloat()
                    Settings.gearRatios[0] = f
                }
                "GearRatio.2" -> {
                    val f = value.toFloat()
                    Settings.gearRatios[1] = f
                }
                "GearRatio.3" -> {
                    val f = value.toFloat()
                    Settings.gearRatios[2] = f
                }
                "GearRatio.4" -> {
                    val f = value.toFloat()
                    Settings.gearRatios[3] = f
                }
                "GearRatio.5" -> {
                    val f = value.toFloat()
                    Settings.gearRatios[4] = f
                }
                "GearRatio.6" -> {
                    val f = value.toFloat()
                    Settings.gearRatios[5] = f
                }
                "GearRatio.7" -> {
                    val f = value.toFloat()
                    Settings.gearRatios[6] = f
                }
                "GearRatio.Final" -> {
                    val f = value.toFloat()
                    Settings.gearRatios[7] = f
                }
                "CurbWeight" -> {
                    val f = value.toFloat()
                    if(f > 1200f && f < 2000f)
                        Settings.curbWeight = f
                }
                "DragCoefficient" -> {
                    val f = value.toFloat()
                    if(f > 0f && f < 5f)
                       Settings.dragCoefficient = f.toDouble() * DEFAULT_DRAG_COEFFICIENT
                }
                "ColorBGNormal" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_BG_NORMAL] = l.toColorInt()
                }
                "ColorBGWarn" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_BG_WARN] = l.toColorInt()
                }
                "ColorText" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_TEXT] = l.toColorInt()
                }
                "ColorBarNormal" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_BAR_NORMAL] = l.toColorInt()
                }
                "ColorBarWarn" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_BAR_WARN] = l.toColorInt()
                }
                "ColorStateError" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_ST_ERROR] = l.toColorInt()
                }
                "ColorStateNone" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_ST_NONE] = l.toColorInt()
                }
                "ColorStateConnecting" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_ST_CONNECTING] = l.toColorInt()
                }
                "ColorStateConnected" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_ST_CONNECTED] = l.toColorInt()
                }
                "ColorStateLogging" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_ST_LOGGING] = l.toColorInt()
                }
                "ColorStateWriting" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[COLOR_ST_WRITING] = l.toColorInt()
                }
                "AlwaysPortrait" -> {
                    val b = value.toBoolean()
                    Settings.alwaysPortrait = b
                }
                "DisplaySize" -> {
                    val f = value.toFloat()
                    if(f > 0f && f < 5f)
                        Settings.displaySize = f
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun writeDefaultConfig(filename: String?, context: Context?) {
        mProperties["Mode"] = "22"
        mProperties["OutputDirectory"] = "Downloads"
        mProperties["UpdateRate"] = (11-DEFAULT_UPDATE_RATE).toString()
        mProperties["InvertCruise"] = DEFAULT_INVERT_CRUISE.toString()
        mProperties["KeepScreenOn"] = DEFAULT_KEEP_SCREEN_ON.toString()
        mProperties["PersistDelay"] = DEFAULT_PERSIST_DELAY.toString()
        mProperties["PersistQDelay"] = DEFAULT_PERSIST_Q_DELAY.toString()
        mProperties["CalculateHP"] = DEFAULT_CALCULATE_HP.toString()
        mProperties["UseMS2Torque"] = DEFAULT_USE_MS2.toString()
        mProperties["TireDiameter"] = DEFAULT_TIRE_DIAMETER.toString()
        mProperties["CurbWeight"] = DEFAULT_CURB_WEIGHT.toString()
        mProperties["DragCoefficient"] = "1.0"
        mProperties["GearRatio.1"] = DEFAULT_GEAR_RATIOS[0].toString()
        mProperties["GearRatio.2"] = DEFAULT_GEAR_RATIOS[1].toString()
        mProperties["GearRatio.3"] = DEFAULT_GEAR_RATIOS[2].toString()
        mProperties["GearRatio.4"] = DEFAULT_GEAR_RATIOS[3].toString()
        mProperties["GearRatio.5"] = DEFAULT_GEAR_RATIOS[4].toString()
        mProperties["GearRatio.6"] = DEFAULT_GEAR_RATIOS[5].toString()
        mProperties["GearRatio.7"] = DEFAULT_GEAR_RATIOS[6].toString()
        mProperties["GearRatio.Final"] = DEFAULT_GEAR_RATIOS[7].toString()
        mProperties["ColorBGNormal"] = DEFAULT_COLOR_LIST[COLOR_BG_NORMAL].toColorHex()
        mProperties["ColorBGWarn"] = DEFAULT_COLOR_LIST[COLOR_BG_WARN].toColorHex()
        mProperties["ColorText"] = DEFAULT_COLOR_LIST[COLOR_TEXT].toColorHex()
        mProperties["ColorBarNormal"] = DEFAULT_COLOR_LIST[COLOR_BAR_NORMAL].toColorHex()
        mProperties["ColorBarWarn"] = DEFAULT_COLOR_LIST[COLOR_BAR_WARN].toColorHex()
        mProperties["ColorStateError"] = DEFAULT_COLOR_LIST[COLOR_ST_ERROR].toColorHex()
        mProperties["ColorStateNone"] = DEFAULT_COLOR_LIST[COLOR_ST_NONE].toColorHex()
        mProperties["ColorStateConnecting"] = DEFAULT_COLOR_LIST[COLOR_ST_CONNECTING].toColorHex()
        mProperties["ColorStateConnected"] = DEFAULT_COLOR_LIST[COLOR_ST_CONNECTED].toColorHex()
        mProperties["ColorStateLogging"] = DEFAULT_COLOR_LIST[COLOR_ST_LOGGING].toColorHex()
        mProperties["ColorStateWriting"] = DEFAULT_COLOR_LIST[COLOR_ST_WRITING].toColorHex()
        mProperties["AlwaysPortrait"] = DEFAULT_ALWAYS_PORTRAIT.toString()
        mProperties["DisplaySize"] = DEFAULT_DISPLAY_SIZE.toString()

        write(filename, context)
    }
}