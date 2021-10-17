package com.app.simoslogger

import android.content.Context
import android.util.Log
import java.io.*
import java.lang.Exception
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DebugLog {
    private val TAG = "DebugLog"
    private var mBufferedWriter: BufferedWriter? = null

    fun create(fileName: String?, context: Context?) {
        context?.let {
            close()

            try {
                val path = it.getExternalFilesDir("")
                val logFile = File(path, "/$fileName")
                if (!logFile.exists()) {
                    logFile.createNewFile()
                }
                mBufferedWriter = BufferedWriter(FileWriter(logFile, true))
            } catch(e: Exception) {
                Log.e(TAG, "Error opening debug log", e)
            }
        }
    }

    fun close() {
        try {
            mBufferedWriter?.close()
            mBufferedWriter = null
        } catch (e: Exception) {
            Log.e(TAG, "File already closed", e)
        }
    }

    fun i(tag: String, text: String) {
        Log.i(tag, text)

        if((Settings.logFlags and LOG_INFO) == 0)
            return

        add("${timeStamp()} [I] $tag: $text")
        newLine()
    }

    fun w(tag: String, text: String) {
        Log.w(tag, text)

        if((Settings.logFlags and LOG_WARNING) == 0)
            return

        add("${timeStamp()} [W] $tag: $text")
        newLine()
    }

    fun d(tag: String, text: String) {
        Log.d(tag, text)

        if((Settings.logFlags and LOG_DEBUG) == 0)
            return

        add("${timeStamp()} [D] $tag: $text")
        newLine()
    }

    fun e(tag: String, text: String, e: Exception) {
        Log.e(tag, text, e)

        if((Settings.logFlags and LOG_EXCEPTION) == 0)
            return

        add("${timeStamp()} [E] $tag: $text")
        newLine()
    }

    fun c(tag: String, buff: ByteArray?, from: Boolean) {
        buff?.let {
            if ((Settings.logFlags and LOG_COMMUNICATIONS) == 0)
                return

            val dirString = if (from) "${it.count()} ->"
                else "${it.count()}  <-"

            add("${timeStamp()} [C] $tag: [$dirString] ${it.toHex()}")
            newLine()
        }
    }

    private fun newLine() {
        try {
            mBufferedWriter?.let {
                it.newLine()
                it.flush()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting new debug line", e)
        }
    }

    private fun add(text: String) {
        if(Settings.logFlags == LOG_NONE)
            return

        try {
            mBufferedWriter?.append(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error appending debug log", e)
        }
    }

    private fun timeStamp(): String {
        return DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneOffset.systemDefault())
            .format(Instant.now())
    }
}