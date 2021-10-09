package com.app.vwflashtools

import android.content.Context
import android.graphics.Color
import android.os.Environment
import android.util.Log
import androidx.core.graphics.toColorInt
import androidx.core.graphics.toColorLong
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Long.parseLong
import java.util.*
import java.util.regex.Pattern
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
        var p = Pattern.compile("^Config.(\\S+)", Pattern.CASE_INSENSITIVE)
        var m = p.matcher(key)

        if (m.matches()) {
            processConfigKey(m.group(1)?: "", value)
            return
        }

        p = Pattern.compile("^PID.([0-9A-F]+).(\\d+).(\\S+)", Pattern.CASE_INSENSITIVE)
        m = p.matcher(key)

        if (m.matches()) {
            processPIDKey(m.group(1)?: "", m.group(2)?: "", m.group(3)?: "", value)
            return
        }
    }

    private fun processConfigKey(variable: String, value: String) {
        Log.i(TAG, "Config[$variable]: $value")

        try {
            when (variable) {
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
                    if(f > 0f && f < 0.001f)
                        Settings.dragCoefficient = f
                }
                "ColorWarn" -> {
                    val l = parseLong(value, 16)
                    Settings.colorWarn = l.toInt()
                }
                "ColorNormal" -> {
                    val l = parseLong(value, 16)
                    Settings.colorNormal = l.toInt()
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
        } catch (e: NumberFormatException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun processPIDKey(type: String, number: String, variable: String, value: String) {
        Log.i(TAG, "PID[$type][$number][$variable]: $value")

        var pidList: List<DIDStruct>? = null
        when(type) {
            "22" -> pidList = DIDs.list22
            "3E" -> pidList = DIDs.list3E
        }

        if(pidList == null) {
            Log.i(TAG, "Invalid logging type: $type")
            return
        }

        try {
            val pidNumber = number.toInt()
            if((pidNumber < pidList.count()) and (pidNumber >= 0)) {
                when (variable) {
                    "Address" -> {
                        val pAddress = Pattern.compile("^[0-9A-F]+\$", Pattern.CASE_INSENSITIVE)
                        val mAddress = pAddress.matcher(value)
                        if (mAddress.matches() and (((type == "22") and (value.length == 4)) or ((type == "3E") and (value.length == 8)))) {
                            pidList[pidNumber].address = parseLong(value, 16)
                        }
                    }
                    "Length" -> {
                        val i = value.toInt()
                        pidList[pidNumber].length = i
                    }
                    "Equation" -> {
                        val i = value.toInt()
                        pidList[pidNumber].equation = i
                    }
                    "Signed" -> {
                        val b = value.toBoolean()
                        pidList[pidNumber].signed = b
                    }
                    "ProgMin" -> {
                        val f = value.toFloat()
                        pidList[pidNumber].progMin = f
                    }
                    "ProgMax" -> {
                        val f = value.toFloat()
                        pidList[pidNumber].progMax = f
                    }
                    "WarnMin" -> {
                        val f = value.toFloat()
                        pidList[pidNumber].warnMin = f
                    }
                    "WarnMax" -> {
                        val f = value.toFloat()
                        pidList[pidNumber].warnMax = f
                    }
                    "Smoothing" -> {
                        val f = value.toFloat()
                        pidList[pidNumber].smoothing = f
                    }
                    "Format" -> pidList[pidNumber].format = value
                    "Name" -> pidList[pidNumber].name = value
                    "Unit" -> pidList[pidNumber].unit = value
                }
            }
        } catch (e: NumberFormatException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun writeDefaultConfig(filename: String?, context: Context?) {
        mProperties["Config.Mode"] = "22"
        mProperties["Config.OutputDirectory"] = "Downloads"
        mProperties["Config.UpdateRate"] = (11-DEFAULT_UPDATE_RATE).toString()
        mProperties["Config.InvertCruise"] = DEFAULT_INVERT_CRUISE.toString()
        mProperties["Config.KeepScreenOn"] = DEFAULT_KEEP_SCREEN_ON.toString()
        mProperties["Config.PersistDelay"] = DEFAULT_PERSIST_DELAY.toString()
        mProperties["Config.PersistQDelay"] = DEFAULT_PERSIST_Q_DELAY.toString()
        mProperties["Config.CalculateHP"] = DEFAULT_CALCULATE_HP.toString()
        mProperties["Config.UseMS2Torque"] = DEFAULT_USE_MS2.toString()
        mProperties["Config.TireDiameter"] = DEFAULT_TIRE_DIAMETER.toString()
        mProperties["Config.CurbWeight"] = DEFAULT_CURB_WEIGHT.toString()
        mProperties["Config.DragCoefficient"] = DEFAULT_DRAG_COEFFICIENT.toString()
        mProperties["Config.GearRatio.1"] = DEFAULT_GEAR_RATIOS[0].toString()
        mProperties["Config.GearRatio.2"] = DEFAULT_GEAR_RATIOS[1].toString()
        mProperties["Config.GearRatio.3"] = DEFAULT_GEAR_RATIOS[2].toString()
        mProperties["Config.GearRatio.4"] = DEFAULT_GEAR_RATIOS[3].toString()
        mProperties["Config.GearRatio.5"] = DEFAULT_GEAR_RATIOS[4].toString()
        mProperties["Config.GearRatio.6"] = DEFAULT_GEAR_RATIOS[5].toString()
        mProperties["Config.GearRatio.7"] = DEFAULT_GEAR_RATIOS[6].toString()
        mProperties["Config.GearRatio.Final"] = DEFAULT_GEAR_RATIOS[7].toString()
        mProperties["Config.ColorWarn"] = DEFAULT_COLOR_WARN.toHex()
        mProperties["Config.ColorNormal"] = DEFAULT_COLOR_NORMAL.toHex()
        mProperties["Config.AlwaysPortrait"] = DEFAULT_ALWAYS_PORTRAIT.toString()
        mProperties["Config.DisplaySize"] = DEFAULT_DISPLAY_SIZE.toString()
        for(i in 0 until DIDs.list22.count()) {
            mProperties["PID.22.${i.toTwo()}.Address"] = DIDs.list22[i].address.toShort().toHex()
            mProperties["PID.22.${i.toTwo()}.Length"] = DIDs.list22[i].length.toString()
            mProperties["PID.22.${i.toTwo()}.Equation"] = DIDs.list22[i].equation.toString()
            mProperties["PID.22.${i.toTwo()}.Signed"] = DIDs.list22[i].signed.toString()
            mProperties["PID.22.${i.toTwo()}.Format"] = DIDs.list22[i].format
            mProperties["PID.22.${i.toTwo()}.Name"] = DIDs.list22[i].name
            mProperties["PID.22.${i.toTwo()}.Unit"] = DIDs.list22[i].unit
            mProperties["PID.22.${i.toTwo()}.ProgMin"] = DIDs.list22[i].progMin.toString()
            mProperties["PID.22.${i.toTwo()}.ProgMax"] = DIDs.list22[i].progMax.toString()
            mProperties["PID.22.${i.toTwo()}.WarnMin"] = DIDs.list22[i].warnMin.toString()
            mProperties["PID.22.${i.toTwo()}.WarnMax"] = DIDs.list22[i].warnMax.toString()
            mProperties["PID.22.${i.toTwo()}.Smoothing"] = DIDs.list22[i].smoothing.toString()
        }

        for(i in 0 until DIDs.list3E.count()) {
            mProperties["PID.3E.${i.toTwo()}.Address"] = DIDs.list3E[i].address.toInt().toHex()
            mProperties["PID.3E.${i.toTwo()}.Length"] = DIDs.list3E[i].length.toString()
            mProperties["PID.3E.${i.toTwo()}.Equation"] = DIDs.list3E[i].equation.toString()
            mProperties["PID.3E.${i.toTwo()}.Signed"] = DIDs.list3E[i].signed.toString()
            mProperties["PID.3E.${i.toTwo()}.Format"] = DIDs.list3E[i].format
            mProperties["PID.3E.${i.toTwo()}.Name"] = DIDs.list3E[i].name
            mProperties["PID.3E.${i.toTwo()}.Unit"] = DIDs.list3E[i].unit
            mProperties["PID.3E.${i.toTwo()}.ProgMin"] = DIDs.list3E[i].progMin.toString()
            mProperties["PID.3E.${i.toTwo()}.ProgMax"] = DIDs.list3E[i].progMax.toString()
            mProperties["PID.3E.${i.toTwo()}.WarnMin"] = DIDs.list3E[i].warnMin.toString()
            mProperties["PID.3E.${i.toTwo()}.WarnMax"] = DIDs.list3E[i].warnMax.toString()
            mProperties["PID.3E.${i.toTwo()}.Smoothing"] = DIDs.list3E[i].smoothing.toString()
        }

        write(filename, context)
    }
}