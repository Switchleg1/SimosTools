package com.app.simoslogger

enum class ECUInfo(val str: String, val address: ByteArray) {
    VIN("VIN", byteArrayOf(0xf1.toByte(), 0x90.toByte())),
    ODX_IDENTIFIER("ASAM/ODX File Identifier", byteArrayOf(0xF1.toByte(), 0x9E.toByte())),
    ODX_VERSION("ASAM/ODX File Version", byteArrayOf(0xF1.toByte(), 0xA2.toByte())),
    VEHICLE_SPEED("Vehicle Speed", byteArrayOf(0xF4.toByte(), 0x0D.toByte())),
    CAL_NUMBER("Calibration Version Numbers", byteArrayOf(0xF8.toByte(), 0x06.toByte())),
    PART_NUMBER("VW Spare part Number", byteArrayOf(0xF1.toByte(), 0x87.toByte())),
    ASW_VERSION("VW ASW Version", byteArrayOf(0xF1.toByte(), 0x89.toByte())),
    HW_NUMBER("ECU Hardware Number", byteArrayOf(0xF1.toByte(), 0x91.toByte())),
    HW_VERSION("ECU Hardware Version Number", byteArrayOf(0xF1.toByte(), 0xA3.toByte())),
    ENGINE_CODE("Engine Code", byteArrayOf(0xF1.toByte(), 0xAD.toByte())),
    WORKSHOP_NAME("VW Workshop Name", byteArrayOf(0xF1.toByte(), 0xAA.toByte())),
    FLASH_STATE("State of Flash Mem", byteArrayOf(0x04.toByte(), 0x05.toByte())),
    CODE_VALUE("VW Coding Value", byteArrayOf(0x06.toByte(), 0x00.toByte()))
}

object UDSInfo {
    private var TAG = "UDSInfo"
    private var mLastString: String = ""

    fun getInfo(): String {
        return mLastString
    }

    fun getCount(): Int {
        return ECUInfo.values().count()
    }

    fun buildECUInfo(index: Int): ByteArray {
        val bleHeader = BLEHeader()
        bleHeader.cmdSize = 1 + ECUInfo.values()[index].address.count()
        bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value

        return bleHeader.toByteArray() + byteArrayOf(0x22.toByte()) + ECUInfo.values()[index].address
    }

    fun processECUInfo(ticks: Int, buff: ByteArray?): UDSReturn {
        buff?.let {
            if (buff.count() >= 11 && buff[8] == 0x62.toByte()) {
                mLastString = "${ECUInfo.values()[ticks].str}: ${String(buff.copyOfRange(11, buff.count()))}"

                return UDSReturn.OK
            }
            return UDSReturn.ERROR_UNKNOWN
        }

        return UDSReturn.ERROR_NULL
    }
}