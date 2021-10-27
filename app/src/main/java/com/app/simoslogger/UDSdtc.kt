package com.app.simoslogger

object UDSdtc {
    private val TAG = "UDSdtc"
    private var mLastString: String = ""

    fun getInfo(): String {
        return mLastString
    }

    fun getStartCount(): Int {
        return 1
    }

    fun startTask(ticks: Int): ByteArray {
        if(ticks < getStartCount()) {
            //Send clear request
            val bleHeader = BLEHeader()
            bleHeader.rxID = 0x7E8
            bleHeader.txID = 0x700
            bleHeader.cmdSize = 1
            bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value

            return bleHeader.toByteArray() + byteArrayOf(0x04.toByte())
        }

        return byteArrayOf()
    }

    fun processPacket(ticks: Int, buff: ByteArray?): UDSReturn {
        buff?.let {
            if(ticks < getStartCount()) {
                if (buff.count() == 9 && buff[8] == 0x44.toByte()) {
                    mLastString = "DTC: cleared."
                } else {
                    mLastString = "DTC: failed."
                }

                return UDSReturn.OK
            }

            return UDSReturn.ERROR_UNKNOWN
        }

        return UDSReturn.ERROR_NULL
    }
}