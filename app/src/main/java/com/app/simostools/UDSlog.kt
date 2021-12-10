package com.app.simostools

import android.content.Context
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

object UDSLogger {
    private val TAG                 = "UDSlog"
    private var mLastEnabled        = false
    private var mMode               = UDSLoggingMode.MODE_22
    private var mLogDSG             = false
    private var mTorquePID          = -1
    private var mEngineRPMPID       = -1
    private var mMS2PID             = -1
    private var mGearPID            = -1
    private var mVelocityPID        = -1
    private var mTireCircumference  = -1f
    private var mFoundMS2PIDS       = false
    private var mFoundTQPIDS        = false
    private var mEnabledArray22     = byteArrayOf()
    private var mEnabledArray3E     = byteArrayOf()
    private var mEnabledArrayDSG    = byteArrayOf()
    private var mAddressArray22     = byteArrayOf()
    private var mAddressArray3E     = byteArrayOf()
    private var mAddressArrayDSG    = byteArrayOf()
    private var mTimeoutCounter     = TIME_OUT_LOGGING
    private var mCalculatedTQ       = 0f
    private var mCalculatedHP       = 0f
    private var mLastFrameSize      = -1
    private var mRevision           = "SimosTools [R1.4:We don't respond to emails]"

    fun clear() {
        LogFile.close()
        mEnabledArray22     = byteArrayOf()
        mEnabledArray3E     = byteArrayOf()
        mEnabledArrayDSG    = byteArrayOf()
        mAddressArray22     = byteArrayOf()
        mAddressArray3E     = byteArrayOf()
        mAddressArrayDSG    = byteArrayOf()
    }

    fun getTQ(): Float {
        return mCalculatedTQ
    }

