package com.app.simoslogger

import android.content.Context
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

object UDSLogger {
    private val TAG = "UDSLogger"
    private var mLastEnabled = false
    private var mMode = UDS_LOGGING_22A
    private var mTorquePID = -1
    private var mEngineRPMPID = -1
    private var mMS2PID = -1
    private var mGearPID = -1
    private var mVelocityPID = -1
    private var mTireCircumference = -1f
    private var mFoundMS2PIDS = false
    private var mFoundTQPIDS = false

    fun isCalcHP(): Boolean {
        if(Settings.calculateHP && (mFoundTQPIDS || mFoundMS2PIDS)) {
            return true
        }

        return false
    }

    fun getTorque(): Float {
        DIDs.list()?.let { list ->
            if (Settings.calculateHP) {
                if (mFoundMS2PIDS && Settings.useMS2Torque) {
                    try {
                        val gearValue = list[mGearPID]!!.value.toInt()

                        if (gearValue in 1..7) {
                            val ms2Value = sqrt(list[mMS2PID]!!.value)
                            val velValue = list[mVelocityPID]!!.value
                            val weightValue = Settings.curbWeight * KG_TO_N
                            val ratioValue = sqrt(Settings.gearRatios[gearValue - 1] * Settings.gearRatios[7])
                            val dragValue = 1.0 + ((velValue * velValue).toDouble() * Settings.dragCoefficient)

                            return ((weightValue * ms2Value / ratioValue / mTireCircumference / TQ_CONSTANT).toDouble() * dragValue).toFloat()
                        }
                    } catch (e: Exception) {
                        return 0f
                    }

                    return 0f
                } else if (mFoundTQPIDS) {
                    return try {
                        list[mTorquePID]!!.value
                    } catch (e: Exception) {
                        0f
                    }
                }
            }
        }

        return 0f
    }

    fun getHP(tq: Float): Float {
        DIDs.list()?.let { list ->
            if (Settings.calculateHP && mEngineRPMPID != -1) {
                return try {
                    val rpmValue = list[mEngineRPMPID]!!.value
                    tq * rpmValue / 7127f
                } catch (e: Exception) {
                    0f
                }
            }
        }

        return 0f
    }

    fun isEnabled(): Boolean {
        return mLastEnabled
    }

    fun setMode(mode: Int) {
        when(mode) {
            in UDS_LOGGING_22A .. UDS_LOGGING_22C -> mMode = mode
            UDS_LOGGING_3E  -> mMode = mode
        }
    }

    fun getMode(): Int {
        return mMode
    }

    fun frameCount(): Int {
        return when (mMode) {
            in UDS_LOGGING_22A .. UDS_LOGGING_22C -> frameCount22()
            UDS_LOGGING_3E -> frameCount3E()
            else -> 0
        }
    }

    fun buildFrame(index: Int): ByteArray {
        return when(mMode) {
            in UDS_LOGGING_22A .. UDS_LOGGING_22C -> return buildFrame22(index)
            UDS_LOGGING_3E -> return buildFrame3E(index)
            else -> byteArrayOf()
        }
    }

    fun processFrame(tick: Int, buff: ByteArray?, context: Context): Int {
        return when(mMode) {
            in UDS_LOGGING_22A .. UDS_LOGGING_22C -> processFrame22(tick, buff, context)
            UDS_LOGGING_3E  -> processFrame3E(tick, buff, context)
            else -> UDS_ERROR_NULL
        }
    }

    private fun resetHPPIDS() {
        mFoundTQPIDS = false
        mFoundMS2PIDS = false
        mTorquePID = -1
        mEngineRPMPID = -1
        mMS2PID = -1
        mGearPID = -1
        mVelocityPID = -1
        mTireCircumference = Settings.tireDiameter * 3.14f
    }

