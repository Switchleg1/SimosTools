package com.app.simoslogger

import android.content.Context
import java.io.*

object DebugLog {
    private val TAG = "DebugLog"
    private var mOutputStream: OutputStream? = null

    fun create(fileName: String?, context: Context?) {
        if(context == null)
            return

        LogFile.close()

        val path = context.getExternalFilesDir("")
        val logFile = File(path, "/$fileName")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }

        mOutputStream = FileOutputStream(logFile)
    }

    fun close() {
        if(mOutputStream != null) {
            mOutputStream!!.close()
            mOutputStream = null
        }
    }

    fun add(text: String?) {
        mOutputStream?.write(text?.toByteArray())
    }

    fun addLine(text: String?) {
        add(text + "\n")
    }
}