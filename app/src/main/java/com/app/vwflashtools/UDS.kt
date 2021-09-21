package com.app.vwflashtools

import android.content.Context
import android.graphics.Color
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
// 11: ( 0.03125 * X + 0.0 ) / 1
// 12: ( 0.08291752498664835 * X + 0.0 ) / 1000.0
// 13: X * 100.0 / 255.0
// 14: X / 2.0 – 64.0
// 15: ( 0.0009765625 * X + 0.0 ) / 1
// 16: X / 2.4
// 17: X * 0.005
// 18: X * 0.002874
// 19: 100 - ( 1.0 * X + 0.0 ) / 100.0

data class DIDStruct(var address: Int,
                     var length: Int,
                     var equation: Int,
                     var signed: Boolean,
                     var min: Float,
                     var max: Float,
                     var warnMin: Float,
                     var warnMax: Float,
                     var value: Float,
                     var format: String,
                     var name: String,
                     var unit: String)

object DIDs {
    val list: List<DIDStruct> = listOf(
        //P1
        DIDStruct(0x2033, 2, 18,false,0f,  220f,   -20f, 200f,   0f, "%06.2f","Speed",                  "km/hr"),
        DIDStruct(0xf40C, 2, 6, false,0f,  7000f,  -1f,  6000f,  0f, "%06.1f","RPM",                    "rpm"),
        DIDStruct(0x39c0, 2, 10,false,0f,  3f,     0f,   2.6f,   0f, "%05.3f","MAP Actual",             "bar"),
        DIDStruct(0x39c2, 2, 10,false,0f,  3f,     0f,   2.6f,   0f, "%05.3f","PUT Actual",             "bar"),
        DIDStruct(0x10c0, 2, 15,false,0f,  2f,     0.7f, 4f,     0f, "%05.3f","Lambda Actual",          "l"),
        DIDStruct(0x2004, 2, 9, true, -10f,10f,    -15f, 60f,    0f, "%05.2f","Ignition angle",         "°"),
        DIDStruct(0x39a2, 2, 19,false,0f,  100f,   -1f,  100f,   0f, "%02.2f","Wastegate Position",     "%"),
        DIDStruct(0x2032, 2, 16,false,0f,  1500f,  -1f,  1500f,  0f, "%07.2f","Mass Airflow",           "g/s"),
        //P2
        DIDStruct(0x2950, 2, 15,false,0f,  2f,     0.7f, 4f,     0f, "%05.3f","Lambda Specified",       "l"),
        DIDStruct(0x13a0, 2, 17,false,0f,  190000f,0f,   185000f,0f, "%05.2f","Injector PW Cyl 1 DI",   "ms"),
        DIDStruct(0x437C, 2, 8, true, -50f,450f,   -100f,500f,   0f, "%03.2f","Engine torque",          "Nm"),
        DIDStruct(0x2027, 2, 8, false,0f,  250f,   10f,  250f,   0f, "%03.2f","HFP Actual",             "bar"),
        DIDStruct(0xf406, 1, 3, false,-25f,25f,    -20f, 20f,    0f, "%02.2f","STFT",                   "%"),
        DIDStruct(0x20ba, 2, 8, true, 0f,  100f,   -1f,  101f,   0f, "%05.1f","Throttle Sensor",        "%"),
        DIDStruct(0x1040, 2, 4, false,0f,  190000f,0f,   185000f,0f, "%08.1f","Turbo Speed",            "rpm"),
        DIDStruct(0x209a, 2, 9, false,0f,  100f,   -1f,  100f,   0f, "%03.2f","HPFP Effective Volume",  "%"),
        //P3
        DIDStruct(0x200a, 2, 9, true, -10f,0f,     -4f,  1f,     0f, "%05.3f","Retard cylinder 1",      "°"),
        DIDStruct(0x200b, 2, 9, true, -10f,0f,     -4f,  1f,     0f, "%05.3f","Retard cylinder 2",      "°"),
        DIDStruct(0x200c, 2, 9, true, -10f,0f,     -4f,  1f,     0f, "%05.3f","Retard cylinder 3",      "°"),
        DIDStruct(0x200d, 2, 9, true, -10f,0f,     -4f,  1f,     0f, "%05.3f","Retard cylinder 4",      "°"),
        DIDStruct(0x2904, 2, 0, false,0f,  20f,    -1f,  10f,    0f, "%03.0f","Misfire Sum Global",     ""),
        DIDStruct(0x1001, 1, 7, false,-40f,55f,    -35f, 50f,    0f, "%03.2f","IAT",                    "°C"),
        DIDStruct(0x2025, 2, 10,false,0f,  15f,    6f,   15f,    0f, "%03.2f","LFP Actual",             "bar"),
        DIDStruct(0x293b, 2, 8, false,0f,  250f,   10f,  250f,   0f, "%03.2f","HFP Command",            "bar"),

        DIDStruct(0x2028, 2, 9, false,0f,  2f,     0.7f, 4f,     0f, "%05.3f","LPFP Duty",              "%"),
        DIDStruct(0x295c, 1, 0, false,0f,  1f,     -1f,  2f,     0f, "%01.0f","Flaps Actual",           ""),
        DIDStruct(0xf456, 1, 3, false,-25f,25f,    -20f, 20f,    0f, "%02.2f","LTFT",                   "%"),
        DIDStruct(0x202f, 2, 2, false,-50f,130f,   0f,   112f,   0f, "%03.2f","Oil temp",               "°C"),
        DIDStruct(0x13ca, 2, 12,false,0.5f,1.3f,   0.7f, 1.2f,   0f, "%07.2f","Ambient pressure",       "bar"),
        DIDStruct(0x1004, 2, 5, true, -40f,50f,    -30f, 45f,    0f, "%07.2f","Ambient air temperature","°C"),
        DIDStruct(0x4380, 2, 8, true, 0f,  500f,   -10f, 500f,   0f, "%03.2f","Engine torque requested","Nm"),
        DIDStruct(0x203c, 2, 0, false,0f,  2f,     -1f,  3f,     0f, "%01.0f","Cruise control status",  ""),
        /*
        DIDStruct(0x13ac, 2, 17, false, 0f, 190000f, 0f, 185000f,0f, "%05.2f","Injector PW Cyl 1 MPI", "ms"),
        DIDStruct(0x2966, 2, 0, false, 0f, 20f, -1f, 10f,0f, "%03.0f","Misfire Sum 1", ""),
        DIDStruct(0x2967, 2, 0, false, 0f, 20f, -1f, 10f,0f, "%03.0f","Misfire Sum 2", ""),
        DIDStruct(0x2968, 2, 0, false, 0f, 20f, -1f, 10f,0f, "%03.0f","Misfire Sum 3", ""),
        DIDStruct(0x2969, 2, 0, false, 0f, 20f, -1f, 10f,0f, "%03.0f","Misfire Sum 4", ""),
        DIDStruct(0x40e1, 2, 10, false, -50f, 130f, -50f, 130f,0f, "%03.2f","Coolant temp","°C"),
        DIDStruct(0x2932, 2, 10, false, 0f, 5f, 6f, 15f,0f, "%03.2f","LFP Command", "bar"),
        DIDStruct(0x295d, 1, 0, false, 0f, 1f, -1f, 2f,0f, "%01.0f","Flaps Command", ""),
        */
    )

