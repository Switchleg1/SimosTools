package com.app.simoslogger

import java.io.InputStream
import java.lang.Math.round

object UDSFlasher {
    private val TAG = "UDSflash"
    private var mTask = FLASH_ECU_CAL_SUBTASK.NONE
    private var mCommand: ByteArray = byteArrayOf()
    private var mLastString: String = ""
    private var bin: ByteArray = byteArrayOf()
    private var ecuAswVersion: ByteArray = byteArrayOf()
    private var transferSequence = -1
    private var progress = 0

    fun getInfo(): String {
        return mLastString
    }

    fun getCommand(): ByteArray {
        return mCommand
    }

    fun started(): Boolean {
        return !(mTask == FLASH_ECU_CAL_SUBTASK.NONE)
    }

    fun getProgress(): Int{
        return progress
    }

    fun setBinFile(input: InputStream) {
        DebugLog.d(TAG, "Received BIN stream from GUI")
        mTask = FLASH_ECU_CAL_SUBTASK.NONE
        bin =  input.readBytes()
    }

    fun startTask(ticks: Int): ByteArray {

        if(bin.isEmpty()){
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
    
    @Synchronized
    fun processFlashCAL(ticks: Int, buff: ByteArray?): UDSReturn {

        buff?.let {


            DebugLog.d(TAG, "Flash subroutine: " + mTask)
            if(checkResponse(buff) == UDS_RESPONSE.NEGATIVE_RESPONSE){
                //DebugLog.w(TAG,"Negative response received from ECU!")
                //mCommand = sendTesterPresent()
                //return UDSReturn.COMMAND_QUEUED
            }

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

                    //val binAswVersion = bin.copyOfRange(0x60, 0x6B)
                    //Hard code an ASW version that won't work so it won't flash!
                    val binAswVersion = byteArrayOf(0xFF.toByte(),0xFF.toByte(),0xFF.toByte(),0xFF.toByte())

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

                    bin = checksummed.bin

                    mTask = mTask.next()
                    mCommand = sendTesterPresent()
                    return UDSReturn.COMMAND_QUEUED
                }

                FLASH_ECU_CAL_SUBTASK.COMPRESS_BIN ->{
                    mLastString = mTask.toString() + "\n"

                    var uncompressedSize = bin.size
                    bin = FlashUtilities.encodeLZSS(bin)

                    var compressedSize = bin.size

                    mLastString += "Uncompressed bin size: $uncompressedSize\n"
                    mLastString += "Compressed bin size: $compressedSize"

                    mTask = mTask.next()
                    mCommand = sendTesterPresent()
                    return UDSReturn.COMMAND_QUEUED

                }

                FLASH_ECU_CAL_SUBTASK.ENCRYPT_BIN -> {
                    mLastString = mTask.toString() + "\n"
                    var unencryptedSize = bin.size

                    bin = FlashUtilities.encrypt(bin, SIMOS18_AES_KEY, SIMOS18_AES_IV)

                    var encryptedSize = bin.size

                    mLastString += "Unencrypted bin size: $unencryptedSize \n"
                    mLastString += "Encrypted bin size: $encryptedSize"

                    if(bin.isEmpty()){
                        mLastString = "Error encrypting BIN"
                        return UDSReturn.ERROR_UNKNOWN
                    }
                    mTask = mTask.next()
                    mCommand = sendTesterPresent()
                    return UDSReturn.COMMAND_QUEUED
                }

                FLASH_ECU_CAL_SUBTASK.CLEAR_DTC -> {
                    when(checkResponse(buff)){
                        UDS_RESPONSE.EXTENDED_DIAG_ACCEPTED -> {
                            mCommand = sendTesterPresent()
                            mLastString = "Extended diagnostic 03 accepted"
                            mTask = mTask.next()
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.CLEAR_DTC_SUCCESSFUL -> {
                            mCommand = buildBLEFrame((UDS_COMMAND.EXTENDED_DIAGNOSTIC.bytes) + byteArrayOf(0x03.toByte()))
                            mLastString = "Entering extended diagnostic 03"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.NEGATIVE_RESPONSE ->{
                            mCommand = sendTesterPresent()
                            mLastString = "Waiting for CLEAR DTC successful"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        else -> {
                            DebugLog.d(TAG,"Received " + buff.toHex())
                            mCommand = clearDTC()
                            mLastString = mTask.toString()
                            return UDSReturn.COMMAND_QUEUED
                        }
                    }

                }
                FLASH_ECU_CAL_SUBTASK.CHECK_PROGRAMMING_PRECONDITION -> {

                    if(checkResponse(buff) == UDS_RESPONSE.ROUTINE_ACCEPTED){
                        //Open extended diagnostic session
                        mCommand = buildBLEFrame(byteArrayOf(0x10.toByte(), 0x02.toByte()))
                        mLastString = "Entering extended diagnostics 02"
                        mTask = mTask.next()
                        return UDSReturn.COMMAND_QUEUED
                    }
                    else {
                        //Check programming precondition, routine 0x0203
                        mCommand = buildBLEFrame(
                            UDS_COMMAND.START_ROUTINE.bytes + UDS_ROUTINE.CHECK_PROGRAMMING_PRECONDITION.bytes

                        )

                        mLastString = mTask.toString()
                        return UDSReturn.COMMAND_QUEUED
                    }
                }
                FLASH_ECU_CAL_SUBTASK.OPEN_EXTENDED_DIAGNOSTIC -> {
                    if(checkResponse(buff) ==  UDS_RESPONSE.NEGATIVE_RESPONSE){
                        mCommand = sendTesterPresent()
                        mLastString = "Waiting for Seed"
                        return UDSReturn.COMMAND_QUEUED
                    }
                    //First response should be 0x50 0x02
                    //
                    if(buff[0] == 0x50.toByte() && buff[1] == 0x02.toByte()){

                        mCommand = buildBLEFrame(byteArrayOf(0x27.toByte(), 0x11.toByte()))
                        mLastString = "Asking for seedkey exhange"
                        mTask = mTask.next()
                        return UDSReturn.COMMAND_QUEUED
                    }

                    else{


                        return UDSReturn.ERROR_UNKNOWN
                    }

                }
                

                FLASH_ECU_CAL_SUBTASK.SA2SEEDKEY -> {
                    //Pass SA2SeedKey unlock_security_access(17)
                    when(checkResponse(buff)){
                        UDS_RESPONSE.SECURITY_ACCESS_GRANTED -> {
                            if(buff[1] == 0x11.toByte()){
                                var challenge = buff.copyOfRange(2,buff.size)

                                var vs = FlashUtilities.Sa2SeedKey(VW_SEEDKEY_TAPE, challenge)
                                var response = vs.execute()

                                mCommand = buildBLEFrame(byteArrayOf(0x27.toByte(), 0x12.toByte()) + response)
                                mLastString = "Passing SeedKey challenege"
                                return UDSReturn.COMMAND_QUEUED
                            }
                            else if(buff[1] == 0x12.toByte()){
                                mLastString = "Passed SeedKey Challenege"
                                mCommand = sendTesterPresent()
                                mTask = mTask.next()
                                return UDSReturn.COMMAND_QUEUED
                            }
                        }
                        else ->{
                            //mCommand = sendTesterPresent()
                            //DebugLog.d(TAG,"Response: " + buff.toHex())
                            return UDSReturn.OK
                        }
                    }

                }
                FLASH_ECU_CAL_SUBTASK.WRITE_WORKSHOP_LOG -> {
                    when(checkResponse(buff)){
                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            //Write workshop tool log
                            //  0x 2E 0xF15A = 0x20, 0x7, 0x17, 0x42,0x04,0x20,0x42,0xB1,0x3D,
                            mCommand = buildBLEFrame(byteArrayOf(0x2E.toByte(),
                                0xF1.toByte(), 0x5A.toByte(), 0x20.toByte(), 0x07.toByte(), 0x17.toByte(),
                                0x42.toByte(),0x04.toByte(),0x20.toByte(),0x42.toByte(),0xB1.toByte(),0x3D.toByte()))
                            mLastString = "Writing workshop code"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.WRITE_IDENTIFIER_ACCEPTED -> {
                            if(buff[1] == 0xF1.toByte() && buff[2] == 0x5A.toByte()) {
                                mLastString = "Wrote workshop code"
                                mCommand = sendTesterPresent()
                                mTask = mTask.next()
                                return UDSReturn.COMMAND_QUEUED
                            }
                        }
                        else -> {
                            return UDSReturn.ERROR_UNKNOWN
                        }
                    }

                }
                FLASH_ECU_CAL_SUBTASK.FLASH_BLOCK -> {
                    when(checkResponse(buff)){
                        //We should enter here from a tester present.
                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            //erase block: 31 01 FF 00 01 05
                            mCommand = buildBLEFrame(UDS_COMMAND.START_ROUTINE.bytes + byteArrayOf(0xFF.toByte(),0x00.toByte(),0x01.toByte(),0x05.toByte()))
                            mLastString = "Erasing CAL block to prepare for flashing"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        //We should have a 71 in response to the erase command we just sent....
                        UDS_RESPONSE.ROUTINE_ACCEPTED -> {
                            //Request download 34 AA 41 05 00 07 FC 00
                            mCommand = buildBLEFrame(byteArrayOf(0x34.toByte(),0xAA.toByte(),0x41.toByte(),0x05.toByte(),0x00.toByte(),0x07.toByte(), 0xFC.toByte(), 0x00.toByte()))
                            mLastString = "Requesting block download"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.DOWNLOAD_ACCEPTED -> {
                            transferSequence = 1
                            progress = round(transferSequence.toFloat() / (bin.size / CAL_BLOCK_TRANSFER_SIZE) * 100)

                            //Send bytes, 0x36 [frame number]
                            //Break the whole bin into frames of FFD size, and
                            // we'll use that array.
                            mCommand = buildBLEFrame(byteArrayOf(0x36.toByte(), transferSequence.toByte()) + bin.copyOfRange(0, CAL_BLOCK_TRANSFER_SIZE))
                            mLastString = "Transfer Started"

                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.TRANSFER_DATA_ACCEPTED -> {
                            val totalFrames: Int = bin.size / CAL_BLOCK_TRANSFER_SIZE

                            if(buff[1] == transferSequence.toByte()){
                                transferSequence++
                                progress = round(transferSequence.toFloat() / (bin.size / CAL_BLOCK_TRANSFER_SIZE) * 100)

                                mLastString = ""
                                //if the current transfer sequence number is larger than the max
                                // number that we need for the payload, send a 'transfer exit'
                                if(transferSequence > totalFrames){
                                    mCommand = buildBLEFrame((byteArrayOf(0x37.toByte())))

                                    return UDSReturn.COMMAND_QUEUED
                                }
                            }

                            //otherwise, we get here
                            // start is frame size + transfer sequence
                            // end is start + frame size *OR* the end of the bin
                            var start = CAL_BLOCK_TRANSFER_SIZE * transferSequence
                            var end = start + CAL_BLOCK_TRANSFER_SIZE
                            if(end > bin.size){
                                end = bin.size
                            }
                            mCommand = buildBLEFrame(byteArrayOf(0x36.toByte(), transferSequence.toByte()) + bin.copyOfRange(start, end))
                            return UDSReturn.COMMAND_QUEUED
                        }

                        UDS_RESPONSE.TRANSFER_EXIT_ACCEPTED -> {
                            progress = 0
                            mCommand = sendTesterPresent()
                            mTask = mTask.next()
                            return UDSReturn.COMMAND_QUEUED
                        }

                        UDS_RESPONSE.NEGATIVE_RESPONSE -> {
                            if(buff[2] == 0x78.toByte()){
                                //just a wait message, return OK
                                return UDSReturn.OK
                            }
                        }

                        else -> {
                            return UDSReturn.ERROR_UNKNOWN
                        }
                    }
                }
                FLASH_ECU_CAL_SUBTASK.CHECKSUM_BLOCK -> {
                    when(checkResponse(buff)){

                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            mCommand = buildBLEFrame(UDS_COMMAND.START_ROUTINE.bytes + byteArrayOf(0x02.toByte(),0x02.toByte(),0x01.toByte(),0x05.toByte(), 0x00.toByte(), 0x04.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()))
                            mLastString = "Checksumming flashed block"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.ROUTINE_ACCEPTED -> {
                            mLastString = "Block Checksummed OK"
                            mCommand = sendTesterPresent()
                            mTask = mTask.next()
                            return UDSReturn.COMMAND_QUEUED
                        }
                        else -> {
                            return UDSReturn.ERROR_UNKNOWN
                        }
                    }

                }
                FLASH_ECU_CAL_SUBTASK.VERIFY_PROGRAMMING_DEPENDENCIES -> {
                    //Verify programming dependencies, routine 0xFF01
                    when(checkResponse(buff)){
                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            mCommand = buildBLEFrame(UDS_COMMAND.START_ROUTINE.bytes + byteArrayOf(0xFF.toByte(), 0x01.toByte()))
                            mLastString = "Verifying Programming Dependencies"
                            return UDSReturn.COMMAND_QUEUED
                        }

                        UDS_RESPONSE.ROUTINE_ACCEPTED -> {
                            mCommand = sendTesterPresent()
                            mTask = mTask.next()
                            return UDSReturn.COMMAND_QUEUED
                        }

                        else -> {
                            return UDSReturn.ERROR_UNKNOWN
                        }

                    }

                }
                FLASH_ECU_CAL_SUBTASK.RESET_ECU -> {
                    when(checkResponse(buff)){
                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            mCommand = buildBLEFrame(UDS_COMMAND.RESET_ECU.bytes)
                            mLastString = "Resetting ECU!!!!"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.ECU_RESET_ACCEPTED -> {
                            mLastString = "Resetting ECU Complete, Please cycle Key"
                            return UDSReturn.OK
                        }
                        else -> {
                            return UDSReturn.ERROR_UNKNOWN
                        }
                    }
                }

                else -> {
                    return UDSReturn.ERROR_UNKNOWN
                }
            }
        }

        DebugLog.d(TAG, "Flash subroutine: " + mTask)
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

    fun sendTesterPresent(): ByteArray{
        return buildBLEFrame(UDS_COMMAND.TESTER_PRESENT.bytes)
    }

    private fun checkResponse(input: ByteArray): UDS_RESPONSE{
        return UDS_RESPONSE.values().find {it.udsByte == input[0]} ?: UDS_RESPONSE.NO_RESPONSE
    }

}