    fun getHP(): Float {
        return mCalculatedHP
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

    fun setModeDSG(dsg: Boolean) {
        mLogDSG = dsg
    }

    fun getModeDSG(): Boolean {
        return mLogDSG
    }

    fun frameCount(): Int {
        return when (mMode) {
            UDSLoggingMode.MODE_22 -> {
                if(mLogDSG) frameCount22() + frameCountDSG()
                else frameCount22()
            }
            UDSLoggingMode.MODE_3E -> {
                if(mLogDSG) frameCount3E() + frameCountDSG()
                else frameCount3E()
            }
        }
    }

    fun startTask(index: Int): ByteArray {
        return when(mMode) {
            UDSLoggingMode.MODE_22 -> buildFrame22(index)
            UDSLoggingMode.MODE_3E -> buildFrame3E(index)
        }
    }

    fun processPacket(tick: Int, buff: ByteArray?, context: Context): UDSReturn {
        buff?.let {
            resetTimeout()

            return when (mMode) {
                UDSLoggingMode.MODE_22 -> processFrame22(tick, buff, context)
                UDSLoggingMode.MODE_3E -> processFrame3E(tick, buff, context)
            }
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
        mTimeoutCounter = TIME_OUT_LOGGING
    }

    private fun isCalcHP(): Boolean {
        if(ConfigSettings.CALCULATE_HP.toBoolean() && (mFoundTQPIDS || mFoundMS2PIDS)) {
            return true
        }

        return false
    }

    private fun calcTQ() {
        PIDs.getList()?.let { list ->
            if (ConfigSettings.CALCULATE_HP.toBoolean()) {
                if (mFoundMS2PIDS && ConfigSettings.USE_MS2.toBoolean()) {
                    try {
                        val gearValue = list[mGearPID]!!.value.toInt()

                        if (gearValue in 1..7) {
                            val ms2Value = sqrt(list[mMS2PID]!!.value)
                            val velValue = list[mVelocityPID]!!.value
                            val weightValue = ConfigSettings.CURB_WEIGHT.toFloat() * KG_TO_N
                            val ratioValue = sqrt(GearRatios.values()[gearValue - 1].ratio * GearRatios.FINAL.ratio)
                            val dragValue = 1.0 + ((velValue * velValue).toDouble() * ConfigSettings.DRAG_COEFFICIENT.toDouble())

                            mCalculatedTQ = ((weightValue * ms2Value / ratioValue / mTireCircumference / TQ_CONSTANT).toDouble() * dragValue).toFloat()
                        }
                    } catch (e: Exception) {
                        mCalculatedTQ = 0f
                    }
                } else if (mFoundTQPIDS) {
                    mCalculatedTQ = try {
                        list[mTorquePID]!!.value
                    } catch (e: Exception) {
                        0f
                    }
                }
            }
        }
    }

    private fun calcHP() {
        PIDs.getList()?.let { list ->
            if (ConfigSettings.CALCULATE_HP.toBoolean() && mEngineRPMPID != -1) {
                mCalculatedHP = try {
                    val rpmValue = list[mEngineRPMPID]!!.value
                    mCalculatedTQ * rpmValue / 7127f
                } catch (e: Exception) {
                    0f
                }
            }
        }
    }

    private fun resetHPPIDS() {
        mFoundTQPIDS        = false
        mFoundMS2PIDS       = false
        mTorquePID          = -1
        mEngineRPMPID       = -1
        mMS2PID             = -1
        mGearPID            = -1
        mVelocityPID        = -1
        mTireCircumference  = ConfigSettings.TIRE_DIAMETER.toFloat() * 3.14f
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
        return ((mAddressArray22.count() - 1) / 16) + 1
    }

    private fun frameCount3E(): Int {
        return (mAddressArray3E.count() / 0x8F) + 2
    }

    private fun frameCountDSG(): Int {
        return ((mAddressArrayDSG.count() - 1) / 16) + 1
    }

    private fun buildEnabledArray(mode: UDSLoggingMode = getMode(), DSG: Boolean = false): ByteArray {
        val pidList = if(DSG) PIDs.listDSG
        else PIDs.getList()

        pidList?.let { list ->
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

    private fun buildEnabledArrayDSG(): ByteArray {
        return buildEnabledArray(UDSLoggingMode.MODE_22, true)
    }

    private fun buildAddress22(): ByteArray {
        PIDs.list22?.let { list ->
            //build list of addresses that are enabled
            var addressArray: ByteArray = byteArrayOf()
            for (i in 0 until mEnabledArray22.count()) {
                val pid: PIDStruct? = list[mEnabledArray22[i].toInt()]
                pid?.let {
                    if (it.enabled && it.address != UDSLoggingMode.MODE_22.addressMax) {
                        addressArray += it.address.toArray2()
                    }
                }
            }

            return addressArray
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
                    if (it.enabled && it.address != UDSLoggingMode.MODE_3E.addressMax) {
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

    private fun buildAddressDSG(): ByteArray {
        PIDs.listDSG?.let { list ->
            //build list of addresses that are enabled
            var addressArray: ByteArray = byteArrayOf()
            for (i in 0 until mEnabledArrayDSG.count()) {
                val pid: PIDStruct? = list[mEnabledArrayDSG[i].toInt()]
                pid?.let {
                    if (it.enabled && it.address != UDSLoggingMode.MODE_22.addressMax) {
                        addressArray += it.address.toArray2()
                    }
                }
            }

            return addressArray
        }

        return byteArrayOf()
    }

    private fun getMode22Buffer(index: Int, input: ByteArray?): ByteArray? {
        var output: ByteArray? = null
        input?.let {
            //add pids to buffer
            val startIndex =
                if (index * 16 > input.count()) input.count()
                else index * 16
            val endIndex =
                if (startIndex + 16 > input.count()) input.count()
                else startIndex + 16

            output = byteArrayOf(0x22.toByte()) + input.copyOfRange(startIndex, endIndex)
        }

        return output
    }

    private fun buildFrame22(index: Int): ByteArray {
        if(index == 0) {
            mEnabledArray22 = buildEnabledArray()
            mAddressArray22 = buildAddress22()
            resetHPPIDS()
            findHPPIDS()
            if(mLogDSG) {
                mEnabledArrayDSG = buildEnabledArrayDSG()
                mAddressArrayDSG = buildAddressDSG()
            }
        }

        //get frame counts
        val frameCount22    = frameCount22()
        val frameCountDSG   = frameCountDSG()
        val frameCount = if(mLogDSG) frameCount22 + frameCountDSG
        else frameCount22

        //find and send frame
        if (index in 0 until frameCount) {
            //Build header
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 1
            bleHeader.cmdFlags = when (index) {
                0 -> BLECommandFlags.PER_ADD.value or BLECommandFlags.PER_CLEAR.value
                frameCount - 1 -> BLECommandFlags.PER_ADD.value or BLECommandFlags.PER_ENABLE.value
                else -> BLECommandFlags.PER_ADD.value
            }

            //Are we sending ECU or DSG?
            var frameType = 0
            var frameIndex = index
            if(mLogDSG && index >= frameCount22) {
                frameIndex = index - frameCount22
                frameType = 1
            }

            //Get Frame data
            val buff = if(frameType == 0) { //ECU
                getMode22Buffer(frameIndex, mAddressArray22) ?: byteArrayOf()
            } else { //DSG
                bleHeader.rxID = BLE_HEADER_DSG_RX
                bleHeader.txID = BLE_HEADER_DSG_TX
                getMode22Buffer(frameIndex, mAddressArrayDSG) ?: byteArrayOf()
            }
            bleHeader.cmdSize = buff.count()
            val writeBuffer = bleHeader.toByteArray() + buff

            DebugLog.d(TAG, "Building 22 frame $frameIndex [Type: $frameType] with length ${writeBuffer.count()}: ${writeBuffer.toHex()}")
            return writeBuffer
        }

        DebugLog.d(TAG, "Building 22 frame $index does not exist")
        return byteArrayOf()
    }

    private fun buildFrame3E(index: Int): ByteArray {
        if(index == 0) {
            mEnabledArray3E = buildEnabledArray()
            mAddressArray3E = buildAddress3E()
            resetHPPIDS()
            findHPPIDS()
            if(mLogDSG) {
                mEnabledArrayDSG = buildEnabledArrayDSG()
                mAddressArrayDSG = buildAddressDSG()
            }
        }

        //get frame counts
        val frameCount3E    = frameCount3E()
        val frameCountDSG   = frameCountDSG()
        val frameCount = if(mLogDSG) frameCount3E + frameCountDSG
        else frameCount3E

        //find and send frame
        if (index in 0 until frameCount) {
            //Build header
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 1
            bleHeader.cmdFlags = when {
                index == frameCount - 1 -> BLECommandFlags.PER_ADD.value or BLECommandFlags.PER_ENABLE.value
                index >= frameCount3E - 1 -> BLECommandFlags.PER_ADD.value
                else -> BLECommandFlags.PER_CLEAR.value
            }

            //Are we sending ECU or DSG?
            var frameType = 0
            var frameIndex = index
            if(mLogDSG && index >= frameCount3E) {
                frameIndex = index - frameCount3E
                frameType = 1
            }

            //Get Frame data
            val buff = if(frameType == 0) { //ECU
                //Do we even have any PIDs in the range?  If not send persist message
                if (index * 0x8F >= mAddressArray3E.count()) {
                    var writeBuffer: ByteArray = byteArrayOf()
                    if(index == frameCount3E - 1) {
                        writeBuffer = byteArrayOf(
                            0x3e.toByte(),
                            0x33.toByte(),
                            0xb0.toByte(),
                            0x01.toByte(),
                            0xe7.toByte(),
                            0x00.toByte()
                        )

                        mLastFrameSize = -1
                    }

                    mLastFrameSize = -1
                    writeBuffer
                } else {
                    //constrain copy range or we will receive an exception
                    val endOfArray = if ((1 + index) * 0x8F > mAddressArray3E.count()) {
                        mAddressArray3E.count()
                    } else {
                        (1 + index) * 0x8F
                    }
                    val selectArray: ByteArray = mAddressArray3E.copyOfRange(index * 0x8F, endOfArray)
                    val memoryOffset = 0xB001E700 + (index * 0x8F)
                    val writeBuffer: ByteArray = byteArrayOf(
                        0x3e.toByte(),
                        0x32.toByte()
                    ) + memoryOffset.toArray4() + selectArray.count().toArray2() + selectArray

                    mLastFrameSize = selectArray.count()
                    writeBuffer
                }
            } else { //DSG
                bleHeader.rxID = BLE_HEADER_DSG_RX
                bleHeader.txID = BLE_HEADER_DSG_TX
                getMode22Buffer(frameIndex, mAddressArrayDSG) ?: byteArrayOf()
            }
            bleHeader.cmdSize = buff.count()
            val writeBuffer = bleHeader.toByteArray() + buff

            DebugLog.d(TAG, "Building 3E frame $frameIndex [Type: $frameType] with length ${writeBuffer.count()}: ${writeBuffer.toHex()}")
            return writeBuffer
        }

        DebugLog.d(TAG, "Building 3E frame $index does not exist")
        return byteArrayOf()
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
            DebugLog.d(TAG, "ERROR in response from ECU: " + bData.toHex())
            return UDSReturn.ERROR_RESPONSE
        }

        //get frame counts
        val frameCount22 = frameCount22()
        val frameCountDSG = frameCountDSG()
        val frameCount = if(mLogDSG) frameCount22 + frameCountDSG
        else frameCount22

        //In init state
        if (tick < frameCount) {
            return UDSReturn.OK
        }

        // process the data in the buffer
        var i = 1
        while (i <= bleHeader.cmdSize - 3) {
            //Find the PID in our ECU/DSG list
            var isDSG = false
            val pidAddress = ((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF).toLong()
            var pid: PIDStruct? = PIDs.getPID(pidAddress)
            if(pid == null && mLogDSG) {
                isDSG = true
                pid = PIDs.getPID(pidAddress, UDSLoggingMode.MODE_22, true)
            }

            //Set new PID value
            pid?.let {
                if (pid.length == 1) {
                    if (pid.signed) PIDs.setValue(pid, (bData[i++] and 0xFF).toByte().toFloat())
                    else PIDs.setValue(pid, (bData[i++] and 0xFF).toFloat())
                } else {
                    var d1 = bData[i++]
                    var d2 = bData[i++]
                    if(isDSG) {
                        val d3 = d1
                        d1 = d2
                        d2 = d3
                    }

                    if (pid.signed) PIDs.setValue(pid, (((d1 and 0xFF) shl 8) + (d2 and 0xFF)).toShort().toFloat())
                    else PIDs.setValue(pid, (((d1 and 0xFF) shl 8) + (d2 and 0xFF)).toFloat())
                }
            } ?: run {
                DebugLog.d(TAG, "PID Address not found: $pidAddress")
                return UDSReturn.ERROR_UNKNOWN
            }
        }

        //Update Log once all pids have been updated
        if (tick % frameCount22 == 0) {
            //Calculate HP and tq PIDS?
            calcTQ()
            calcHP()

            //Check and process non-addressable PIDS
            PIDs.list22?.let { list ->
                for (x in 0 until mEnabledArray22.count()) {
                    //Is this a real address?
                    list[x]?.let { pid ->
                        if (pid.address == UDSLoggingMode.MODE_22.addressMax) {
                            PIDs.setValue(pid, 0f)
                        }
                    }
                }
            }

            if(mLogDSG) {
                //Check and process non-addressable PIDS
                PIDs.listDSG?.let { list ->
                    for (x in 0 until mEnabledArrayDSG.count()) {
                        //Is this a real address?
                        list[x]?.let { pid ->
                            if (pid.address == UDSLoggingMode.MODE_22.addressMax) {
                                PIDs.setValue(pid, 0f)
                            }
                        }
                    }
                }
            }

            //Update PID data
            PIDs.updateData()
            if(mLogDSG)
                PIDs.updateDSGData()

            //don't log until stream is constant
            if(tick < 50)
                return UDSReturn.OK

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
            if (bData[0] != 0x62.toByte() && (bData.count() < 3 || bData[0] != 0x7e.toByte())) {
                DebugLog.d(TAG, "ERROR in response from ECU: " + bData.toHex())
                return UDSReturn.ERROR_RESPONSE
            }

            //get frame counts
            val frameCount3E    = frameCount3E()
            val frameCountDSG   = frameCountDSG()
            val frameCount = if(mLogDSG) frameCount3E + frameCountDSG
            else frameCount3E

            // make sure we received an 'OK' from the ECU while initiating
            if(tick < frameCount3E-1) {
                if(bData[1] != 0x00.toByte() || bData[2] != (mLastFrameSize and 0xFF).toByte())
                    return UDSReturn.ERROR_RESPONSE

                return UDSReturn.OK
            } else if(tick < frameCount) {
                return UDSReturn.OK
            }

            var isDSGFrame = false
            if(mLogDSG && bData[0] == 0x62.toByte()) {
                isDSGFrame = true
                // process the data in the buffer
                var i = 1
                while (i <= bleHeader.cmdSize - 3) {
                    //Find the PID in our ECU/DSG list
                    val pidAddress = ((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF).toLong()
                    val pid: PIDStruct? = PIDs.getPID(pidAddress, UDSLoggingMode.MODE_22, true)

                    //Set new PID value
                    pid?.let {
                        if (pid.length == 1) {
                            if (pid.signed) PIDs.setValue(pid, (bData[i++] and 0xFF).toByte().toFloat())
                            else PIDs.setValue(pid, (bData[i++] and 0xFF).toFloat())
                        } else {
                            val d2 = bData[i++]
                            val d1 = bData[i++]

                            if (pid.signed) PIDs.setValue(pid, (((d1 and 0xFF) shl 8) + (d2 and 0xFF)).toShort().toFloat())
                            else PIDs.setValue(pid, (((d1 and 0xFF) shl 8) + (d2 and 0xFF)).toFloat())
                        }
                    } ?: run {
                        DebugLog.d(TAG, "PID Address not found: $pidAddress")
                        return UDSReturn.ERROR_UNKNOWN
                    }
                }
            } else {
                //Update PID Values
                var dPos = 1
                for (i in 0 until mEnabledArray3E.count()) {
                    val pid = list[mEnabledArray3E[i].toInt()]!!
                    try {
                        //Is this a real address?
                        if (pid.address == UDSLoggingMode.MODE_3E.addressMax) {
                            PIDs.setValue(pid, 0f)
                        } else {
                            //make sure we are in range, if not report error
                            if (dPos + pid.length > bData.count()) {
                                return UDSReturn.ERROR_UNKNOWN
                            }

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
                        }
                    } catch (e: Exception) {
                        return UDSReturn.ERROR_UNKNOWN
                    }
                }
            }

            //Update Log once all pids have been updated
            if (!isDSGFrame) {
                //Calculate HP and tq PIDS?
                calcTQ()
                calcHP()

                if(mLogDSG) {
                    //Check and process non-addressable PIDS
                    PIDs.listDSG?.let { dsgList ->
                        for (x in 0 until mEnabledArrayDSG.count()) {
                            //Is this a real address?
                            dsgList[x]?.let { pid ->
                                if (pid.address == UDSLoggingMode.MODE_22.addressMax) {
                                    PIDs.setValue(pid, 0f)
                                }
                            }
                        }
                    }
                }

                //Update PID data
                PIDs.updateData()
                if (mLogDSG)
                    PIDs.updateDSGData()

                //don't log until stream is constant
                if(tick < 50)
                    return UDSReturn.OK

                //Check if we need to write to log
                return writeToLog(bleHeader.tickCount, context)
            }

            return UDSReturn.OK
        }

        return UDSReturn.ERROR_NULL
    }

    private fun writeToLog(tick: Int, context: Context): UDSReturn {
        PIDs.getList()?.let { list ->
            val dEnable = list[list.count() - 1]
            if ((!ConfigSettings.INVERT_CRUISE.toBoolean() && dEnable?.value != 0.0f) ||
                (ConfigSettings.INVERT_CRUISE.toBoolean() && dEnable?.value == 0.0f)
            ) {
                //If we were not enabled before we must open a log to start writing
                if (!mLastEnabled) {
                    val currentDateTime = LocalDateTime.now()
                    LogFile.create(
                        "${ConfigSettings.LOG_NAME}-${
                            currentDateTime.format(
                                DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss")
                            )
                        }.csv", ConfigSettings.LOG_SUB_FOLDER.toString(), context
                    )

                    //Add time its required
                    var strItems: String = "Time"

                    //Add PIDs including units
                    for (x in 0 until list.count()) {
                        if(x != list.count()-1) strItems += ",${list[x]?.name} (${list[x]?.unit})"
                            else strItems += ",$mRevision"
                    }

                    //Add DSG PIDs including units
                    if(mLogDSG) {
                        PIDs.listDSG?.let { dsgList ->
                            for (x in 0 until dsgList.count()) {
                                strItems += ",${dsgList[x]?.name} (${dsgList[x]?.unit})"
                            }
                        }
                    }

                    //Send it
                    LogFile.addLine(strItems)
                }
                mLastEnabled = true

                //Write new values to log
                var strItems: String = (tick.toFloat() / 1000.0f).toString()
                for (x in 0 until list.count()) {
                    strItems += ",${list[x]?.value}"
                }

                //Add DSG PIDs including units
                if(mLogDSG) {
                    PIDs.listDSG?.let { dsgList ->
                        for (x in 0 until dsgList.count()) {
                            strItems += ",${dsgList[x]?.value}"
                        }
                    }
                }

                //Send it
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
