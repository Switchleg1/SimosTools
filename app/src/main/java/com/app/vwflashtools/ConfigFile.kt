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

        if((pidNumber < DIDs.list.count()) and (pidNumber >= 0)) {
            when(pidVar) {
                "Address" -> {
                    val pAddress = Pattern.compile("^[0-9A-F]+\$", Pattern.CASE_INSENSITIVE)
                    val mAddress = pAddress.matcher(value)
                    if(mAddress.matches() and (value.length == 4)) {
                        DIDs.list[pidNumber].address = Integer.decode("0x$value")
                    }
                }
                "Length" -> {
                    DIDs.list[pidNumber].length = value.toInt()
                }
                "Equation" -> {
                    DIDs.list[pidNumber].equation = value.toInt()
                }
                "Signed" -> {
                    DIDs.list[pidNumber].signed = value.toBoolean()
                }
                "Min" -> {
                    DIDs.list[pidNumber].min = value.toFloat()
                }
                "Max" -> {
                    DIDs.list[pidNumber].max = value.toFloat()
                }
                "WarnMin" -> {
                    DIDs.list[pidNumber].warnMin = value.toFloat()
                }
                "WarnMax" -> {
                    DIDs.list[pidNumber].warnMax = value.toFloat()
                }
                "Format" -> {
                    DIDs.list[pidNumber].format = value
                }
                "Name" -> {
                    DIDs.list[pidNumber].name = value
                }
                "Unit" -> {
                    DIDs.list[pidNumber].unit = value
                }
            }
        }
    }

    private fun writeDefaultConfig(filename: String?, context: Context?) {
        for(i in 0 until DIDs.list.count()) {
            mProperties["PID${i.toTwo()}.Address"] = DIDs.list[i].address.toShort().toHex()
            mProperties["PID${i.toTwo()}.Length"] = DIDs.list[i].length.toString()
            mProperties["PID${i.toTwo()}.Equation"] = DIDs.list[i].equation.toString()
            mProperties["PID${i.toTwo()}.Signed"] = DIDs.list[i].signed.toString()
            mProperties["PID${i.toTwo()}.Min"] = DIDs.list[i].min.toString()
            mProperties["PID${i.toTwo()}.Max"] = DIDs.list[i].max.toString()
            mProperties["PID${i.toTwo()}.WarnMin"] = DIDs.list[i].warnMin.toString()
            mProperties["PID${i.toTwo()}.WarnMax"] = DIDs.list[i].warnMax.toString()
            mProperties["PID${i.toTwo()}.Format"] = DIDs.list[i].format
            mProperties["PID${i.toTwo()}.Name"] = DIDs.list[i].name
            mProperties["PID${i.toTwo()}.Unit"] = DIDs.list[i].unit
        }

        write(filename, context)
    }
}