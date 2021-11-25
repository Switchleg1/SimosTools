package com.app.simostools

enum class DTCCommands(val str: String, val command: ByteArray, val response: ByteArray) {
    EXT_DIAG("Extended Diagnostic", byteArrayOf(0x10.toByte(), 0x03.toByte()), byteArrayOf(0x50.toByte(), 0x03.toByte())),
    DTC_REQ("DTC Request", byteArrayOf(0x19.toByte(), 0x02.toByte(), 0xAB.toByte()), byteArrayOf(0x7F.toByte(), 0x19.toByte()))
}

object UDSdtc {
    private val TAG                     = "UDSdtc"
    private var mLastString: String     = ""
    private var mTimeoutCounter: Int    = TIME_OUT_DTC

    fun getInfo(): String {
        return mLastString
    }

    fun getStartCount(clear: Boolean): Int {
        return if(clear) 1
        else DTCCommands.values().count()
    }

    fun startTask(ticks: Int, clear: Boolean): ByteArray {
        return if (clear) startClearDTC(ticks)
        else return startGetDTC(ticks)
    }

    private fun startClearDTC(ticks: Int): ByteArray {
        if(ticks < getStartCount(true)) {
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

    private fun startGetDTC(ticks: Int): ByteArray {
        if(ticks < getStartCount(false)) {
            //Send clear request
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = DTCCommands.values()[ticks].command.count()
            bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value

            return bleHeader.toByteArray() + DTCCommands.values()[ticks].command
        }

        return byteArrayOf()
    }

    fun processPacket(ticks: Int, buff: ByteArray?, clear: Boolean): UDSReturn {
        buff?.let {
            resetTimeout()

            return if(clear) processClearPacket(ticks, buff)
            else processGetPacket(ticks, buff)
        }

        return addTimeout()
    }

    private fun processClearPacket(ticks: Int, buff: ByteArray): UDSReturn {
        if(ticks < getStartCount(true)) {
            mLastString = if (buff.count() == 9 && buff[8] == 0x44.toByte()) {
                "Clear DTC: ok."
            } else {
                "Clear DTC: failed."
            }

            return UDSReturn.OK
        }
        return UDSReturn.ERROR_UNKNOWN
    }

    private fun processGetPacket(ticks: Int, buff: ByteArray): UDSReturn {
        if(ticks < getStartCount(false) && buff.count() > 8) {
            val data = buff.copyOfRange(8, buff.count())
            var resOk = true
            DTCCommands.values()[ticks].response.forEachIndexed() { i, d ->
                if(d != data[i])
                    resOk = false
            }

            return if (resOk) {
                mLastString = "Get DTC: ok."
                UDSReturn.OK
            } else {
                mLastString = "Get DTC: failed."
                UDSReturn.ERROR_RESPONSE
            }
        }

        if(buff.count() > 8) {
            mLastString = "Get DTC\n-------"
            var data = buff.copyOfRange(8, buff.count())
            if(data.count() >= 3 && data[0] == 0x59.toByte() && data[1] == 0x02.toByte() && data[2] == 0xFF.toByte()) {
                return if (data.count() > 3) {
                    data = data.copyOfRange(3, data.count())
                    while(data.count() >= 4) {
                        val resInt = (data[1] shl 8) + data[2]
                        DTCs.list.forEachIndexed() { i, d->
                            if(d?.code == resInt) {
                                mLastString += "\n${d.pcode} ${d.name}"
                            }
                        }

                        data = data.copyOfRange(4, data.count())
                    }

                    UDSReturn.COMPLETE
                } else {
                    mLastString += "\nNone found."
                    UDSReturn.COMPLETE
                }
            }
            return UDSReturn.ERROR_RESPONSE
        }

        return UDSReturn.ERROR_HEADER
    }

    private fun addTimeout(): UDSReturn {
        if(--mTimeoutCounter == 0) {
            return UDSReturn.ERROR_TIME_OUT
        }

        return UDSReturn.OK
    }

    private fun resetTimeout() {
        mTimeoutCounter = TIME_OUT_DTC
    }
}