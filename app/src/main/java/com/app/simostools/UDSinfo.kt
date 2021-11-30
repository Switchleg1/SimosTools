package com.app.simostools

object UDSInfo {
    private var TAG                     = "UDSInfo"
    private var mLastString: String     = ""
    private var mTimeoutCounter: Int    = TIME_OUT_INFO

    fun getInfo(): String {
        return mLastString
    }

    fun getStartCount(): Int {
        return ECUInfo.values().count()
    }

    fun startTask(index: Int): ByteArray {
        val bleHeader = BLEHeader()
        bleHeader.cmdSize = 1 + ECUInfo.values()[index].address.count()
        bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value

        return bleHeader.toByteArray() + byteArrayOf(0x22.toByte()) + ECUInfo.values()[index].address
    }

    fun processPacket(ticks: Int, buff: ByteArray?): UDSReturn {
        buff?.let {
            resetTimeout()

            if (buff.count() >= 11 && buff[8] == 0x62.toByte()) {
                mLastString = "${ECUInfo.values()[ticks].str}: ${ECUInfo.values()[ticks].parseResponse(buff.copyOfRange(11, buff.count()))}"

                return UDSReturn.OK
            }
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
        mTimeoutCounter = TIME_OUT_INFO
    }
}