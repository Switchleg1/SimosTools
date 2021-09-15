package com.app.vwflashtools

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

object LogFile {
    private val TAG = "LogFile"
    private var mLogFile: File? = null
    private var mBufferedWriter: BufferedWriter? = null

    fun create(fileName: String?, context: Context?) {
        close()

        val path = context?.getExternalFilesDir("")
        Log.i(TAG, "$path/$fileName")
        mLogFile = File(path, "/$fileName")
        if(mLogFile == null)
            return

        try {
            mLogFile!!.createNewFile()
            mBufferedWriter = BufferedWriter(FileWriter(mLogFile, true))
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun close() {
        if(mBufferedWriter != null) {
            mBufferedWriter!!.close()
            mBufferedWriter = null
        }

        mLogFile = null
    }

    fun add(text: String?) {
        if(mLogFile == null || mBufferedWriter == null)
            return

        try {
            mBufferedWriter!!.append(text)
            mBufferedWriter!!.newLine()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}