    private fun findHPPIDS() {
        if(mMode == UDS_LOGGING_3E) {
            DIDs.list3E?.let { list ->
                for (x in 0 until list.count()) {
                    //Look for torque PID
                    if (list[x]?.address == 0xd0015344) {
                        mTorquePID = x
                    }
                    //Look for rpm PID
                    if (list[x]?.address == 0xd0012400) {
                        mEngineRPMPID = x
                    }

                    //Look for MS2 PID
                    if (list[x]?.address == 0xd00141ba) {
                        mMS2PID = x
                    }

                    //Look for Gear PID
                    if (list[x]?.address == 0xd000f39a) {
                        mGearPID = x
                    }

                    //Look for Velocity PID
                    if (list[x]?.address == 0xd00155b6) {
                        mVelocityPID = x
                    }
                }
            }
            //Did we find the PIDs required?
            if(mEngineRPMPID != -1 && mMS2PID != -1 && mGearPID != -1 && mVelocityPID != -1)
                mFoundMS2PIDS = true

            if(mEngineRPMPID != -1 && mTorquePID != -1)
                mFoundTQPIDS = true
        } else {
            DIDs.list()?.let { list ->
                for (x in 0 until list.count()) {
                    //Look for torque PID
                    if (list[x]?.address == 0x437C.toLong()) {
                        mTorquePID = x
                    }

                    //Look for rpm PID
                    if (list[x]?.address == 0xf40C.toLong()) {
                        mEngineRPMPID = x
                    }
                }
                //Did we find the PIDs required?
                if (mEngineRPMPID != -1 && mTorquePID != -1)
                    mFoundTQPIDS = true
            }
        }
    }

    private fun frameCount22(): Int {
        DIDs.list()?.let { list ->
            if(list.count() > 0)
                return (list.count()-1) / 8 + 1
        }
        return 0
    }

    private fun frameCount3E(): Int {
        return try {
            (DIDs.list3E!!.count() * 5 / 0x8F) + 2
        } catch (e: Exception) {
            0
        }
    }

    private fun buildFrame22(index: Int): ByteArray {
        DIDs.list()?.let { list ->
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 1
            bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_ADD
            if (index == 0) {
                bleHeader.cmdFlags += BLE_COMMAND_FLAG_PER_CLEAR
            }
            if (index == frameCount22() - 1) {
                bleHeader.cmdFlags += BLE_COMMAND_FLAG_PER_ENABLE
            }

            var buff: ByteArray = byteArrayOf(0x22.toByte())
            val startIndex = if(index * 8 > list.count()) list.count()
                else index * 8
            val endIndex = if(startIndex + 8 > list.count()) list.count()
                else startIndex + 8
            for (i in startIndex until endIndex) {
                val did: DIDStruct? = list[i]
                did?.let {
                    bleHeader.cmdSize += 2
                    buff += ((did.address and 0xFF00) shr 8).toByte()
                    buff += (did.address and 0xFF).toByte()
                }
            }
            return bleHeader.toByteArray() + buff
        }

        return byteArrayOf()
    }

    private fun buildFrame3E(index: Int): ByteArray {
        DIDs.list3E?.let { list ->
            var addressArray: ByteArray = byteArrayOf()
            for (i in 0 until list.count()) {
                val did: DIDStruct? = list[i]
                did?.let {
                    addressArray += (did.length and 0xFF).toByte()
                    addressArray += did.address.toArray4()
                }
            }
            addressArray += 0

            //Do we even have any PIDs in the range?  If not send persist message
            if ((index * 0x8F >= addressArray.count()) and (index == frameCount3E() - 1)) {
                val bleHeader = BLEHeader()
                bleHeader.cmdSize = 6
                bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_CLEAR or BLE_COMMAND_FLAG_PER_ADD or BLE_COMMAND_FLAG_PER_ENABLE

                val writeBuffer: ByteArray = bleHeader.toByteArray() + byteArrayOf(
                    0x3e.toByte(),
                    0x33.toByte(),
                    0xb0.toByte(),
                    0x01.toByte(),
                    0xe7.toByte(),
                    0x00.toByte()
                )

                DebugLog.d(TAG, "Building 3E frame $index with length ${writeBuffer.count()}: ${writeBuffer.toHex()}")

                return writeBuffer
            }

            //constrain copy range or we will receive an exception
            val endOfArray = if ((1 + index) * 0x8F > addressArray.count()) {
                addressArray.count()
            } else {
                (1 + index) * 0x8F
            }
            val selectArray: ByteArray = addressArray.copyOfRange(index * 0x8F, endOfArray)
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 8 + selectArray.count()
            bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_CLEAR

            val memoryOffset = 0xB001E700 + (index * 0x8F)
            val writeBuffer: ByteArray = bleHeader.toByteArray() + byteArrayOf(
                0x3e.toByte(),
                0x32.toByte()
            ) + memoryOffset.toArray4() + selectArray.count().toArray2() + selectArray

            DebugLog.d(TAG, "Building 3E frame $index with length ${writeBuffer.count()}: ${writeBuffer.toHex()}")

            return writeBuffer
        }

        return byteArrayOf()
    }

