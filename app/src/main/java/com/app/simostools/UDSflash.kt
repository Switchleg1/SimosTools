package com.app.simostools

import java.io.InputStream
import java.lang.Math.round

object UDSFlasher {
    private val TAG = "UDSflash"
    private var mTask = FLASH_ECU_CAL_SUBTASK.NONE
    private var mCommand: ByteArray = byteArrayOf()
    private var mLastString: String = ""
    private var flashConfirmed: Boolean = false
    private var cancelFlash: Boolean = false
    private var bin: Array<ByteArray> = arrayOf(byteArrayOf(), byteArrayOf(), byteArrayOf(), byteArrayOf(), byteArrayOf(), byteArrayOf())
    private var inputBin: ByteArray = byteArrayOf()
    private var ecuAswVersion: ByteArray = byteArrayOf()
    private var transferSequence = -1
    private var progress = 0
    private var flashEcuBlock = FLASH_ECU_BLOCK.NONE
    private var binAswVersion = COMPATIBLE_BOXCODE_VERSIONS._UNDEFINED
    private var clearDTCStart = 0
    private var clearDTCcontinue = 0
    private var currentBlockOperation = 0

    fun getSubtask(): FLASH_ECU_CAL_SUBTASK{
        return mTask
    }

    fun getFlashConfirmed(): Boolean{
        return flashConfirmed
    }

    fun setFlashConfirmed(input: Boolean = false){
        flashConfirmed = input
    }

    fun cancelFlash(){
        cancelFlash = true
    }

    fun getInfo(): String {
        val response = mLastString
        //mLastString = ""
        return response
    }

    fun getCommand(): ByteArray {
        val response = mCommand
        //mCommand = byteArrayOf()
        return response
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
        flashConfirmed = false
        cancelFlash = false
        progress = 0
        clearDTCStart = 0
        clearDTCcontinue = 0
        inputBin =  input.readBytes()
    }

