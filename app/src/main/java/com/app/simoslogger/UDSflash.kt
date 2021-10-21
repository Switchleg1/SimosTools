package com.app.simoslogger

import android.net.Uri

object UDSFlasher {
    private val TAG = "UDSflash"
    private var mTask = FLASH_ECU_CAL_SUBTASK.NONE
    private var mUri:Uri? = null
    private var mLastString: String = ""

    fun getInfo(): String {
        return mLastString
    }

    fun finished(): Boolean {
        return mTask == FLASH_ECU_CAL_SUBTASK.NONE
    }

    fun setUri(uri: Uri) {
        mUri = uri
    }

    fun buildFlashCAL(ticks: Int): ByteArray {
        return byteArrayOf()
    }

    fun processFlashCAL(ticks: Int, buff: ByteArray?): UDSReturn {
        buff?.let {

            return UDSReturn.ERROR_UNKNOWN
        }

        return UDSReturn.ERROR_NULL
    }
}