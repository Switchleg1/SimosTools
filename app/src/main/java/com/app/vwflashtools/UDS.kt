package com.app.vwflashtools

//Equation list
//  0: none
//  1: ( 0.375 * X + -48.0 ) / 1
//  2: ( 1.0 * X + -2731.4 ) / 10.0
//  3: ( 1.0 * X + 0.0 ) / 1.28f - 100.0f
//  4: ( 6.103515624994278 * X + 0.0 ) / 1
//  5: ( 0.0078125 * X + -0.0 ) / 1
//  6: ( 1.0 * X + 0.0 ) / 4.0
//  7: ( 0.75 * X + -48.0 ) / 1
//  8: ( 1.0 * X + 0.0 ) / 10.0
//  9: ( 1.0 * X + 0.0 ) / 100.0
// 10: ( 1.0 * X + 0.0 ) / 1000.0

data class DIDStruct(val address: Int, val length: Int, val equation: Int, val name: String, val unit: String, var value: Float)

val DIDList: List<DIDStruct> = listOf(
    DIDStruct(0x1001, 1, 7, "IAT", "°C", 0f),
    DIDStruct(0x1040, 2, 7, "Turbo Speed", "rpm", 0f),
    DIDStruct(0x13f2, 1, 1, "Retard cylinder 1", "°", 0f),
    DIDStruct(0x13f3, 1, 1, "Retard cylinder 2", "°", 0f),
    DIDStruct(0x13f4, 1, 1, "Retard cylinder 3", "°", 0f),
    DIDStruct(0x13f5, 1, 1, "Retard cylinder 4", "°", 0f),
    DIDStruct(0x15d3, 2, 5, "Speed", "km/hr", 0f),
    DIDStruct(0x2025, 2, 10, "LFP Actual", "bar", 0f),
    DIDStruct(0x2027, 2, 8, "HFP Actual", "bar", 0f),
    DIDStruct(0x203c, 2, 0, "Cruise control status", "", 0f),
    DIDStruct(0x203f, 2, 8, "Torque limit", "Nm", 0f),
    DIDStruct(0x206d, 2, 9, "Throttle", "%", 0f),
    DIDStruct(0x293b, 2, 10, "HFP Command", "bar", 0f),
    DIDStruct(0x295c, 1, 0, "Flaps Actual", "", 0f),
    DIDStruct(0x295d, 1, 0, "Flaps Command", "", 0f),
    DIDStruct(0x2932, 2, 8, "LFP Command", "bar", 0f),
    DIDStruct(0x39a9, 2, 9, "Ignition angle", "°", 0f),
    DIDStruct(0x39c0, 2, 10, "MAP","bar", 0f),
    DIDStruct(0x39c2, 2, 10, "PUT","bar", 0f),
    DIDStruct(0x3d97, 2, 0, "O2","l", 0f),
    DIDStruct(0x3e0a, 2, 9, "Coolant temp","°C", 0f),
    DIDStruct(0xf406, 1, 3, "STFT","%", 0f),
    DIDStruct(0xf456, 1, 3, "LTFT","%", 0f),
    DIDStruct(0xf40C, 2, 6, "RPM","rpm", 0f),
    DIDStruct(0x202f, 2, 2, "Oil temp", "°C", 0f),
)

object DIDs {
    fun getDID(address: Int): DIDStruct? {
        for (i in 0 until DIDList.count()) {
            if(DIDList[i].address == address) {
                return DIDList[i]
            }
        }
        return null
    }

    fun getValue(did: DIDStruct, data: Float): Float {
        when(did.equation) {
            0 -> {
                return data
            }
            1 -> {
                return 0.375f * data - 48.0f
            }
            2 -> {
                return 0.375f * data - 48.0f
            }
            3 -> {
                return data / 1.28f - 100.0f
            }
            4 -> {
                return 6.1035f * data
            }
            5 -> {
                return 0.0078125f * data
            }
            6 -> {
                return data / 4.0f
            }
            7 -> {
                return 0.75f * data - 48.0f
            }
            8 -> {
                return data / 10.0f
            }
            9 -> {
                return data / 100.0f
            }
            10 -> {
                return data / 1000.0f
            }
        }

        return data
    }
}

object UDS22Logger {

    var didList: ByteArray? = null
    var didEnable: DIDStruct? = null

    fun processFrame(buff: ByteArray?): Int {
        // if the buffer is null abort
        if(buff == null) {
            return UDS_ERROR_NULL
        }

        // check to make sure ble header byte matches
        val bleHeader = BLEHeader()
        bleHeader.fromByteArray(buff)
        val bData = buff.copyOfRange(8, buff.size)
        if(!bleHeader.isValid()) {
            return UDS_ERROR_HEADER
        }

        // does the size of the data match the header?
        if(bData.count() != bleHeader.cmdSize) {
            return UDS_ERROR_CMDSIZE
        }

        // make sure we received an 'OK' from the ECU
        if(bData[0] != 0x62.toByte()) {
            return UDS_ERROR_RESPONSE
        }

        // process the data in the buffer
        var i = 1
        while(i < bleHeader.cmdSize-3) {
            val did: DIDStruct = DIDs.getDID(((bData[i] and 0xFF) shl 8) + (bData[i+1] and 0xFF)) ?: return UDS_ERROR_UNKNOWN
            if(did.length == 1) {
                did.value = (bData[i+2] and 0xFF).toFloat()
                i += 3
            } else {
                did.value = ((bData[i+2] and 0xFF) shl 8 + (bData[i+3] and 0xFF)).toFloat()
                i += 4
            }
        }

        return UDS_OK
    }
}