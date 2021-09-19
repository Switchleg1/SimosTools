package com.app.vwflashtools

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

    fun put(key: String, value: String) {
        mProperties[key] = value
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

    fun get(key: String): String {
        return mProperties[key].toString()
    }

    fun count(): Int {
        return mProperties.count()
    }

    private fun processKey(key: String, value: String) {
        val p = Pattern.compile("^PID(\\d+).(\\S+)", Pattern.CASE_INSENSITIVE)
        val m = p.matcher(key)

        if(!m.matches()) {
            Log.e(TAG, "Invalid key: $key")
            return
        }
        val pidStr: String? = m.group(1)
        var pidNumber = -1
        if(pidStr != null)
            pidNumber = pidStr.toInt()
        val pidVar = m.group(2)

        Log.i(TAG, "PID[$pidNumber][$pidVar]: $value")

        if((pidNumber < DIDList.count()) and (pidNumber >= 0)) {
            when(pidVar) {
                "Address" -> {
                    val pAddress = Pattern.compile("^[0-9A-F]+\$", Pattern.CASE_INSENSITIVE)
                    val mAddress = pAddress.matcher(value)
                    if(mAddress.matches() and (value.length == 4)) {
                        DIDList[pidNumber].address = Integer.decode("0x$value")
                        Log.i(TAG, DIDList[pidNumber].address.toHex())
                    }
                }
                "Length" -> {
                    DIDList[pidNumber].length = value.toInt()
                }
                "Equation" -> {
                    DIDList[pidNumber].equation = value.toInt()
                }
                "Signed" -> {
                    DIDList[pidNumber].signed = value.toBoolean()
                }
                "Min" -> {
                    DIDList[pidNumber].min = value.toFloat()
                }
                "Max" -> {
                    DIDList[pidNumber].max = value.toFloat()
                }
                "WarnMin" -> {
                    DIDList[pidNumber].warnMin = value.toFloat()
                }
                "WarnMax" -> {
                    DIDList[pidNumber].warnMax = value.toFloat()
                }
                "Format" -> {
                    DIDList[pidNumber].format = value
                }
                "Name" -> {
                    DIDList[pidNumber].name = value
                }
                "Unit" -> {
                    DIDList[pidNumber].unit = value
                }
            }
        }
    }

    private fun writeDefaultConfig(filename: String?, context: Context?) {
        for(i in 0 until DIDList.count()) {
            put("PID$i.Address", DIDList[i].address.toShort().toHex())
            put("PID$i.Length", DIDList[i].length.toString())
            put("PID$i.Equation", DIDList[i].equation.toString())
            put("PID$i.Signed", DIDList[i].signed.toString())
            put("PID$i.Min", DIDList[i].min.toString())
            put("PID$i.Max", DIDList[i].max.toString())
            put("PID$i.WarnMin", DIDList[i].warnMin.toString())
            put("PID$i.WarnMax", DIDList[i].warnMax.toString())
            put("PID$i.Format", DIDList[i].format)
            put("PID$i.Name", DIDList[i].name)
            put("PID$i.Unit", DIDList[i].unit)
        }

        write(filename, context)
    }
}