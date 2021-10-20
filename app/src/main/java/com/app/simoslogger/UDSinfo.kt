package com.app.simoslogger

data class InfoStruct(var name: String,
                        var data: ByteArray)

val ECUInfo: Array<InfoStruct> = arrayOf(
    InfoStruct("VIN", byteArrayOf(0xf1.toByte(), 0x90.toByte())),
    InfoStruct("ASAM/ODX File Identifier", byteArrayOf(0xF1.toByte(), 0x9E.toByte())),
    InfoStruct("ASAM/ODX File Version", byteArrayOf(0xF1.toByte(), 0xA2.toByte())),
    InfoStruct("Vehicle Speed", byteArrayOf(0xF4.toByte(), 0x0D.toByte())),
    InfoStruct("Calibration Version Numbers", byteArrayOf(0xF8.toByte(), 0x06.toByte())),
    InfoStruct("VW Spare part Number", byteArrayOf(0xF1.toByte(), 0x87.toByte())),
    InfoStruct("VW ASW Version", byteArrayOf(0xF1.toByte(), 0x89.toByte())),
    InfoStruct("ECU Hardware Number", byteArrayOf(0xF1.toByte(), 0x91.toByte())),
    InfoStruct("ECU Hardware Version Number", byteArrayOf(0xF1.toByte(), 0xA3.toByte())),
    InfoStruct("Engine Code", byteArrayOf(0xF1.toByte(), 0xAD.toByte())),
    InfoStruct("VW Workshop Name", byteArrayOf(0xF1.toByte(), 0xAA.toByte())),
    InfoStruct("State of Flash Mem", byteArrayOf(0x04.toByte(), 0x05.toByte())),
    InfoStruct("VW Coding Value", byteArrayOf(0x06.toByte(), 0x00.toByte())),
    )

object UDSInfo {
    private var TAG = "UDSInfo"
    private var mLastString: String = ""

    fun getInfo(): String {
        return mLastString
    }

    fun getCount(): Int {
        return ECUInfo.count()
    }

    fun buildECUInfo(index: Int): ByteArray {
        val bleHeader = BLEHeader()
        bleHeader.cmdSize = 1 + ECUInfo[index].data.count()
        bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_CLEAR

        return bleHeader.toByteArray() + byteArrayOf(0x22.toByte()) + ECUInfo[index].data
    }

    fun processECUInfo(ticks: Int, buff: ByteArray?): Int {
        buff?.let {
            if (buff.count() >= 11 && buff[8] == 0x62.toByte()) {
                mLastString = "${ECUInfo[ticks].name}: ${String(buff.copyOfRange(11, buff.count()))}"

                return UDS_OK
            }
            return UDS_ERROR_UNKNOWN
        }

        return UDS_ERROR_NULL
    }
}