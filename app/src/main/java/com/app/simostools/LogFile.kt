package com.app.simostools

import android.content.Context
import android.provider.MediaStore
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import java.io.*

object LogFile {
    private val TAG = "LogFile"
    private var mOutputStream: OutputStream?    = null
    private var mLastFileName: String           = ""
    private var mLastFileDir: String            = ""
    private var mLastUri: Uri?                  = null

    fun create(fileName: String, subFolder: String, context: Context?) {
        if(context == null)
            return

        close()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ConfigSettings.OUT_DIRECTORY.toDirectory() == DirectoryList.APP || RequiredPermissions.READ_STORAGE.result == PackageManager.PERMISSION_DENIED) {
            val path = context.getExternalFilesDir(subFolder)
            path?.let {
                if (!path.exists())
                    path.mkdir()

                val logFile = File(path, "/$fileName")
                if (!logFile.exists()) {
                    logFile.createNewFile()
                }

                mOutputStream = FileOutputStream(logFile)
                mLastFileName = fileName
                mLastFileDir = subFolder
                mLastUri = null
            }
        } else {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/my-custom-type")
                if(subFolder != "") contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "${ConfigSettings.OUT_DIRECTORY.toDirectory().location}/$subFolder")
                else contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, ConfigSettings.OUT_DIRECTORY.toDirectory().location)
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                mOutputStream = resolver.openOutputStream(uri!!)
                mLastFileName = ""
                mLastFileDir = ""
                mLastUri = uri
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

    fun add(text: String) {
        mOutputStream?.write(text.toByteArray())
    }

    fun addLine(text: String) {
        add(text + "\n")
    }

    fun getLastFileName(): String {
        return mLastFileName
    }

    fun getLastFileDir(): String {
        return mLastFileDir
    }

    fun getLastUri(): Uri? {
        return mLastUri
    }
}