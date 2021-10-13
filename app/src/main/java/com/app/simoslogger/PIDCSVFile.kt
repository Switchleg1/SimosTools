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
                    //make room
                    pidList = pidList?.copyOf(i+1) ?: arrayOfNulls(1)

                    //make sure the length is legal
                    if(pidStrings[5]!!.toInt() != 1 || pidStrings[5]!!.toInt() != 2 || pidStrings[5]!!.toInt() != 4)
                        throw throw RuntimeException("Unexpected pid length: ${pidStrings[5]!!}")

                    //convert address
                    val l = parseLong(pidStrings[4]!!.substringAfter("0x"), 16)

                    //Build did
                    pidList[i++] = DIDStruct(l,
                                            pidStrings[5]!!.toInt(),
                                            pidStrings[6]!!.toBoolean(),
                                            pidStrings[7]!!.toFloat(),
                                            pidStrings[8]!!.toFloat(),
                                            pidStrings[9]!!.toFloat(),
                                            pidStrings[10]!!.toFloat(),
                                            pidStrings[11]!!.toFloat(),
                                            0.0f,
                                            pidStrings[2]!!,
                                            pidStrings[3]!!,
                                            pidStrings[0]!!,
                                            pidStrings[1]!!)
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
                    val addressString = if((did.address.toInt() and 0xFFFF0000.toInt()) != 0) {
                        "0x${did.address.toInt().toHex()}"
                    } else {
                        "0x${did.address.toShort().toHex()}"
                    }

                    val writeString = "${did.name},${did.unit}," +
                                        "${did.equation},${did.format}," +
                                        "${addressString},${did.length}," +
                                        "${did.signed},${did.progMin}," +
                                        "${did.progMax},${did.warnMin}," +
                                        "${did.warnMax},${did.smoothing}"

                    outStream.write((writeString + "\n").toByteArray())
                }
            }

            outStream.close()

            return true
        }

        return false
    }
}