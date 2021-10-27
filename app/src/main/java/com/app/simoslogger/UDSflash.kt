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

    fun started(): Boolean {
        return !(mTask == FLASH_ECU_CAL_SUBTASK.NONE)
    }

    fun setBinFile(input: InputStream) {
        DebugLog.d(TAG, "Received BIN stream from GUI")
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

            DebugLog.d(TAG, "Initiating Flash subroutine: " + mTask.toString())
            mLastString = "Initiating flash routines"
            return buildBLEFrame(byteArrayOf(0x22.toByte()) + ECUInfo.PART_NUMBER.address)
        }
    }

    fun processFlashCAL(ticks: Int, buff: ByteArray?): UDSReturn {
        buff?.let {

            DebugLog.d(TAG, "Flash subroutine: " + mTask)

            when(mTask){
                FLASH_ECU_CAL_SUBTASK.GET_ECU_BOX_CODE ->{
                    DebugLog.d(TAG, "Processing ASW response from ECU: " + buff.toHex())
                    //response should be the ASW version... parse it out
                    if (buff[0] != 0x62.toByte()) {
                        DebugLog.d(TAG, "Error with ECU Response: " + buff.toHex())
                        mLastString = "Error with ECU Response: " + String(buff)
                        return UDSReturn.ERROR_UNKNOWN
                    }

                    ecuAswVersion = buff.copyOfRange(3, buff.size)
                    DebugLog.d(TAG, "Received ASW version ${ecuAswVersion.toHex()} from ecu")

                    mTask = mTask.next()

                    mLastString = "Read box code from ECU: " + String(ecuAswVersion)
                    mCommand = sendTesterPresent()
                    return UDSReturn.COMMAND_QUEUED
                }


                FLASH_ECU_CAL_SUBTASK.CHECK_FILE_COMPAT -> {

                    val binAswVersion = bin.copyOfRange(0x60, 0x6B)

                    //Compare the two strings:
                    if (String(ecuAswVersion).trim() != String(binAswVersion).trim()) {
                        DebugLog.d(TAG,"ECU software version: ${ecuAswVersion.toHex()}, and file" +
                                " software version: ${binAswVersion.toHex()}")
                        return UDSReturn.ERROR_RESPONSE
                    }

                    mLastString = mTask.toString() + "\nBox code on selected BIN file: " + String(binAswVersion)
                    mTask = mTask.next()
                    mCommand = sendTesterPresent()
                    return UDSReturn.COMMAND_QUEUED


                }

                FLASH_ECU_CAL_SUBTASK.CHECKSUM_BIN ->{
                    mLastString = mTask.toString() + "\n"

                    var checksummed = FlashUtilities.checksumSimos18(bin)
                    mLastString += "Original checksum: " + checksummed.fileChecksum + "\n"
                    mLastString += "Calculatated checksum: " + checksummed.calculatedChecksum + "\n"
                    if(checksummed.updated) mLastString += "    Checksum corrected\n"
                    else mLastString += "    Checksum not updated\n"

                    checksummed = FlashUtilities.checksumECM3(checksummed.bin)
                    mLastString += "Original ECM3: " + checksummed.fileChecksum + "\n"
                    mLastString += "    Calculated ECM3: " + checksummed.calculatedChecksum + "\n"
                    if(checksummed.updated) mLastString += "Checksum corrected\n"
                    else mLastString += "    Checksum not updated\n"


                    mTask = mTask.next()
                    mCommand = sendTesterPresent()
                    return UDSReturn.COMMAND_QUEUED

                }

                FLASH_ECU_CAL_SUBTASK.COMPRESS_BIN ->{
                    mLastString = mTask.toString() + "\n"

                    var uncompressedSize = bin.size
                    bin = FlashUtilities.encodeLZSS(bin)

                    var compressedSize = bin.size

                    mLastString += "Uncompressed bin size: " + uncompressedSize + "\n"
                    mLastString += "Compressed bin size: " + compressedSize

                    mTask = mTask.next()
                    mCommand = sendTesterPresent()
                    return UDSReturn.COMMAND_QUEUED

                }

                FLASH_ECU_CAL_SUBTASK.ENCRYPT_BIN -> {
                    mLastString = mTask.toString() + "\n"
                    var unencryptedSize = bin.size

                    bin = FlashUtilities.encrypt(bin)

                    var encryptedSize = bin.size

                    mLastString += "Unencrypted bin size: " + unencryptedSize + "\n"
                    mLastString += "Encrypted bin size: " + encryptedSize


                    mTask = mTask.next()
                    mCommand = sendTesterPresent()
                    return UDSReturn.COMMAND_QUEUED
                }

                FLASH_ECU_CAL_SUBTASK.CLEAR_DTC -> {
                    mCommand = clearDTC()
                    mLastString = mTask.toString()
                    mTask = mTask.next()
                    return UDSReturn.COMMAND_QUEUED
                }
                FLASH_ECU_CAL_SUBTASK.CHECK_PROGRAMMING_PRECONDITION -> {
                    //Check programming precondition, routine 0x0203
                    mCommand = buildBLEFrame(byteArrayOf(0x31.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte()))

                    mLastString = mTask.toString()
                    mTask = mTask.next()

                    return UDSReturn.COMMAND_QUEUED

                }
                FLASH_ECU_CAL_SUBTASK.OPEN_EXTENDED_DIAGNOSTIC -> {
                    //First entry should be a postivie response from 0x31
                    if(buff[0] == 0x71.toByte()){
                        //Open extended diagnostic session
                        mCommand = buildBLEFrame(byteArrayOf(0x10.toByte(), 0x03.toByte()))
                        mLastString = "Entering extended diagnostics"
                        return UDSReturn.COMMAND_QUEUED
                    }
                    //Response should be 0x50 0x03
                    //
                    else if(buff[0] == 0x50.toByte() && buff[1] == 0x03.toByte()){

                        mCommand = buildBLEFrame(byteArrayOf(0x27.toByte(), 0x11.toByte()))
                        mLastString = "Entering extended diagnostics"
                        mTask = mTask.next()
                        return UDSReturn.COMMAND_QUEUED
                    }

                    else{
                        return UDSReturn.ERROR_UNKNOWN
                    }

                }
                

                FLASH_ECU_CAL_SUBTASK.SA2SEEDKEY -> {
                    //Pass SA2SeedKey unlock_security_access(17)
                    if(buff[0] == 0x67.toByte() && buff[1] == 0x11.toByte()){
                        var challenge = buff.copyOfRange(2,buff.size)

                        var vs = FlashUtilities.Sa2SeedKey(VW_SEEDKEY_TAPE, challenge)
                        var response = vs.execute()

                        mCommand = buildBLEFrame(byteArrayOf(0x27.toByte(), 0x12.toByte()) + response)
                        mLastString = "Passing SeedKey challenege"
                        return UDSReturn.COMMAND_QUEUED
                    }
                    else if(buff[0] == 0x67.toByte() && buff[1] == 0x12.toByte()){
                        mLastString = "Passed SeedKey Challenege"
                        mCommand = sendTesterPresent()
                        mTask = mTask.next()
                        return UDSReturn.COMMAND_QUEUED
                    }
                    else{
                        return UDSReturn.ERROR_UNKNOWN
                    }

                }
                FLASH_ECU_CAL_SUBTASK.WRITE_WORKSHOP_LOG -> {
                    if(buff[0] == 0x7e.toByte()){
                        //Write workshop tool log
                        //  0x 2E 0xF15A = 0x20, 0x7, 0x17, 0x42,0x04,0x20,0x42,0xB1,0x3D,
                        mCommand = buildBLEFrame(byteArrayOf(0x2E.toByte(),
                            0xF1.toByte(), 0x5A.toByte(), 0x20.toByte(), 0x07.toByte(), 0x17.toByte(),
                            0x42.toByte(),0x04.toByte(),0x20.toByte(),0x42.toByte(),0xB1.toByte(),0x3D.toByte()))
                        mLastString = "Writing workshop code"
                        return UDSReturn.COMMAND_QUEUED

                    }
                    else if(buff[0] == 0x6E.toByte() && buff[1] == 0xF1.toByte() && buff[2] == 0x5A.toByte()){
                        mLastString = "Wrote workshop code"
                        mCommand = sendTesterPresent()
                        mTask = mTask.next()
                        return UDSReturn.COMMAND_QUEUED
                    }
                    else {
                        UDSReturn.ERROR_UNKNOWN
                    }
                }
                FLASH_ECU_CAL_SUBTASK.FLASH_BLOCK -> {
                    if(buff[0] == 0x7e.toByte()){
                        //erase block: 31 01 FF 00 01 05
                    }
                    else if(buff[0] == 0x71.toByte()){
                        //Request download 34 AA 41 05 00 07 FC 00
                    }
                    else if(buff[0] == 0x74.toByte()){
                        //Send bytes, 0x36 [frame number]
                        //Break the whole bin into frames of FFD size, and
                        // we'll use that array.
                    }
                    else if(buff[0] == 0x76.toByte()){
                        //buff[1] == frame number that was received
                        //if buff[1] == bin array size, send 0x37
                    }
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


                }
                FLASH_ECU_CAL_SUBTASK.VERIFY_PROGRAMMING_DEPENDENCIES -> {
                    //Verify programming dependencies, routine 0xFF01


                }
                FLASH_ECU_CAL_SUBTASK.RESET_ECU -> {
                    //Reset ECU
                    return UDSReturn.OK
                }
            }

            DebugLog.d(TAG, "No valid flash subroutine triggered")
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