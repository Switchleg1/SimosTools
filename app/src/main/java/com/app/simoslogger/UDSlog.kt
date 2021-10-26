package com.app.simoslogger

import android.content.Context
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

object UDSLogger {
    private val TAG = "UDSlog"
    private var mLastEnabled = false
    private var mMode = UDSLoggingMode.MODE_22
    private var mTorquePID = -1
    private var mEngineRPMPID = -1
    private var mMS2PID = -1
    private var mGearPID = -1
    private var mVelocityPID = -1
    private var mTireCircumference = -1f
    private var mFoundMS2PIDS = false
    private var mFoundTQPIDS = false
    private var mEnabledArray22 = byteArrayOf()
    private var mEnabledArray3E = byteArrayOf()
    private var mAddressArray3E = byteArrayOf()


    fun isCalcHP(): Boolean {
        if(Settings.calculateHP && (mFoundTQPIDS || mFoundMS2PIDS)) {
            return true
        }

        return false
    }

    fun getTorque(): Float {
        PIDs.getList()?.let { list ->
            if (Settings.calculateHP) {
                if (mFoundMS2PIDS && Settings.useMS2Torque) {
                    try {
                        val gearValue = list[mGearPID]!!.value.toInt()

                        if (gearValue in 1..7) {
                            val ms2Value = sqrt(list[mMS2PID]!!.value)
                            val velValue = list[mVelocityPID]!!.value
                            val weightValue = Settings.curbWeight * KG_TO_N
                            val ratioValue = sqrt(GearRatios.values()[gearValue - 1].ratio * GearRatios.FINAL.ratio)
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
        PIDs.getList()?.let { list ->
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

    fun setMode(mode: UDSLoggingMode) {
        mMode = mode
    }

    fun getMode(): UDSLoggingMode {
        return mMode
    }

    fun frameCount(): Int {
        return when (mMode) {
            UDSLoggingMode.MODE_22 -> frameCount22()
            UDSLoggingMode.MODE_3E -> frameCount3E()
        }
    }

    fun buildFrame(index: Int): ByteArray {
        return when(mMode) {
            UDSLoggingMode.MODE_22 -> buildFrame22(index)
            UDSLoggingMode.MODE_3E -> buildFrame3E(index)
        }
    }

    fun processFrame(tick: Int, buff: ByteArray?, context: Context): UDSReturn {
        return when(mMode) {
            UDSLoggingMode.MODE_22 -> processFrame22(tick, buff, context)
            UDSLoggingMode.MODE_3E -> processFrame3E(tick, buff, context)
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
        when(mMode) {
            UDSLoggingMode.MODE_22 -> findHPPIDS22()
            UDSLoggingMode.MODE_3E -> findHPPIDS3E()
        }
    }

    private fun findHPPIDS22() {
        PIDs.list22?.let { list ->
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
            //pid we find the PIDs required?
            if (mEngineRPMPID != -1 && mTorquePID != -1)
                mFoundTQPIDS = true
        }
    }

    private fun findHPPIDS3E() {
        PIDs.list3E?.let { list ->
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
        //pid we find the PIDs required?
        if(mEngineRPMPID != -1 && mMS2PID != -1 && mGearPID != -1 && mVelocityPID != -1)
            mFoundMS2PIDS = true

        if(mEngineRPMPID != -1 && mTorquePID != -1)
            mFoundTQPIDS = true
    }

    private fun frameCount22(): Int {
        return (mEnabledArray22.count()-1) / 8 + 1
    }

    private fun frameCount3E(): Int {
        return (mAddressArray3E.count() / 0x8F) + 2
    }

    private fun buildEnabledArray(mode: UDSLoggingMode = getMode()): ByteArray {
        PIDs.getList(mode)?.let { list ->
            //build list of addresses that are enabled
            var enabledArray: ByteArray = byteArrayOf()
            for (i in 0 until list.count()) {
                val pid: PIDStruct? = list[i]
                pid?.let {
                    if (it.enabled) {
                        enabledArray += i.toByte()
                    }
                }
            }

            return enabledArray
        }

        return byteArrayOf()
    }

    private fun buildAddress3E(): ByteArray {
        PIDs.list3E?.let { list ->
            //build list of addresses that are enabled
            var addressArray: ByteArray = byteArrayOf()
            for (i in 0 until mEnabledArray3E.count()) {
                val pid: PIDStruct? = list[mEnabledArray3E[i].toInt()]
                pid?.let {
                    if (it.enabled) {
                        addressArray += (it.length and 0xFF).toByte()
                        addressArray += it.address.toArray4()
                    }
                }
            }
            addressArray += 0

            return addressArray
        }

        return byteArrayOf()
    }

    private fun buildFrame22(index: Int): ByteArray {
        if(index == 0)
            mEnabledArray22 = buildEnabledArray()

        PIDs.list22?.let { list ->
            val frameCount = frameCount22()
            if (index in 0 until frameCount) {
                //Build header
                val bleHeader = BLEHeader()
                bleHeader.cmdSize = 1
                bleHeader.cmdFlags = when (index) {
                    0 -> BLECommandFlags.PER_ADD.value or BLECommandFlags.PER_CLEAR.value
                    frameCount - 1 -> BLECommandFlags.PER_ADD.value or BLECommandFlags.PER_ENABLE.value
                    else -> BLECommandFlags.PER_ADD.value
                }

                //add pids to buffer
                var pidBuff = byteArrayOf(0x22.toByte())
                val startIndex =
                    if (index * 8 > mEnabledArray22.count()) mEnabledArray22.count()
                    else index * 8
                val endIndex =
                    if (startIndex + 8 > mEnabledArray22.count()) mEnabledArray22.count()
                    else startIndex + 8
                for (i in startIndex until endIndex) {
                    bleHeader.cmdSize += 2
                    pidBuff += list[mEnabledArray22[i].toInt()]!!.address.toArray2()
                }
                return bleHeader.toByteArray() + pidBuff
            }
        }

        return byteArrayOf()
    }

    private fun buildFrame3E(index: Int): ByteArray {
        if(index == 0) {
            mEnabledArray3E = buildEnabledArray()
            mAddressArray3E = buildAddress3E()
        }

        //Do we even have any PIDs in the range?  If not send persist message
        if (index * 0x8F >= mAddressArray3E.count()) {
            if(index == frameCount3E() - 1) {
                val bleHeader = BLEHeader()
                bleHeader.cmdSize = 6
                bleHeader.cmdFlags =
                    BLECommandFlags.PER_CLEAR.value or BLECommandFlags.PER_ADD.value or BLECommandFlags.PER_ENABLE.value

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

            DebugLog.d(TAG, "Building 3E frame $index does not exist")
            return byteArrayOf()
        }

        //constrain copy range or we will receive an exception
        val endOfArray = if ((1 + index) * 0x8F > mAddressArray3E.count()) {
            mAddressArray3E.count()
        } else {
            (1 + index) * 0x8F
        }
        val selectArray: ByteArray = mAddressArray3E.copyOfRange(index * 0x8F, endOfArray)
        val bleHeader = BLEHeader()
        bleHeader.cmdSize = 8 + selectArray.count()
        bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value

        val memoryOffset = 0xB001E700 + (index * 0x8F)
        val writeBuffer: ByteArray = bleHeader.toByteArray() + byteArrayOf(
            0x3e.toByte(),
            0x32.toByte()
        ) + memoryOffset.toArray4() + selectArray.count().toArray2() + selectArray

        DebugLog.d(TAG, "Building 3E frame $index with length ${writeBuffer.count()}: ${writeBuffer.toHex()}")
        return writeBuffer
    }

    private fun processFrame22(tick: Int, buff: ByteArray?, context: Context): UDSReturn {
        // if the buffer is null abort
        if (buff == null) {
            return UDSReturn.ERROR_NULL
        }

        // check to make sure ble header byte matches
        val bleHeader = BLEHeader()
        bleHeader.fromByteArray(buff)
        val bData = buff.copyOfRange(8, buff.size)
        if (!bleHeader.isValid()) {
            return UDSReturn.ERROR_HEADER
        }

        // does the size of the data match the header?
        if (bData.count() != bleHeader.cmdSize) {
            return UDSReturn.ERROR_CMDSIZE
        }

        // make sure we received an 'OK' from the ECU
        if (bData[0] != 0x62.toByte()) {
            return UDSReturn.ERROR_RESPONSE
        }

        //In init state
        if (tick < frameCount22()) {
            return UDSReturn.OK
        }

        // process the data in the buffer
        var i = 1
        while (i <= bleHeader.cmdSize - 3) {
            val pid: PIDStruct =
                PIDs.getPID(((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF).toLong())
                    ?: return UDSReturn.ERROR_UNKNOWN
            if (pid.length == 1) {
                if (pid.signed) {
                    PIDs.setValue(pid, (bData[i++] and 0xFF).toByte().toFloat())
                } else {
                    PIDs.setValue(pid, (bData[i++] and 0xFF).toFloat())
                }
            } else {
                if (pid.signed) {
                    PIDs.setValue(
                        pid,
                        (((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF)).toShort()
                            .toFloat()
                    )
                } else {
                    PIDs.setValue(
                        pid,
                        (((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF)).toFloat()
                    )
                }
            }
        }

        //Update Log once all pids have been updated
        if (tick % frameCount22() == 0) {
            //Update PID data
            PIDs.updateData()

            //Check if we need to write to log
            return writeToLog(bleHeader.tickCount, context)
        }

        return UDSReturn.OK
    }

    private fun processFrame3E(tick: Int, buff: ByteArray?, context: Context): UDSReturn {
        PIDs.getList()?.let { list ->
            // if the buffer is null abort
            if (buff == null) {
                return UDSReturn.ERROR_NULL
            }

            // check to make sure ble header byte matches
            val bleHeader = BLEHeader()
            bleHeader.fromByteArray(buff)
            val bData = buff.copyOfRange(8, buff.size)
            if (!bleHeader.isValid()) {
                return UDSReturn.ERROR_HEADER
            }

            // does the size of the data match the header?
            if (bData.count() != bleHeader.cmdSize) {
                return UDSReturn.ERROR_CMDSIZE
            }

            // make sure we received an 'OK' from the ECU
            if (bData[0] != 0x7e.toByte()) {
                return UDSReturn.ERROR_RESPONSE
            }

            //still in the initial setup?
            if (tick < frameCount3E()) {
                return UDSReturn.OK
            }

            //Update PID Values
            var dPos = 1
            for (i in 0 until mEnabledArray3E.count()) {
                val pid: PIDStruct? = list[mEnabledArray3E[i].toInt()]
                try {
                    //make sure we are in range
                    if (dPos + pid!!.length > bData.count())
                        break

                    //Build the value in little endian
                    var newValue: Int = bData[dPos + pid.length - 1] and 0xFF
                    for (d in 1 until pid.length) {
                        newValue = newValue shl 8
                        newValue += bData[dPos + pid.length - d - 1] and 0xFF
                    }
                    dPos += pid.length

                    //set pid values
                    if (pid.signed) {
                        when (pid.length) {
                            1 -> PIDs.setValue(pid, newValue.toByte().toFloat())
                            2 -> PIDs.setValue(pid, newValue.toShort().toFloat())
                            4 -> PIDs.setValue(pid, newValue.toFloat())
                        }
                    } else {
                        when (pid.length) {
                            1 -> PIDs.setValue(pid, newValue.toFloat())
                            2 -> PIDs.setValue(pid, newValue.toFloat())
                            4 -> PIDs.setValue(pid, Float.fromBits(newValue))
                        }
                    }
                } catch (e: Exception) {
                    return UDSReturn.ERROR_UNKNOWN
                }
            }

            //Update PID data
            PIDs.updateData()

            //Check if we need to write to log
            return writeToLog(bleHeader.tickCount, context)
        }

        return UDSReturn.ERROR_UNKNOWN
    }

    private fun writeToLog(tick: Int, context: Context): UDSReturn {
        PIDs.getList()?.let { list ->
            val dEnable = list[list.count() - 1]
            if ((!Settings.invertCruise && dEnable?.value != 0.0f) ||
                (Settings.invertCruise && dEnable?.value == 0.0f)
            ) {
                //If we were not enabled before we must open a log to start writing
                if (!mLastEnabled) {
                    val currentDateTime = LocalDateTime.now()
                    LogFile.create(
                        "simoslogger-${
                            currentDateTime.format(
                                DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss")
                            )
                        }.csv", context
                    )

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

                        //If we found HP PIDs add columns
                        if (isCalcHP())
                            strItems += ",TQ,HP"
                    }

                    LogFile.addLine(strItems)
                }
                mLastEnabled = true

                //Write new values to log
                var strItems: String? = (tick.toFloat() / 1000.0f).toString()
                for (x in 0 until list.count()) {
                    strItems += ",${list[x]?.value}"
                }

                //Calculate HP and found PIDS?
                if (isCalcHP()) {
                    val calcTQ = getTorque()
                    val calcHP = getHP(calcTQ)
                    strItems += ",$calcTQ,$calcHP"
                }

                LogFile.addLine(strItems)
            } else {
                if (mLastEnabled) {
                    LogFile.close()
                }
                mLastEnabled = false
            }

            return UDSReturn.OK
        }

        return UDSReturn.ERROR_UNKNOWN
    }
}