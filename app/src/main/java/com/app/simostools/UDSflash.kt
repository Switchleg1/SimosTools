package com.app.simostools

import java.io.InputStream

object UDSFlasher {
    private val TAG                         = "UDSflash"
    private var mTask                       = FLASH_ECU_CAL_SUBTASK.NONE
    private var mFileStream: InputStream?   = null
    private var mLastString: String         = ""
    private var mTimeoutCounter: Int        = TIME_OUT_FLASH

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
            resetTimeout()

            return UDSReturn.ERROR_UNKNOWN
        }

        return addTimeout()
    }

    private fun addTimeout(): UDSReturn {
        if(--mTimeoutCounter == 0) {
            return UDSReturn.ERROR_TIME_OUT
        }

        return UDSReturn.OK
    }

    private fun resetTimeout() {
        mTimeoutCounter = TIME_OUT_FLASH
    }
}