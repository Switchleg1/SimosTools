package com.app.simoslogger

import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.OutputStream

object DebugLog {
    private val TAG = "DebugLog"
    private var mOutputStream: OutputStream? = null

    /*private fun create() {
        if(!LOG_COMMUNICATIONS)
            return

        logClose()

        val path = applicationContext.getExternalFilesDir("")
        Log.i(TAG, "$path/data.log")
        mLogFile = File(path, "/data.log")
        if(mLogFile == null)
            return

        try {
            mLogFile!!.createNewFile()
            mBufferedWriter = BufferedWriter(FileWriter(mLogFile, true))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logClose() {
        if(!LOG_COMMUNICATIONS)
            return

        if(mBufferedWriter != null) {
            mBufferedWriter!!.close()
            mBufferedWriter = null
        }

        mLogFile = null
    }

    private fun logAdd(from: Boolean, buff: ByteArray?) {
        if(!LOG_COMMUNICATIONS)
            return

        if(mLogFile == null || mBufferedWriter == null || buff == null)
            return

        try {
            if(from) mBufferedWriter!!.append("->[${buff.count()}]:${buff.toHex()}")
            else mBufferedWriter!!.append("<-[${buff.count()}]:${buff.toHex()}")
            mBufferedWriter!!.newLine()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}*/
}