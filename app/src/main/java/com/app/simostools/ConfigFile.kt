package com.app.simostools

import android.content.Context
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
            when(val subKey = key.substringBefore(".")) {
                UDSLoggingMode.values()[0].key -> {
                    val mode = UDSLoggingMode.values().find {it.cfgName == value}
                    mode?.let {
                        UDSLogger.setMode(it)
                    }
                }
                GearRatios.values()[0].key -> {
                    val f = value.toFloat()
                    val gearString = key.substringAfter(".")
                    val gear = GearRatios.values().find {it.gear == gearString}
                    gear?.let {
                        it.ratio = f
                    }
                }
                ColorList.values()[0].key -> {
                    val name = key.substringAfter(".")
                    val l = parseLong(value, 16)
                    val color = ColorList.values().find {it.cfgName == name}
                    color?.let {
                        it.value = l.toColorInt()
                    }
                }
                else -> {
                    if(subKey == ConfigSettings.DEBUG_LOG.cfgName) {
                        DebugLog.setFlags(value.toInt())
                    } else {
                        val cfg = ConfigSettings.values().find { it.cfgName == subKey }
                        cfg?.set(value)
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
        GearRatios.values().forEach {
            mProperties["${it.key}.${it.gear}"] = it.ratio.toString()
        }
        ColorList.values().forEach {
            mProperties["${it.key}.${it.cfgName}"] = it.value.toColorHex()
        }
        ConfigSettings.values().forEach {
            mProperties[it.cfgName] = it.toString()
        }

        write(filename, context)
    }
}