    fun processFrame22(tick: Int, buff: ByteArray?, context: Context): Int {
        DIDs.list()?.let { list ->
            // if the buffer is null abort
            if (buff == null) {
                return UDS_ERROR_NULL
            }

            // check to make sure ble header byte matches
            val bleHeader = BLEHeader()
            bleHeader.fromByteArray(buff)
            val bData = buff.copyOfRange(8, buff.size)
            if (!bleHeader.isValid()) {
                return UDS_ERROR_HEADER
            }

            // does the size of the data match the header?
            if (bData.count() != bleHeader.cmdSize) {
                return UDS_ERROR_CMDSIZE
            }

            // make sure we received an 'OK' from the ECU
            if (bData[0] != 0x62.toByte()) {
                return UDS_ERROR_RESPONSE
            }

            //In init state
            if (tick < frameCount22()) {
                return UDS_OK
            }

            // process the data in the buffer
            var i = 1
            while (i < bleHeader.cmdSize - 3) {
                val did: DIDStruct =
                    DIDs.getDID(((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF).toLong())
                        ?: return UDS_ERROR_UNKNOWN
                if (did.length == 1) {
                    if (did.signed) {
                        DIDs.setValue(did, (bData[i++] and 0xFF).toByte().toFloat())
                    } else {
                        DIDs.setValue(did, (bData[i++] and 0xFF).toFloat())
                    }
                } else {
                    if (did.signed) {
                        DIDs.setValue(
                            did,
                            (((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF)).toShort()
                                .toFloat()
                        )
                    } else {
                        DIDs.setValue(
                            did,
                            (((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF)).toFloat()
                        )
                    }
                }
            }

            //Update Log every 2nd tick
            if (tick % frameCount22() == 0) {
                val dEnable = list[list.count() - 1]
                if ((!Settings.invertCruise && dEnable?.value != 0.0f) ||
                    (Settings.invertCruise && dEnable?.value == 0.0f)) {
                    //If we were not enabled before we must open a log to start writing
                    if (!mLastEnabled) {
                        val currentDateTime = LocalDateTime.now()
                        LogFile.create(
                            "simoslogger-${
                                currentDateTime.format(
                                    DateTimeFormatter.ofPattern(
                                        "yyyy_MM_dd-HH_mm_ss"
                                    )
                                )
                            }.csv", context
                        )
                        //Start with time, its required
                        var strItems: String? = "Time"

                        //Add all PIDS
                        for (x in 0 until list.count()) {
                            strItems += ",${list[x]?.name}"
                        }

                        //reset torque / rpm pids
                        resetHPPIDS()

                        //Are we supposed to calculate HP? find PIDS
                        if (Settings.calculateHP) {
                            //Look for PIDS related to calculating HP
                            findHPPIDS()

                            //If we found PIDs add column
                            if (isCalcHP())
                                strItems += ",HP"
                        }

                        LogFile.addLine(strItems)
                    }
                    mLastEnabled = true

                    //Write new values to log
                    var strItems: String? = (bleHeader.tickCount.toFloat() / 1000.0f).toString()
                    for (x in 0 until list.count()) {
                        strItems += ",${list[x]?.value}"
                    }

                    //Calculate HP and found PIDS?
                    if (isCalcHP()) {
                        val calcHP = getHP(getTorque())
                        strItems += ",${calcHP}"
                    }

                    LogFile.addLine(strItems)
                } else {
                    if (mLastEnabled) {
                        LogFile.close()
                    }
                    mLastEnabled = false
                }
            }

            return UDS_OK
        }

        return UDS_ERROR_UNKNOWN
    }

    private fun processFrame3E(tick: Int, buff: ByteArray?, context: Context): Int {
        DIDs.list3E?.let { list ->
            // if the buffer is null abort
            if (buff == null) {
                return UDS_ERROR_NULL
            }

            // check to make sure ble header byte matches
            val bleHeader = BLEHeader()
            bleHeader.fromByteArray(buff)
            val bData = buff.copyOfRange(8, buff.size)
            if (!bleHeader.isValid()) {
                return UDS_ERROR_HEADER
            }

            // does the size of the data match the header?
            if (bData.count() != bleHeader.cmdSize) {
                return UDS_ERROR_CMDSIZE
            }

            // make sure we received an 'OK' from the ECU
            if (bData[0] != 0x7e.toByte()) {
                return UDS_ERROR_RESPONSE
            }

            //still in the initial setup?
            if (tick < frameCount3E()) {
                return UDS_OK
            }

            //Update PID Values
            var dPos = 1
            for (i in 0 until list.count()) {
                val did: DIDStruct? = list[i]

                try {
                    //make sure we are in range
                    if (dPos + did!!.length > bData.count())
                        break

                    //Build the value in little endian
                    var newValue: Int = bData[dPos + did.length - 1] and 0xFF
                    for (d in 1 until did.length) {
                        newValue = newValue shl 8
                        newValue += bData[dPos + did.length - d - 1] and 0xFF
                    }
                    dPos += did.length

                    //set pid values
                    if (did.signed) {
                        when (did.length) {
                            1 -> DIDs.setValue(did, newValue.toByte().toFloat())
                            2 -> DIDs.setValue(did, newValue.toShort().toFloat())
                            4 -> DIDs.setValue(did, newValue.toFloat())
                        }
                    } else {
                        when (did.length) {
                            1 -> DIDs.setValue(did, newValue.toFloat())
                            2 -> DIDs.setValue(did, newValue.toFloat())
                            4 -> DIDs.setValue(did, Float.fromBits(newValue))
                        }
                    }
                } catch (e: Exception) {
                    return UDS_ERROR_UNKNOWN
                }
            }

            val dEnable = list[list.count() - 1]
            if ((!Settings.invertCruise && dEnable?.value != 0.0f) ||
                (Settings.invertCruise && dEnable?.value == 0.0f)) {
                //If we were not enabled before we must open a log to start writing
                if (!mLastEnabled) {
                    val currentDateTime = LocalDateTime.now()
                    LogFile.create("simoslogger-${currentDateTime.format(
                        DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss"))}.csv", context)

                    //Add time its required
                    var strItems: String? = "Time"

                    //Add PIDs
                    for (x in 0 until list.count()) {
                        strItems += ",${list[x]?.name}"
                    }

                    //reset torque / rpm pids
                    resetHPPIDS()

                    //Are we supposed to calculate HP? find PIDS
                    if (Settings.calculateHP) {
                        //Look for PIDS related to calculating HP
                        findHPPIDS()

                        //If we found PIDs add column
                        if (isCalcHP())
                            strItems += ",TQ,HP"
                    }

                    LogFile.addLine(strItems)
                }
                mLastEnabled = true

                //Write new values to log
                var strItems: String? = (bleHeader.tickCount.toFloat() / 1000.0f).toString()
                for (x in 0 until list.count()) {
                    strItems += ",${list[x]?.value}"
                }

                //Calculate HP and found PIDS?
                if (isCalcHP()) {
                    val calcTQ = getTorque()
                    val calcHP = getHP(calcTQ)
                    strItems += ",${calcTQ},${calcHP}"
                }

                LogFile.addLine(strItems)
            } else {
                if (mLastEnabled) {
                    LogFile.close()
                }
                mLastEnabled = false
            }

            return UDS_OK
        }

        return UDS_ERROR_UNKNOWN
    }
}