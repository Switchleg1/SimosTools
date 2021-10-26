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
            when(key.substringBefore(".")) {
                UDSLoggingMode.values()[0].key -> {
                    val mode = UDSLoggingMode.values().find {it.cfgName == value}
                    mode?.let {
                        UDSLogger.setMode(it)
                    }
                }
                DirectoryList.values()[0].key -> {
                    val dir = DirectoryList.values().find {it.cfgName == value}
                    dir?.let {
                        Settings.outputDirectory = it
                    }
                }
                "UpdateRate" -> {
                    val i = value.toInt()
                    if(i in 1..10)
                        Settings.updateRate = 11 - i
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
                GearRatios.values()[0].key -> {
                    val f = value.toFloat()
                    val gearString = key.substringAfter(".")
                    val gear = GearRatios.values().find {it.gear == gearString}
                    gear?.let {
                        it.ratio = f
                    }
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
                ColorList.values()[0].key -> {
                    val name = key.substringAfter(".")
                    val l = parseLong(value, 16)
                    val color = ColorList.values().find {it.cfgName == name}
                    color?.let {
                        it.value = l.toColorInt()
                    }
                }
                "AlwaysPortrait" -> {
                    val b = value.toBoolean()
                    Settings.alwaysPortrait = b
                }
                "DebugLogFlags" -> {
                    val i = value.toInt()
                    DebugLog.setFlags(i)
                }
                DisplayType.values()[0].key -> {
                    val type = DisplayType.values().find {it.cfgName == value}
                    type?.let {
                        Settings.displayType = it
                    }
                }
            }
        } catch (e: Exception) {
            DebugLog.e(TAG, "Exception parsing $key=$value: ", e)
        }
    }

    private fun writeDefaultConfig(filename: String?, context: Context?) {
        DebugLog.i(TAG, "Writing default config file.")

        mProperties[UDSLoggingMode.values()[0].key] = UDSLoggingMode.MODE_22.cfgName
        mProperties[DirectoryList.values()[0].key] = DirectoryList.APP.cfgName
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
        mProperties["DebugLogFlags"] = DebugLog.getFlags().toString()
        mProperties[DisplayType.values()[0].key] = DisplayType.BAR.cfgName
        GearRatios.values().forEach {
            mProperties["${it.key}.${it.gear}"] = it.ratio.toString()
        }
        ColorList.values().forEach {
            mProperties["${it.key}.${it.cfgName}"] = it.value.toColorHex()
        }

        write(filename, context)
    }
}