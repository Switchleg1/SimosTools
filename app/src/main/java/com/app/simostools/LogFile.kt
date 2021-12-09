package com.app.simostools

import android.content.Context
import android.provider.MediaStore
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import java.io.*

object LogFile {
    private val TAG = "LogFile"
    private var mOutputStream: OutputStream?    = null
    private var mLastFile: File?                = null
    private var mLastUri: Uri?                  = null

    fun create(fileName: String, subFolder: String, context: Context?) {
        context?.let {
            close()

            if (ConfigSettings.OUT_DIRECTORY.toDirectory() == DirectoryList.APP || RequiredPermissions.READ_STORAGE.result == PackageManager.PERMISSION_DENIED) {
                val path = context.getExternalFilesDir(subFolder)
                path?.let {
                    if (!path.exists())
                        path.mkdir()

                    val logFile = File(path, "/$fileName")
                    if (!logFile.exists()) {
                        logFile.createNewFile()
                    }

                    mOutputStream = FileOutputStream(logFile)
                    mLastFile = logFile
                    mLastUri = null
                } ?: DebugLog.w(TAG, "Unable to open file for logging: $subFolder/$fileName")
            } else {
                //get file directory
                var fileNameDir = ConfigSettings.OUT_DIRECTORY.toDirectory().location
                if (subFolder != "")
                    fileNameDir += "/$subFolder"

                //try to open the file
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        val dirName = ConfigSettings.OUT_DIRECTORY.toDirectory().location
                        val path = File(Environment.getExternalStoragePublicDirectory(dirName), "/$subFolder")
                        if (!path.exists())
                            path.mkdir()

                        val logFile = File(path, "/$fileName")
                        if (!logFile.exists()) {
                            logFile.createNewFile()
                        }

                        mOutputStream = FileOutputStream(logFile)
                        mLastFile = logFile
                        mLastUri = null
                    } else {
                        val contentValues = ContentValues()
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        contentValues.put(
                            MediaStore.MediaColumns.MIME_TYPE,
                            "application/my-custom-type"
                        )
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, fileNameDir)
                        val resolver = context.contentResolver
                        val uri = resolver.insert(
                            MediaStore.Files.getContentUri("external"),
                            contentValues
                        )

                        mOutputStream = resolver.openOutputStream(uri!!)
                        mLastFile = null
                        mLastUri = uri
                    }
                    DebugLog.i(TAG, "Log opened: $fileNameDir/$fileName")
                } catch (e: Exception) {
                    DebugLog.w(TAG, "Unable to open file for logging: $fileNameDir/$fileName")
                }
            }
        } ?: DebugLog.w(TAG, "Unable to open file for logging, invalid context")
    }

    fun close() {
       if(mOutputStream != null) {
           DebugLog.i(TAG, "Log closed.")
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

    fun getLastFile(): File? {
        return mLastFile
    }

    fun getLastUri(): Uri? {
        return mLastUri
    }
}