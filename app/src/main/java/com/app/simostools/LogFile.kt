package com.app.simostools

import android.content.Context
import android.provider.MediaStore
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import java.io.*

object LogFile {
    private val TAG = "LogFile"
    private var mOutputStream: OutputStream? = null

    fun create(fileName: String?, context: Context?) {
        if(context == null)
            return

        close()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ConfigSettings.OUT_DIRECTORY.toDirectory() == DirectoryList.APP || RequiredPermissions.READ_STORAGE.result == PackageManager.PERMISSION_DENIED) {
            val path = context.getExternalFilesDir("")
            val logFile = File(path, "/$fileName")
            if (!logFile.exists()) {
                logFile.createNewFile()
            }

            mOutputStream = FileOutputStream(logFile)
        } else {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/my-custom-type")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, ConfigSettings.OUT_DIRECTORY.toDirectory().location)
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                mOutputStream = resolver.openOutputStream(uri!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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