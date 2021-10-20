package com.app.simoslogger

import android.content.Context
import android.os.Environment
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
        DebugLog.i(TAG, "Writing config file.")
        try {

            val path = context?.getExternalFilesDir("")
            val propertiesFile = File(path, "/$fileName")
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile()
            }

            val propertiesOutputStream = FileOutputStream(propertiesFile)
            mProperties.store(propertiesOutputStream, "save to properties file")
            propertiesOutputStream.close()
            DebugLog.i(TAG, "successful.")
        } catch(e: Exception) {
            DebugLog.e(TAG, "unable to write config file.", e)
            DebugLog.i(TAG, "failed.")
        }
    }

    fun read(fileName: String?, context: Context?) {
        DebugLog.i(TAG, "Reading config file.")

        try {
            val path = context?.getExternalFilesDir("")
            val propertiesFile = File(path, "/$fileName")
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile()
                writeDefaultConfig(fileName, context)
            }

            val inputStream = FileInputStream(propertiesFile)
            mProperties.load(inputStream)

            mProperties.forEach { (k, v) ->
                processKey(k.toString(), v.toString())
            }
            inputStream.close()
            DebugLog.i(TAG, "successful.")
        } catch(e: Exception) {
            DebugLog.e(TAG, "unable to read config file.", e)
            DebugLog.i(TAG, "failed.")
        }
    }

    fun set(key: String, value: String) {
        mProperties[key] = value
    }

    fun get(key: String): String {
        return mProperties[key].toString()
    }

    private fun processKey(key: String, value: String) {
        DebugLog.d(TAG, "Found $key=$value")

        try {
            when (key) {
                "Mode" -> {
                    when (value) {
                        "3E" -> UDSLogger.setMode(UDS_LOGGING_3E)
                        "22" -> UDSLogger.setMode(UDS_LOGGING_22)

                    }
                }
                "List" -> {
                    when (value) {
                        "A" -> PIDs.setIndex(PID_LIST_A)
                        "B" -> PIDs.setIndex(PID_LIST_B)
                        "C" -> PIDs.setIndex(PID_LIST_C)
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
                    Settings.colorList[ColorIndex.BG_NORMAL.ordinal] = l.toColorInt()
                }
                "ColorBGWarn" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.BG_WARN.ordinal] = l.toColorInt()
                }
                "ColorText" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.TEXT.ordinal] = l.toColorInt()
                }
                "ColorBarNormal" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.BAR_NORMAL.ordinal] = l.toColorInt()
                }
                "ColorBarWarn" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.BAR_WARN.ordinal] = l.toColorInt()
                }
                "ColorStateError" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.ST_ERROR.ordinal] = l.toColorInt()
                }
                "ColorStateNone" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.ST_NONE.ordinal] = l.toColorInt()
                }
                "ColorStateConnecting" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.ST_CONNECTING.ordinal] = l.toColorInt()
                }
                "ColorStateConnected" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.ST_CONNECTED.ordinal] = l.toColorInt()
                }
                "ColorStateLogging" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.ST_LOGGING.ordinal] = l.toColorInt()
                }
                "ColorStateWriting" -> {
                    val l = parseLong(value, 16)
                    Settings.colorList[ColorIndex.ST_WRITING.ordinal] = l.toColorInt()
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
                "LogFile" -> {
                    val i = value.toInt()
                    if(i in 1 .. 0xF)
                        Settings.logFlags = i
                }
            }
        } catch (e: Exception) {
            DebugLog.e(TAG, "Exception parsing $key=$value: ", e)
        }
    }

    private fun writeDefaultConfig(filename: String?, context: Context?) {
        DebugLog.i(TAG, "Writing default config file.")

        mProperties["Mode"] = "22"
        mProperties["List"] = "A"
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
        mProperties["ColorBGNormal"] = DEFAULT_COLOR_LIST[ColorIndex.BG_NORMAL.ordinal].toColorHex()
        mProperties["ColorBGWarn"] = DEFAULT_COLOR_LIST[ColorIndex.BG_WARN.ordinal].toColorHex()
        mProperties["ColorText"] = DEFAULT_COLOR_LIST[ColorIndex.TEXT.ordinal].toColorHex()
        mProperties["ColorBarNormal"] = DEFAULT_COLOR_LIST[ColorIndex.BAR_NORMAL.ordinal].toColorHex()
        mProperties["ColorBarWarn"] = DEFAULT_COLOR_LIST[ColorIndex.BAR_WARN.ordinal].toColorHex()
        mProperties["ColorStateError"] = DEFAULT_COLOR_LIST[ColorIndex.ST_ERROR.ordinal].toColorHex()
        mProperties["ColorStateNone"] = DEFAULT_COLOR_LIST[ColorIndex.ST_NONE.ordinal].toColorHex()
        mProperties["ColorStateConnecting"] = DEFAULT_COLOR_LIST[ColorIndex.ST_CONNECTING.ordinal].toColorHex()
        mProperties["ColorStateConnected"] = DEFAULT_COLOR_LIST[ColorIndex.ST_CONNECTED.ordinal].toColorHex()
        mProperties["ColorStateLogging"] = DEFAULT_COLOR_LIST[ColorIndex.ST_LOGGING.ordinal].toColorHex()
        mProperties["ColorStateWriting"] = DEFAULT_COLOR_LIST[ColorIndex.ST_WRITING.ordinal].toColorHex()
        mProperties["AlwaysPortrait"] = DEFAULT_ALWAYS_PORTRAIT.toString()
        mProperties["DisplaySize"] = DEFAULT_DISPLAY_SIZE.toString()
        mProperties["LogFile"] = DEFAULT_LOG_FLAGS.toString()

        write(filename, context)
    }
}