    fun getDID(address: Int): DIDStruct? {
        for (i in 0 until list.count()) {
            if(list[i].address == address) {
                return list[i]
            }
        }
        return null
    }

    fun setValue(did: DIDStruct?, data: Int): Float {
        if(did == null)
            return 0f

        Log.i("DID", data.toString())

        when(did.equation) {
            0 -> {
                did.value = data.toFloat()
            }
            1 -> {
                did.value = 0.375f * data.toFloat() - 48.0f
            }
            2 -> {
                did.value = (data.toFloat() - 2731.4f) / 10.0f
            }
            3 -> {
                did.value = data.toFloat() / 1.28f - 100.0f
            }
            4 -> {
                did.value = 6.103515624994278f * data.toFloat()
            }
            5 -> {
                did.value = 0.0078125f * data.toFloat()
            }
            6 -> {
                did.value = data.toFloat() / 4.0f
            }
            7 -> {
                did.value = 0.75f * data.toFloat() - 48.0f
            }
            8 -> {
                did.value = data.toFloat() / 10.0f
            }
            9 -> {
                did.value = data.toFloat() / 100.0f
            }
            10 -> {
                did.value = data.toFloat() / 1000.0f
            }
            11 -> {
                did.value = 0.03125f * data.toFloat()
            }
            12 -> {
                did.value = 0.08291752498664835f * data.toFloat() / 1000.0f
            }
            13 -> {
                did.value = data.toFloat() * 100.0f / 255.0f
            }
            14 -> {
                did.value = (data.toFloat() / 2.0f) - 64.0f
            }
            15 -> {
                did.value = data.toFloat() * 0.0009765625f
            }
            16 -> {
                did.value = data.toFloat() / 2.4f
            }
            17 -> {
                did.value = data.toFloat() * 0.005f
            }
            18 -> {
                did.value = data.toFloat() * 0.002874f
            }
            19 -> {
                did.value = 100.0f - data.toFloat() / 100.0f
            }
        }

        return did.value
    }

