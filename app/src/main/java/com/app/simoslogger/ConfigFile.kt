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
                "Mode" -> {
                    val mode = UDSLoggingMode.values().find {it.value == value}
                    mode?.let {
                        UDSLogger.setMode(it)
                    }
                }
                "List" -> {
                    val mode = PIDIndex.values().find {it.name == value}
                    mode?.let {
                        PIDs.setIndex(it)
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
                "GearRatio" -> {
                    val f = value.toFloat()
                    val gear = key.substringAfter(".")
                    (GearRatios.values().find {it.gear == gear})?.ratio = f
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
                "Color" -> {
                    val name = key.substringAfter(".")
                    val l = parseLong(value, 16)
                    ColorList.valueOf(name).value = l.toColorInt()
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
        GearRatios.values().forEach {
            mProperties["GearRatio.${it.gear}"] = it.ratio.toString()
        }
        ColorList.values().forEach {
            mProperties["Color.${it.name}"] = it.value.toColorHex()
        }

        write(filename, context)
    }
}