    fun startTask(ticks: Int): ByteArray {

        if(inputBin.size < 500000){
            mLastString = "Selected file too small..."
            return byteArrayOf()
        }
        else if(inputBin.size > 500000 && inputBin.size < 4000000){
            //Read box code from ECU
            mTask = FLASH_ECU_CAL_SUBTASK.GET_ECU_BOX_CODE
            flashEcuBlock = FLASH_ECU_BLOCK.CAL

            DebugLog.d(TAG, "Initiating Calibration Flash subroutine: " + mTask.toString())
            mLastString = "Initiating calibration flash routines"
            //Reading inputBin into block position 5 of the bin array
            bin[5] = inputBin

            return UDS_COMMAND.READ_IDENTIFIER.bytes + ECUInfo.PART_NUMBER.address
        }
        else{
            //It's a full bin flash....
            mLastString = "Full flash isn't implemented yet, extracting JUST the cal..."
            mTask = FLASH_ECU_CAL_SUBTASK.GET_ECU_BOX_CODE
            flashEcuBlock = FLASH_ECU_BLOCK.CAL
            bin = FlashUtilities.splitBinBlocks(inputBin)
            return UDS_COMMAND.READ_IDENTIFIER.bytes + ECUInfo.PART_NUMBER.address
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

                    //If we can't get a good response from the ECU, we'll
                    // Skip to the force option
                    //if(....){
                    //    mLastString = "NO VALID RESPONSE, FORCE FLASH???\n" +
                    //            "NO INTEGRITY CHECK POSSIBLE!!!"
                    //    mTask = FLASH_ECU_CAL_SUBTASK.CLEAR_DTC
                    //}


                    //If we're in here with a response to our PID request
                    when(checkResponse(buff)){

                        UDS_RESPONSE.READ_IDENTIFIER ->{
                            ecuAswVersion = buff.copyOfRange(3, buff.size)
                            DebugLog.d(TAG, "Received ASW version ${ecuAswVersion.toHex()} from ecu")

                            mLastString = "Read box code from ECU: " + String(ecuAswVersion)
                            mTask = mTask.next()

                            mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                            return UDSReturn.COMMAND_QUEUED
                        }

                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            mCommand = UDS_COMMAND.READ_IDENTIFIER.bytes + ECUInfo.PART_NUMBER.address
                            mLastString = "Initiating flash routines"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        else -> {
                            DebugLog.d(TAG, "Error with ECU Response: " + buff.toHex())
                            mLastString = "Error with ECU Response: " + String(buff)
                            return UDSReturn.ERROR_UNKNOWN
                        }

                    }

                }

                FLASH_ECU_CAL_SUBTASK.CHECK_FILE_COMPAT -> {

                    //val binAswVersion = bin.copyOfRange(0x60, 0x6B)
                    binAswVersion = FlashUtilities.getBoxCodeFromBin(inputBin) ?: COMPATIBLE_BOXCODE_VERSIONS._UNDEFINED

                    //Compare the two strings:
                    if (String(ecuAswVersion).trim() != binAswVersion!!.str) {
                        DebugLog.d(TAG,"ECU software version: ${ecuAswVersion.toHex()}, and file" +
                                " software version: ${binAswVersion.toString()}")
                        mLastString = "Box code on selected BIN file: $binAswVersion" +
                                "\n File mismatch!!!"
                        return UDSReturn.ERROR_RESPONSE
                    }

                    mLastString = mTask.toString() + "\nBox code on selected BIN file: $binAswVersion" +
                            "\nPlease confirm flash procedure"
                    mTask = mTask.next()
                    mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                    return UDSReturn.COMMAND_QUEUED
                }

                FLASH_ECU_CAL_SUBTASK.CONFIRM_PROCEED -> {
                    mLastString = ""
                    if(cancelFlash){
                        mLastString = "Flash has been canceled"
                        bin = arrayOf(byteArrayOf(), byteArrayOf(), byteArrayOf(), byteArrayOf(), byteArrayOf(), byteArrayOf())
                        mTask = FLASH_ECU_CAL_SUBTASK.NONE
                        return UDSReturn.ABORTED
                    }

                    if(!flashConfirmed){

                        return UDSReturn.FLASH_CONFIRM
                    }
                    else{
                        mLastString = "Flash confirmed! Proceeding"
                        mTask = mTask.next()
                        mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                        return UDSReturn.OK
                    }
                }

                FLASH_ECU_CAL_SUBTASK.CHECKSUM_BIN ->{
                    mLastString = ""
                    if(currentBlockOperation == bin.size){
                        currentBlockOperation = 0

                        mTask = mTask.next()
                        mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                        return UDSReturn.COMMAND_QUEUED
                    }

                    if(bin[currentBlockOperation].size != 0){

                        mLastString = mTask.toString() + "\n"
                        mLastString += "Block identifier: $currentBlockOperation" + "\n"

                        var checksummed = FlashUtilities.checksumSimos18(bin[currentBlockOperation],
                            binAswVersion.software.baseAddresses[currentBlockOperation],
                            binAswVersion.software.checksumLocations[currentBlockOperation]

                            )
                        mLastString += "Original checksum: " + checksummed.fileChecksum + "\n"
                        mLastString += "Calculatated checksum: " + checksummed.calculatedChecksum + "\n"
                        if (checksummed.updated) mLastString += "    Checksum corrected\n"
                        else mLastString += "    Checksum not updated\n"

                        checksummed = FlashUtilities.checksumECM3(checksummed.bin)
                        mLastString += "Original ECM3: " + checksummed.fileChecksum + "\n"
                        mLastString += "    Calculated ECM3: " + checksummed.calculatedChecksum + "\n"
                        if (checksummed.updated) mLastString += "Checksum corrected\n"
                        else mLastString += "    Checksum not updated\n"

                        bin[currentBlockOperation] = checksummed.bin

                    }

                    currentBlockOperation++

                    mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                    return UDSReturn.COMMAND_QUEUED
                }

                FLASH_ECU_CAL_SUBTASK.COMPRESS_BIN ->{
                    mLastString = ""
                    if(currentBlockOperation == bin.size){
                        currentBlockOperation = 0

                        mTask = mTask.next()
                        mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                        return UDSReturn.COMMAND_QUEUED
                    }

                    if(bin[currentBlockOperation].size != 0) {
                        mLastString = mTask.toString() + "\n"

                        var uncompressedSize = bin[currentBlockOperation].size
                        bin[currentBlockOperation] = FlashUtilities.encodeLZSS(bin[currentBlockOperation])

                        var compressedSize = bin[currentBlockOperation].size

                        mLastString += "Uncompressed bin size: $uncompressedSize\n"
                        mLastString += "Compressed bin size: $compressedSize"
                    }

                    currentBlockOperation++

                    mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                    return UDSReturn.COMMAND_QUEUED

                }

                FLASH_ECU_CAL_SUBTASK.ENCRYPT_BIN -> {
                    mLastString = ""
                    if(currentBlockOperation == bin.size){
                        currentBlockOperation = 0

                        mTask = mTask.next()
                        mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                        return UDSReturn.COMMAND_QUEUED
                    }

                    if(bin[currentBlockOperation].size != 0) {
                        mLastString = mTask.toString() + "\n"
                        var unencryptedSize = bin[currentBlockOperation].size

                        bin[currentBlockOperation] = FlashUtilities.encrypt(bin[currentBlockOperation], SIMOS18_AES_KEY, SIMOS18_AES_IV)

                        var encryptedSize = bin[currentBlockOperation].size

                        mLastString += "Unencrypted bin size: $unencryptedSize \n"
                        mLastString += "Encrypted bin size: $encryptedSize \n"

                        if (bin.isEmpty()) {
                            mLastString = "Error encrypting BIN"
                            return UDSReturn.ERROR_UNKNOWN
                        }

                    }

                    currentBlockOperation++
                    mCommand = UDS_COMMAND.TESTER_PRESENT.bytes

                    return UDSReturn.COMMAND_QUEUED
                }

                FLASH_ECU_CAL_SUBTASK.CLEAR_DTC -> {
                    //We should enter this function after a 3e response
                    when(checkResponse(buff)){
                        UDS_RESPONSE.EXTENDED_DIAG_ACCEPTED -> {
                            mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                            mLastString = "Extended diagnostic 03 accepted"
                            mTask = mTask.next()
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.CLEAR_DTC_SUCCESSFUL -> {
                            mCommand = (UDS_COMMAND.EXTENDED_DIAGNOSTIC.bytes) + byteArrayOf(0x03.toByte())
                            mLastString = "Entering extended diagnostic 03"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.NEGATIVE_RESPONSE ->{
                            mCommand = byteArrayOf()
                            mLastString = "Waiting for CLEAR DTC successful"
                            //Move the ticks counter out of the way since it should actually be in ASW
                            clearDTCcontinue = ticks + 15
                            return UDSReturn.OK
                        }
                        UDS_RESPONSE.POSITIVE_RESPONSE ->{
                            //There's a chance we're stuck in CBOOT... if that's the case
                            // when we try to clear DTCs it'll give us a positive response, but
                            // will never actually succeed
                            if(clearDTCStart == 0){
                                mLastString = "Attempting to clear DTC"
                                clearDTCStart = ticks
                                clearDTCcontinue = ticks + 15
                            }
                            else{
                                mLastString = ""
                            }
                            if(ticks > clearDTCcontinue){
                                mCommand = (UDS_COMMAND.EXTENDED_DIAGNOSTIC.bytes) + byteArrayOf(0x03.toByte())
                                mLastString = "Entering extended diagnostic 03"
                                return UDSReturn.COMMAND_QUEUED
                            }
                            else{
                                DebugLog.d(TAG,"Received " + buff.toHex() + "for $ticks")
                                mLastString = ""

                                return UDSReturn.CLEAR_DTC_REQUEST
                            }
                        }
                        else -> {
                            mCommand = byteArrayOf()
                            return UDSReturn.OK
                        }
                    }

                }

                FLASH_ECU_CAL_SUBTASK.CHECK_PROGRAMMING_PRECONDITION -> {

                    when(checkResponse(buff)) {
                        UDS_RESPONSE.ROUTINE_ACCEPTED -> {
                            //Open extended diagnostic session
                            mCommand = UDS_COMMAND.EXTENDED_DIAGNOSTIC.bytes + byteArrayOf(0x02.toByte())
                            mLastString = "Entering extended diagnostics 02"
                            mTask = mTask.next()
                            return UDSReturn.COMMAND_QUEUED
                        }

                        else -> {
                            //Check programming precondition, routine 0x0203
                            mCommand = UDS_COMMAND.START_ROUTINE.bytes + UDS_ROUTINE.CHECK_PROGRAMMING_PRECONDITION.bytes
                            mLastString = mTask.toString()
                            return UDSReturn.COMMAND_QUEUED
                        }
                    }

                }

                FLASH_ECU_CAL_SUBTASK.OPEN_EXTENDED_DIAGNOSTIC -> {
                    when(checkResponse(buff)) {

                        UDS_RESPONSE.NEGATIVE_RESPONSE -> {
                            //mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                            mLastString = "Waiting for Seed"
                            //return UDSReturn.COMMAND_QUEUED
                            return UDSReturn.OK
                        }

                        UDS_RESPONSE.EXTENDED_DIAG_ACCEPTED -> {
                            if(buff[1] == 0x02.toByte()){
                                mCommand = UDS_COMMAND.SECURITY_ACCESS.bytes + byteArrayOf(0x11.toByte())
                                mLastString = "Asking for seedkey exhange"
                                mTask = mTask.next()
                                return UDSReturn.COMMAND_QUEUED
                            }
                        }

                        else -> {
                            return UDSReturn.ERROR_UNKNOWN
                        }

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

                                mCommand = UDS_COMMAND.SECURITY_ACCESS.bytes + byteArrayOf(0x12.toByte()) + response
                                mLastString = "Passing SeedKey challenege"
                                return UDSReturn.COMMAND_QUEUED
                            }
                            else if(buff[1] == 0x12.toByte()){
                                mLastString = "Passed SeedKey Challenege"
                                mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                                mTask = mTask.next()
                                return UDSReturn.COMMAND_QUEUED
                            }
                        }
                        else ->{
                            mLastString = ""
                            return UDSReturn.OK
                        }
                    }

                }
                FLASH_ECU_CAL_SUBTASK.WRITE_WORKSHOP_LOG -> {
                    when(checkResponse(buff)){
                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            //Write workshop tool log
                            //  0x 2E 0xF15A = 0x20, 0x7, 0x17, 0x42,0x04,0x20,0x42,0xB1,0x3D,
                            mCommand = byteArrayOf(0x2E.toByte(),
                                0xF1.toByte(), 0x5A.toByte(), 0x20.toByte(), 0x07.toByte(), 0x17.toByte(),
                                0x42.toByte(),0x04.toByte(),0x20.toByte(),0x42.toByte(),0xB1.toByte(),0x3D.toByte())
                            mLastString = "Writing workshop code"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.WRITE_IDENTIFIER_ACCEPTED -> {
                            if(buff[1] == 0xF1.toByte() && buff[2] == 0x5A.toByte()) {
                                mLastString = "Wrote workshop code"
                                mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
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
                    mLastString = ""
                    //If we're done flashing all the blocks, pass off into the
                    //  Reset ecu stage
                    if(currentBlockOperation == bin.size){
                        currentBlockOperation = 0

                        mTask = FLASH_ECU_CAL_SUBTASK.RESET_ECU
                        mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                        return UDSReturn.COMMAND_QUEUED
                    }

                    if(bin[currentBlockOperation].size == 0){
                        currentBlockOperation++
                        mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                        return UDSReturn.COMMAND_QUEUED
                    }

                    when(checkResponse(buff)){
                        //We should enter here from a tester present.
                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            //erase block: 31 01 FF 00 01 BLOCKID
                            mCommand = UDS_COMMAND.START_ROUTINE.bytes +
                                    UDS_ROUTINE.ERASE_BLOCK.bytes +
                                    0x01.toByte() +
                                    binAswVersion.software.blockNumberMap[currentBlockOperation].toByte()

                            DebugLog.d(TAG, "Executing ERASE block command: ${mCommand.toHex()}")
                            mLastString = "Erasing CAL block to prepare for flashing"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        //We should have a 71 in response to the erase command we just sent....
                        UDS_RESPONSE.ROUTINE_ACCEPTED -> {
                            //Request download 34 AA 41 05 00 07 FC 00

                            mCommand = UDS_COMMAND.REQUEST_DOWNLOAD.bytes +
                                    UDS_DOWNLOAD_PROPERTIES.ENCRYPTED_COMPRESSED.bytes +
                                    UDS_DOWNLOAD_PROPERTIES.FOUR_ONE_ADDRESS_LENGTH.bytes +
                                    binAswVersion.software.blockNumberMap[currentBlockOperation].toByte() +
                                    FlashUtilities.intToByteArray(binAswVersion.software.blockLengths[binAswVersion.software.blockNumberMap[currentBlockOperation]])

                            DebugLog.d(TAG, "Executing Request download command: ${mCommand.toHex()}")
                            mLastString = "Requesting block download"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.DOWNLOAD_ACCEPTED -> {
                            transferSequence = 1
                            progress = round(transferSequence.toFloat() / (bin[currentBlockOperation].size / CAL_BLOCK_TRANSFER_SIZE) * 100)

                            //Send bytes, 0x36 [frame number]
                            //Break the whole bin into frames of FFD size, and
                            // we'll use that array.
                            mCommand = UDS_COMMAND.TRANSFER_DATA.bytes +  byteArrayOf(transferSequence.toByte()) + bin[currentBlockOperation].copyOfRange(0, CAL_BLOCK_TRANSFER_SIZE)
                            mLastString = "Transfer Started"

                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.TRANSFER_DATA_ACCEPTED -> {
                            val totalFrames: Int = bin[currentBlockOperation].size / CAL_BLOCK_TRANSFER_SIZE


                            //If the last frame we sent was acked, increment the transfer counter
                            // set the progress bar.  Check to see if we're at the total number
                            // of frames that we should be (and if we are, request an exit from
                            // the transfer
                            if(buff[1] == transferSequence.toByte()){
                                transferSequence++
                                progress = round(transferSequence.toFloat() / (bin[currentBlockOperation].size / CAL_BLOCK_TRANSFER_SIZE) * 100)

                                mLastString = ""
                                //if the current transfer sequence number is larger than the max
                                // number that we need for the payload, send a 'transfer exit'
                                if(transferSequence > totalFrames + 1){
                                    mCommand = UDS_COMMAND.TRANSFER_EXIT.bytes

                                    return UDSReturn.COMMAND_QUEUED
                                }
                            }

                            //otherwise, we get here
                            // start is frame size + transfer sequence
                            // end is start + frame size *OR* the end of the bin
                            var start = CAL_BLOCK_TRANSFER_SIZE * (transferSequence - 1)
                            var end = start + CAL_BLOCK_TRANSFER_SIZE
                            if(end > bin[currentBlockOperation].size) end = bin[currentBlockOperation].size

                            mCommand = UDS_COMMAND.TRANSFER_DATA.bytes + byteArrayOf(transferSequence.toByte()) + bin[currentBlockOperation].copyOfRange(start, end)
                            return UDSReturn.COMMAND_QUEUED
                        }

                        UDS_RESPONSE.TRANSFER_EXIT_ACCEPTED -> {
                            progress = 0
                            mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                            mTask = mTask.next()
                            mLastString = "Transfer Done"
                            return UDSReturn.COMMAND_QUEUED
                        }

                        UDS_RESPONSE.NEGATIVE_RESPONSE -> {
                            if(buff[2] == 0x78.toByte()){
                                mLastString = ""
                                //just a wait message, return OK
                                return UDSReturn.OK
                            }
                        }

                        else -> {
                            mLastString = buff.toHex()
                            return UDSReturn.ERROR_UNKNOWN
                        }
                    }
                }
                FLASH_ECU_CAL_SUBTASK.CHECKSUM_BLOCK -> {
                    when(checkResponse(buff)){

                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            mCommand = UDS_COMMAND.START_ROUTINE.bytes +
                                    UDS_ROUTINE.CHECKSUM_BLOCK.bytes +
                                    0x01.toByte() +
                                    binAswVersion.software.blockNumberMap[currentBlockOperation].toByte() +
                                    0x00.toByte() + 0x04.toByte() + 0x00.toByte() + 0x00.toByte() + 0x00.toByte() + 0x00.toByte()
                            mLastString = "Checksumming flashed block"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.ROUTINE_ACCEPTED -> {
                            mLastString = "Block Checksummed OK"
                            mCommand = UDS_COMMAND.TESTER_PRESENT.bytes

                            currentBlockOperation++
                            mTask = FLASH_ECU_CAL_SUBTASK.FLASH_BLOCK
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.NEGATIVE_RESPONSE -> {
                            if(buff[2] == 0x78.toByte()){
                                mLastString = ""
                                //just a wait message, return OK
                                return UDSReturn.OK
                            }
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
                            mCommand = UDS_COMMAND.START_ROUTINE.bytes + byteArrayOf(0xFF.toByte(), 0x01.toByte())
                            mLastString = "Verifying Programming Dependencies"
                            return UDSReturn.COMMAND_QUEUED
                        }

                        UDS_RESPONSE.ROUTINE_ACCEPTED -> {
                            mCommand = UDS_COMMAND.TESTER_PRESENT.bytes
                            mTask = mTask.next()
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.NEGATIVE_RESPONSE -> {
                            if(buff[2] == 0x78.toByte()){
                                mLastString = ""
                                //just a wait message, return OK
                                return UDSReturn.OK
                            }
                        }

                        else -> {
                            return UDSReturn.ERROR_UNKNOWN
                        }

                    }

                }
                FLASH_ECU_CAL_SUBTASK.RESET_ECU -> {
                    DebugLog.d(TAG,"Response during reset ecu request: " + buff.toHex())
                    when(checkResponse(buff)){
                        UDS_RESPONSE.POSITIVE_RESPONSE -> {
                            mCommand = UDS_COMMAND.RESET_ECU.bytes
                            mLastString = "Resetting ECU!!!!"
                            return UDSReturn.COMMAND_QUEUED
                        }
                        UDS_RESPONSE.ECU_RESET_ACCEPTED -> {
                            mLastString = "Resetting ECU Complete, Please cycle Key"
                            bin = arrayOf(byteArrayOf(), byteArrayOf(), byteArrayOf(),
                                byteArrayOf(), byteArrayOf(), byteArrayOf())
                            mTask = FLASH_ECU_CAL_SUBTASK.NONE
                            return UDSReturn.FLASH_COMPLETE
                        }
                        UDS_RESPONSE.NEGATIVE_RESPONSE -> {
                            if (buff[2] == 0x78.toByte()) {
                                mLastString = ""
                                //just a wait message, return OK
                                return UDSReturn.OK
                            }
                            else {
                                return UDSReturn.ERROR_UNKNOWN
                            }
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





    private fun checkResponse(input: ByteArray): UDS_RESPONSE{
        return UDS_RESPONSE.values().find {it.udsByte == input[0]} ?: UDS_RESPONSE.NO_RESPONSE
    }

}