    fun getValue(did: DIDStruct?): Float {
        if (did == null)
            return 0f

        return did.value
    }
}

object UDS22Logger {
    private var mLastEnabled = false

    fun frameCount(): Int {
        return 8
    }

    fun buildFrame(index: Int): ByteArray {
        val bleHeader = BLEHeader()
        bleHeader.cmdSize = 1
        bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_ADD
        if(index == 0) {
            bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_ADD or BLE_COMMAND_FLAG_PER_CLEAR
        } else if(index == frameCount()-1) {
            bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_ADD or BLE_COMMAND_FLAG_PER_ENABLE
        }

        var buff: ByteArray = byteArrayOf(0x22.toByte())
        if(index % 2 == 0) {
            //Write P1 PIDS
            for (i in 0 until 8) {
                val did: DIDStruct = DIDs.list[i]
                bleHeader.cmdSize += 2
                buff += ((did.address and 0xFF00) shr 8).toByte()
                buff += (did.address and 0xFF).toByte()
            }
        } else {
            //Write P2 PIDS
            var startIndex = 8 + ((index % 4) / 2 * 4)
            var endIndex = startIndex + 4
            for (i in startIndex until endIndex) {
                val did: DIDStruct = DIDs.list[i]
                bleHeader.cmdSize += 2
                buff += ((did.address and 0xFF00) shr 8).toByte()
                buff += (did.address and 0xFF).toByte()
            }
            //Write P3 PIDS
            startIndex = 16 + ((index % 8) / 2 * 4)
            endIndex = startIndex + 4
            for (i in startIndex until endIndex) {
                val did: DIDStruct = DIDs.list[i]
                bleHeader.cmdSize += 2
                buff += ((did.address and 0xFF00) shr 8).toByte()
                buff += (did.address and 0xFF).toByte()
            }
        }

        return bleHeader.toByteArray() + buff
    }

    fun processFrame(tick: Int, buff: ByteArray?, context: Context): Int {
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
            val did: DIDStruct = DIDs.getDID(((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF)) ?: return UDS_ERROR_UNKNOWN
            if(did.length == 1) {
                if(did.signed) {
                    DIDs.setValue(did, (bData[i++] and 0xFF).toByte().toInt())
                } else {
                    DIDs.setValue(did, (bData[i++] and 0xFF))
                }
            } else {
                if(did.signed) {
                    DIDs.setValue(did, (((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF)).toShort().toInt())
                } else {
                    DIDs.setValue(did, ((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF))
                }
            }
        }

        //Update Log every 2nd tick
        if(tick % 2 == 0) {
            val dEnable = DIDs.list[DIDs.list.count()-1]
            if (dEnable.value != 0.0f) {
                //If we were not enabled before we must open a log to start writing
                if (!mLastEnabled) {
                    val currentDateTime = LocalDateTime.now()
                    LogFile.create("vwflashtools-${currentDateTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss"))}.csv", context)
                    var strItems: String? = "Time"
                    for (x in 0 until DIDs.list.count()) {
                        strItems += ",${DIDs.list[x].name}"
                    }
                    LogFile.add(strItems)
                }
                mLastEnabled = true

                //Write new values to log
                var strItems: String? = (bleHeader.tickCount.toFloat() / 1000.0f).toString()
                for (x in 0 until DIDs.list.count()) {
                    strItems += ",${DIDs.list[x].value}"
                }
                LogFile.add(strItems)
            } else {
                if (mLastEnabled) {
                    LogFile.close()
                }
                mLastEnabled = false
            }
        }

        return UDS_OK
    }
}