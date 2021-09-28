package com.app.vwflashtools

import android.content.Context
import android.provider.MediaStore
import android.content.ContentValues
import java.io.*

object LogFile {
    private val TAG = "LogFile"
    private var mOutputStream: OutputStream? = null

    fun create(fileName: String?, context: Context?) {
        close()

        try {
            val resolver = context!!.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/my-custom-type")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, LOG_DIRECTORY)
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            mOutputStream = resolver.openOutputStream(uri!!)
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
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