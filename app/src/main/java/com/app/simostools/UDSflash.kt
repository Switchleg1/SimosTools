package com.app.simostools

import java.io.InputStream

object UDSFlasher {
    private val TAG = "UDSflash"
    private var mTask = FLASH_ECU_CAL_SUBTASK.NONE
    private var mFileStream: InputStream? = null
    private var mLastString: String = ""

    fun getInfo(): String {
        return mLastString
    }

    fun finished(): Boolean {
        return mTask == FLASH_ECU_CAL_SUBTASK.NONE
    }

    fun setStream(stream: InputStream?) {
        //If state == idle...
        stream?.let {
            mFileStream = stream
        }
    }

    fun startTask(ticks: Int): ByteArray {
        return byteArrayOf()
    }

    fun processPacket(ticks: Int, buff: ByteArray?): UDSReturn {
        buff?.let {

            return UDSReturn.ERROR_UNKNOWN
        }

        return UDSReturn.ERROR_NULL
    }
}