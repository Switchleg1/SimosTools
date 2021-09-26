package com.app.vwflashtools

import android.content.Context
import android.util.Log
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

        when(variable) {
            "Mode"     -> {
                when(value) {
                    "22" -> UDSLogger.setMode(UDS_LOGGING_22)
                    "3E" -> UDSLogger.setMode(UDS_LOGGING_3E)
                }
            }
            "UpdateRate" -> {
                Settings.updateRate = 11-value.toInt()
            }
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

        val pidNumber = number.toInt()
        if((pidNumber < pidList.count()) and (pidNumber >= 0)) {
            when(variable) {
                "Address" -> {
                    val pAddress = Pattern.compile("^[0-9A-F]+\$", Pattern.CASE_INSENSITIVE)
                    val mAddress = pAddress.matcher(value)
                    if(mAddress.matches() and (((type == "22") and (value.length == 4)) or ((type == "3E") and (value.length == 8)))) {
                        pidList[pidNumber].address = parseLong(value, 16)
                    }
                }
                "Length"    -> pidList[pidNumber].length = value.toInt()
                "Equation"  -> pidList[pidNumber].equation = value.toInt()
                "Signed"    -> pidList[pidNumber].signed = value.toBoolean()
                "ProgMin"   -> pidList[pidNumber].progMin = value.toFloat()
                "ProgMax"   -> pidList[pidNumber].progMax = value.toFloat()
                "WarnMin"   -> pidList[pidNumber].warnMin = value.toFloat()
                "WarnMax"   -> pidList[pidNumber].warnMax = value.toFloat()
                "Format"    -> pidList[pidNumber].format = value
                "Name"      -> pidList[pidNumber].name = value
                "Unit"      -> pidList[pidNumber].unit = value
            }
        }
    }

    private fun writeDefaultConfig(filename: String?, context: Context?) {
        mProperties["Config.Mode"] = "22"
        mProperties["Config.UpdateRate"] = "7"
        for(i in 0 until DIDs.list22.count()) {
            mProperties["PID.22.${i.toTwo()}.Address"] = DIDs.list22[i].address.toShort().toHex()
            mProperties["PID.22.${i.toTwo()}.Length"] = DIDs.list22[i].length.toString()
            mProperties["PID.22.${i.toTwo()}.Equation"] = DIDs.list22[i].equation.toString()
            mProperties["PID.22.${i.toTwo()}.Signed"] = DIDs.list22[i].signed.toString()
            mProperties["PID.22.${i.toTwo()}.Format"] = DIDs.list22[i].format
            mProperties["PID.22.${i.toTwo()}.Name"] = DIDs.list22[i].name
            mProperties["PID.22.${i.toTwo()}.Unit"] = DIDs.list22[i].unit

            if(i < 8) {
                mProperties["PID.22.${i.toTwo()}.ProgMin"] = DIDs.list22[i].progMin.toString()
                mProperties["PID.22.${i.toTwo()}.ProgMax"] = DIDs.list22[i].progMax.toString()
                mProperties["PID.22.${i.toTwo()}.WarnMin"] = DIDs.list22[i].warnMin.toString()
                mProperties["PID.22.${i.toTwo()}.WarnMax"] = DIDs.list22[i].warnMax.toString()
            }
        }

        for(i in 0 until DIDs.list3E.count()) {
            mProperties["PID.3E.${i.toTwo()}.Address"] = DIDs.list3E[i].address.toInt().toHex()
            mProperties["PID.3E.${i.toTwo()}.Length"] = DIDs.list3E[i].length.toString()
            mProperties["PID.3E.${i.toTwo()}.Equation"] = DIDs.list3E[i].equation.toString()
            mProperties["PID.3E.${i.toTwo()}.Signed"] = DIDs.list3E[i].signed.toString()
            mProperties["PID.3E.${i.toTwo()}.Format"] = DIDs.list3E[i].format
            mProperties["PID.3E.${i.toTwo()}.Name"] = DIDs.list3E[i].name
            mProperties["PID.3E.${i.toTwo()}.Unit"] = DIDs.list3E[i].unit

            if(i < 8) {
                mProperties["PID.3E.${i.toTwo()}.ProgMin"] = DIDs.list3E[i].progMin.toString()
                mProperties["PID.3E.${i.toTwo()}.ProgMax"] = DIDs.list3E[i].progMax.toString()
                mProperties["PID.3E.${i.toTwo()}.WarnMin"] = DIDs.list3E[i].warnMin.toString()
                mProperties["PID.3E.${i.toTwo()}.WarnMax"] = DIDs.list3E[i].warnMax.toString()
            }
        }

        write(filename, context)
    }
}