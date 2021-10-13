package com.app.simoslogger

import android.content.Context
import java.io.*

object DebugLog {
    private val TAG = "DebugLog"
    private var mBufferedWriter: BufferedWriter? = null

    fun create(fileName: String?, context: Context?) {
        if(!LOG_DEBUG)
            return

        if(context == null)
            return

        close()

        val path = context.getExternalFilesDir("")
        val logFile = File(path, "/$fileName")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }

        mBufferedWriter = BufferedWriter(FileWriter(logFile, true))
    }

    fun close() {
        if(!LOG_DEBUG)
            return

        if(mBufferedWriter != null) {
            mBufferedWriter!!.close()
            mBufferedWriter = null
        }
    }

    fun add(text: String?) {
        if(!LOG_DEBUG)
            return

        mBufferedWriter?.append(text)
    }

    fun addLine(text: String?) {
        add(text)
        mBufferedWriter?.newLine()
        mBufferedWriter?.flush()
    }
}