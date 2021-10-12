package com.app.simoslogger

import android.content.Context
import android.util.Log
import java.io.*
import java.lang.Long.parseLong

object PIDCSVFile {
    private val TAG = "PIDCSVFile"

    fun read(fileName: String?, context: Context?):Array<DIDStruct?>? {
        if(context == null)
            return null

        val path = context.getExternalFilesDir("")
        val csvFile = File(path, "/$fileName")
        if (!csvFile.exists()) {
            Log.i(TAG, "file does not exist.")
            return null
        }

        val inStream = BufferedReader(InputStreamReader(FileInputStream(csvFile)))
        if(!inStream.ready()) {
            Log.i(TAG, "file is empty.")
            return null
        }

        val cfgLine = inStream.readLine()
        if(cfgLine == CSV_CFG_LINE + "\n") {
            Log.i(TAG, "config line does not match")
            return null
        }

        var i = 0
        var pidList: Array<DIDStruct?>? = null
        while(inStream.ready() && i < MAX_PIDS) {
            var pidString = inStream.readLine()
            val pidStrings: Array<String?> = arrayOfNulls(CSV_VALUE_COUNT)

            var d = 0
            while(d < CSV_VALUE_COUNT-1 && pidString != pidString.substringBefore(",")) {
                pidStrings[d] = pidString.substringBefore(",")
                pidString = pidString.substringAfter(",")
                Log.i(TAG, "PID $i,$d: ${pidStrings[d]}")
                d++
            }
            pidStrings[d++] = pidString

            if(d == CSV_VALUE_COUNT) {
                try {
                    pidList = pidList?.copyOf(i+1) ?: arrayOfNulls(1)

                    val l = parseLong(pidStrings[0]!!.substringAfter("0x"), 16)
                    pidList[i++] = DIDStruct(l,
                                            pidStrings[1]!!.toInt(),
                                            pidStrings[2]!!.toBoolean(),
                                            pidStrings[3]!!.toFloat(),
                                            pidStrings[4]!!.toFloat(),
                                            pidStrings[5]!!.toFloat(),
                                            pidStrings[6]!!.toFloat(),
                                            pidStrings[7]!!.toFloat(),
                                            0.0f,
                                            pidStrings[8]!!,
                                            pidStrings[9]!!,
                                            pidStrings[10]!!,
                                            pidStrings[11]!!)
                } catch(e: Exception) {
                    Log.e(TAG, "Unable to create DIDStructure ${pidList?.count()}")
                    return null
                }
            }
        }

        return pidList
    }

    fun write(fileName: String?, context: Context?, pidList: Array<DIDStruct?>?): Boolean {
        pidList?.let { list ->
            if (context == null)
                return false

            val path = context.getExternalFilesDir("")
            val logFile = File(path, "/$fileName")
            if (logFile.exists()) {
                return false
            }

            logFile.createNewFile()
            val outStream = FileOutputStream(logFile)

            outStream.write((CSV_CFG_LINE + "\n").toByteArray())

            for (i in 0 until list.count()) {
                val did = list[i]
                did?.let {
                    var writeString = ""
                    writeString += if((did.address.toInt() and 0xFFFF0000.toInt()) != 0) {
                        "0x${did.address.toInt().toHex()},"
                    } else {
                        "0x${did.address.toShort().toHex()},"
                    }

                    writeString += "${did.length}," +
                                        "${did.signed},${did.progMin}," +
                                        "${did.progMax},${did.warnMin}," +
                                        "${did.warnMax},${did.smoothing}," +
                                        "${did.equation},${did.format}," +
                                        "${did.name}, ${did.unit}"

                    outStream.write((writeString + "\n").toByteArray())
                }
            }

            outStream.close()

            return true
        }

        return false
    }
}