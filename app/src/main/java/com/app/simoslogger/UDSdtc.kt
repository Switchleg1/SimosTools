package com.app.simoslogger

object UDSdtc {
    private val TAG = "UDSdtc"
    private var mLastString: String = ""

    fun getInfo(): String {
        return mLastString
    }

    fun getCount(): Int {
        return 1
    }

    fun buildClearDTC(ticks: Int): ByteArray {
        if(ticks < getCount()) {
            //Send clear request
            val bleHeader = BLEHeader()
            bleHeader.rxID = 0x7E8
            bleHeader.txID = 0x700
            bleHeader.cmdSize = 1
            bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_CLEAR

            return bleHeader.toByteArray() + byteArrayOf(0x04.toByte())
        }

        return byteArrayOf()
    }

    fun processClearDTC(ticks: Int, buff: ByteArray?): Int {
        buff?.let {
            if(ticks < getCount()) {
                if (buff.count() == 9 && buff[8] == 0x44.toByte()) {
                    mLastString = "DTC: cleared."
                } else {
                    mLastString = "DTC: failed."
                }

                return UDS_OK
            }

            return UDS_ERROR_UNKNOWN
        }

        return UDS_ERROR_NULL
    }
}