package com.app.simoslogger

import android.net.Uri
import android.app.Activity
import com.app.simoslogger.FlashUtilities
import java.io.InputStream


object UDSFlasher {
    private val TAG = "UDSflash"
    private var mTask = FLASH_ECU_CAL_SUBTASK.NONE
    private var mCommand: ByteArray = byteArrayOf()
    private var mLastString: String = ""
    private var bin: ByteArray = byteArrayOf()
    private var ecuAswVersion: ByteArray = byteArrayOf()

    fun getInfo(): String {
        return mLastString
    }

    fun getCommand(): ByteArray {
        return mCommand
    }

    fun finished(): Boolean {
        return mTask == FLASH_ECU_CAL_SUBTASK.NONE
    }

    fun setBinFile(input: InputStream) {
        DebugLog.i(TAG, "Received BIN stream from GUI")
        bin =  input.readBytes()
    }

    fun buildFlashCAL(ticks: Int): ByteArray {
        if(bin.size == 0){
            mLastString = "Selected file is empty!"
            return byteArrayOf()
        }
        else{
            //Read box code from ECU
            mTask = FLASH_ECU_CAL_SUBTASK.GET_ECU_BOX_CODE

            return buildBLEFrame(byteArrayOf(0x22.toByte()) + ECUInfo.ASW_VERSION.address)
        }
    }

    fun processFlashCAL(ticks: Int, buff: ByteArray?): UDSReturn {
        buff?.let {
            DebugLog.i(TAG, "Flash subroutine: " + mTask.toString())
            when(mTask){
                FLASH_ECU_CAL_SUBTASK.GET_ECU_BOX_CODE ->{

                    //response should be the ASW version... parse it out
                    if (buff[0] != 0x62.toByte()) {
                        return UDSReturn.ERROR_UNKNOWN
                    }

                    mLastString = mTask.toString()
                    mTask = mTask.next()
                    ecuAswVersion = buff.copyOfRange(1, buff.size - 1)
                    return UDSReturn.OK
                }


                FLASH_ECU_CAL_SUBTASK.CHECK_FILE_COMPAT -> {

                    val binAswVersion = bin.copyOfRange(0x60, 0x6B)

                    //Compare the two strings:
                    if (String(ecuAswVersion) != String(binAswVersion)) {
                        return UDSReturn.ERROR_RESPONSE
                    }

                    mLastString = mTask.toString()
                    mTask = mTask.next()
                    return UDSReturn.OK

                }

                FLASH_ECU_CAL_SUBTASK.CHECKSUM_BIN ->{
                    bin = FlashUtilities.checksumSimos18(bin)
                    bin = FlashUtilities.checksumECM3(bin)

                    mLastString = mTask.toString()
                    mTask = mTask.next()
                    return UDSReturn.OK

                }

                FLASH_ECU_CAL_SUBTASK.COMPRESS_BIN ->{
                    bin = FlashUtilities.encodeLZSS(bin)

                    mLastString = mTask.toString()
                    mTask = mTask.next()
                    return UDSReturn.OK

                }

                FLASH_ECU_CAL_SUBTASK.ENCRYPT_BIN -> {
                    bin = FlashUtilities.encrypt(bin)

                    mLastString = mTask.toString()
                    mTask = mTask.next()
                    return UDSReturn.OK
                }

                FLASH_ECU_CAL_SUBTASK.CLEAR_DTC -> {
                    mCommand = clearDTC()
                    mLastString = mTask.toString()
                    mTask = mTask.next()
                    return UDSReturn.COMMAND_QUEUED
                }

                FLASH_ECU_CAL_SUBTASK.OPEN_EXTENDED_DIAGNOSTIC -> {

                    //Open extended diagnostic session
                    mCommand = buildBLEFrame(byteArrayOf(0x10.toByte(), 0x04.toByte()))

                    mLastString = mTask.toString()
                    mTask = mTask.next()

                    return UDSReturn.COMMAND_QUEUED

                }
                
                FLASH_ECU_CAL_SUBTASK.CHECK_PROGRAMMING_PRECONDITION -> {
                    //Check programming precondition, routine 0x0203
                    mCommand = buildBLEFrame(byteArrayOf(0x01.toByte(), 0x02.toByte(), 0x03.toByte()))

                    mLastString = mTask.toString()
                    mTask = mTask.next()

                    return UDSReturn.COMMAND_QUEUED

                }
                FLASH_ECU_CAL_SUBTASK.SA2SEEDKEY -> {
                    //Pass SA2SeedKey unlock_security_access(17)


                }
                FLASH_ECU_CAL_SUBTASK.WRITE_WORKSHOP_LOG -> {
                    //Write workshop tool log
                    //  0xF15A = 0x20, 0x7, 0x17, 0x42,0x04,0x20,0x42,0xB1,0x3D,

                    if (true) {
                        mTask = mTask.next()
                        return UDSReturn.OK
                    } else {
                        UDSReturn.ERROR_UNKNOWN
                    }
                }
                FLASH_ECU_CAL_SUBTASK.FLASH_BLOCK -> {
                    //FLASH BLOCK
                    //  erase block 0x01 0x05
                    //  request download
                    //  transfer data in blocks
                    //  request transfer exit
                    //  tester present
                    if (true) {
                        mTask = mTask.next()
                        return UDSReturn.OK
                    } else {
                        UDSReturn.ERROR_UNKNOWN
                    }
                }
                FLASH_ECU_CAL_SUBTASK.CHECKSUM_BLOCK -> {
                    //  run checksum start_routine(0x0202, data=bytes(checksum_data))

                    if (true) {
                        mTask = mTask.next()
                        return UDSReturn.OK
                    } else {
                        UDSReturn.ERROR_UNKNOWN
                    }
                }
                FLASH_ECU_CAL_SUBTASK.VERIFY_PROGRAMMING_DEPENDENCIES -> {
                    //Verify programming dependencies, routine 0xFF01

                    if (true) {
                        mTask = mTask.next()
                        return UDSReturn.OK
                    } else {
                        UDSReturn.ERROR_UNKNOWN
                    }
                }
                FLASH_ECU_CAL_SUBTASK.RESET_ECU -> {
                    //Reset ECU
                    return UDSReturn.OK
                }
            }
            return UDSReturn.ERROR_UNKNOWN
        }

        return UDSReturn.ERROR_NULL
    }

    private fun buildBLEFrame(udsCommand: ByteArray): ByteArray{
        val bleHeader = BLEHeader()
        bleHeader.cmdSize = udsCommand.size
        bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value

        return bleHeader.toByteArray() + udsCommand
    }

    private fun clearDTC(): ByteArray{
        //Send clear request
        val bleHeader = BLEHeader()
        bleHeader.rxID = 0x7E8
        bleHeader.txID = 0x700
        bleHeader.cmdSize = 1
        bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value
        val dataBytes = byteArrayOf(0x04.toByte())
        val buf = bleHeader.toByteArray() + dataBytes
        return buf
    }

    private fun sendTesterPresent(): ByteArray{
        return buildBLEFrame(byteArrayOf(0x3e.toByte(), 0x02.toByte()))
    }

}