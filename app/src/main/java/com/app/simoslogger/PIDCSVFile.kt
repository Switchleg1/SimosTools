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

        //get file path and check if file exists
        val path = context.getExternalFilesDir("")
        val csvFile = File(path, "/$fileName")
        if (!csvFile.exists()) {
            Log.i(TAG, "file does not exist.")
            return null
        }

        return readStream(FileInputStream(csvFile))
    }

    fun readStream(fileStream: InputStream?):Array<DIDStruct?>? {

        val inStream = BufferedReader(InputStreamReader(fileStream))
        //is the file empty?
        if(!inStream.ready()) {
            Log.i(TAG, "file is empty.")
            return null
        }

        //check header
        val cfgLine = inStream.readLine()
        if(cfgLine == CSV_CFG_LINE + "\n") {
            Log.i(TAG, "config line does not match")
            return null
        }

        //read PIDS
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
            pidStrings[d] = pidString
            Log.i(TAG, "PID $i,$d: ${pidStrings[d]}")
            d++

            if(d == CSV_VALUE_COUNT) {
                try {
                    //make room
                    pidList = pidList?.copyOf(i+1) ?: arrayOfNulls(1)

                    //make sure the length is legal
                    if(pidStrings[5]!!.toInt() != 1 && pidStrings[5]!!.toInt() != 2 && pidStrings[5]!!.toInt() != 4)
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
            } else {
                return null
            }
        }

        return pidList
    }

    fun write(fileName: String?, context: Context?, pidList: Array<DIDStruct?>?, overWrite: Boolean): Boolean {
        pidList?.let { list ->
            if (context == null)
                return false

            //get filename and check if it exists
            val path = context.getExternalFilesDir("")
            val csvFile = File(path, "/$fileName")
            if (csvFile.exists()) {
                if(overWrite) csvFile.delete()
                    else return false
            }

            //Create new file
            csvFile.createNewFile()
            val outStream = FileOutputStream(csvFile)

            //write header
            outStream.write((CSV_CFG_LINE + "\n").toByteArray())

            //write pids
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

            //close file and return
            outStream.close()

            return true
        }

        return false
    }
}