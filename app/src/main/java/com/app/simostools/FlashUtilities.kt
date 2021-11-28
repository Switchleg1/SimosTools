package com.app.simostools

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayDeque

data class checksummedBin(var bin: ByteArray, var fileChecksum: String, var calculatedChecksum: String, var updated: Boolean)



object FlashUtilities {
    val TAG = "FlashUtilities"

    fun splitBinBlocks(bin: ByteArray): Array<ByteArray>{
        val boxCode = getBoxCodeFromBin(bin)
        DebugLog.d(TAG, boxCode.toString())

        var splitBlocks: Array<ByteArray> = arrayOf(byteArrayOf(),byteArrayOf(),byteArrayOf(),byteArrayOf(),byteArrayOf(),byteArrayOf())

        for(i in 1..5){
            splitBlocks[i] = bin.copyOfRange(boxCode!!.software.fullBinLocations[i], boxCode.software.fullBinLocations[i] + boxCode.software.blockLengths[i])
        }

        return splitBlocks
    }

    fun getBoxCodeFromBin(bin: ByteArray): COMPATIBLE_BOXCODE_VERSIONS?{
        DebugLog.d(TAG, "getBoxCodeFromBin")
        if(bin.size >= 4000000){
            //This is a full flash file, so the box code location needs to be checked
                //based on the offset of the CAL in a full bin..
            enumValues<COMPATIBLE_BOXCODE_VERSIONS>().forEach {
                val boxFromBin = String(
                    bin.copyOfRange(
                        it.boxCodeLocation[0] + it.software.fullBinLocations[5],
                        it.boxCodeLocation[1] + it.software.fullBinLocations[5]
                    )
                ).trim()

                if (it.str == boxFromBin)
                {
                    return it
                }
            }
        }
        else {
            //This is only a CAL file, so we'll check with boxCodeLocation directly
            enumValues<COMPATIBLE_BOXCODE_VERSIONS>().forEach {
                val boxFromBin = String(
                    bin.copyOfRange(
                        it.boxCodeLocation[0],
                        it.boxCodeLocation[1]
                    )
                ).trim()
                if (it.str == boxFromBin )
                {
                    DebugLog.d(TAG,"Matched BIN to compatible box code: $boxFromBin")
                    return it
                }
            }

        }
        return null
    }

    fun checksumSimos18(bin: ByteArray, baseAddress: UInt, checksumLocation: Int): checksummedBin{
        DebugLog.d(TAG, "Checksumming block, base address $baseAddress, and checksum location: $checksumLocation")

        var currentChecksum = bin.copyOfRange(checksumLocation, checksumLocation + 8)
        DebugLog.d(TAG,"Current Checksum: " + currentChecksum.toHex())
        var offset = baseAddress
        var startAddress1 = byteArrayToInt(bin.copyOfRange(checksumLocation + 12, checksumLocation + 16).reversedArray()).toUInt() - offset
        var endAddress1 = byteArrayToInt(bin.copyOfRange(checksumLocation + 16, checksumLocation + 20).reversedArray()).toUInt() - offset
        var startAddress2 = byteArrayToInt(bin.copyOfRange(checksumLocation + 20, checksumLocation + 24).reversedArray()).toUInt() - offset
        var endAddress2 = byteArrayToInt(bin.copyOfRange(checksumLocation + 24, checksumLocation + 28).reversedArray()).toUInt() - offset

        var checksumData: ByteArray = byteArrayOf()

        checksumData = bin.copyOfRange(startAddress1.toInt(), endAddress1.toInt() + 1)

        if(endAddress2 - startAddress2 > 0.toUInt()){
            checksumData += bin.copyOfRange(startAddress2.toInt(), endAddress2.toInt() + 1)
        }

        var polynomial = 0x4c11db7;
        var crc = 0x00000000;

        for (c in checksumData)
        {
            for (j in 7 downTo 0)
            {
                var z32: Byte = (crc ushr 31).toByte()
                crc = crc shl 1;
                var test = ((c.toUInt() shr j) and 1.toUInt()) xor z32.toUInt()
                if (test.toInt() > 0) {
                    crc = crc xor polynomial;
                }

                crc = crc and 0xffffffff.toInt();

            }
        }

        var checksumCalculated = byteArrayOf(0x0.toByte(), 0x0.toByte(), 0x0.toByte(), 0x0.toByte()) + intToByteArray(crc).reversedArray()
        println("  Current checksum:      " + currentChecksum.toHex())
        println("  Calculated checksum:   " + checksumCalculated.toHex())


        if(currentChecksum contentEquals checksumCalculated){
            println("  Checksum matches!")
        }
        else{
            println("  Checksum doesn't match!")
        }

        for(i in 0..checksumCalculated.size - 1){
            bin[checksumLocation + i] = checksumCalculated[i]
        }


        return(checksummedBin(bin,currentChecksum.toHex(), checksumCalculated.toHex(), true))
    }

    fun checksumECM3(bin: ByteArray, range: IntArray): checksummedBin{
        var startAddress = range[0]
        var endAddress = range[1]

        var checksumLocation = 0x400;
        var checksumCurrent = bin.copyOfRange(checksumLocation, checksumLocation + 8)

        //Starting Value
        var checksum = byteArrayToInt(bin.copyOfRange(checksumLocation + 8, checksumLocation + 12).reversedArray()).toULong() shl 32;
        checksum += byteArrayToInt(bin.copyOfRange(checksumLocation + 12, checksumLocation + 16).reversedArray()).toUInt()

        for(i in (startAddress)..(endAddress - 1) step 4){
            checksum += byteArrayToInt(bin.copyOfRange(i, i+4).reversedArray()).toUInt()
        }

        var checksumCalculated = intToByteArray((checksum shr 32).toInt()).reversedArray() + intToByteArray((checksum.toInt() and 0xFFFFFFFF.toInt())).reversedArray()
        println("  Current ECM3:      " + checksumCurrent.toHex())
        println("  Calculated ECM3:   " + checksumCalculated.toHex())

        if(checksumCurrent contentEquals checksumCalculated){
            println("  ECM3 checksum matches!")
        }
        else{
            println("  ECM3 checksum doesn't match!")
        }

        for(i in 0..checksumCalculated.size - 1){
            bin[0x400 + i] = checksumCalculated[i]
        }

        return(checksummedBin(bin,checksumCurrent.toHex(), checksumCalculated.toHex(), true))
    }

    fun encodeLZSS_orig(input: ByteArray, maxSlidingWindowSize: Int = 1023, debug: Boolean = false): ByteArray {
        var flags = 0
        var flagPos = 0x80

        var searchBuffer: ByteArray = byteArrayOf()
        var checkCharacters: ByteArray = byteArrayOf()
        var outputBuffer: ByteArray = byteArrayOf()
        var output: ByteArray = byteArrayOf()
        var searchStart: Int = 0
        var index: Int = -1

        var i = 0

        fun copyCheckCharacters(){
            for(j in 0..checkCharacters.size - 1){
                if(debug) println("    -> Adding " + "%02x".format(checkCharacters[j]) + " to outputBuffer")
                outputBuffer += byteArrayOf(checkCharacters[j])
                flagPos = flagPos shr 1

                if(flagPos == 0x00){

                    output += byteArrayOf(flags.toByte()) + outputBuffer

                    flags = 0
                    flagPos = 0x80
                    outputBuffer = byteArrayOf()

                    //println("  output: " + output.toHex())
                }
            }
        }

        while(i <= input.size - 1){
            if(i > maxSlidingWindowSize) searchStart = i - maxSlidingWindowSize + 1
            else searchStart = 0

            if(i > 0) searchBuffer = input.copyOfRange(searchStart, i)
            else searchBuffer = byteArrayOf()

            //Add the current byte to the check buffer
            checkCharacters += input[i]

            //find out whether the searchBuffer contains our character bytes
            index = searchBuffer.findFirst(checkCharacters) //The index where the bytes appear in the search buffer


            //if the searchBuffer does contain this byte OR we're at the end of the file...
            if(index == -1 || i == input.size - 1 || checkCharacters.size > 63){

                //If our checkCharacters array is larger than our desired minimum size...
                if(checkCharacters.size > 2 && i < input.size - 1){

                    index = searchBuffer.findFirst(checkCharacters.copyOfRange(0, checkCharacters.size - 1))
                    var length = checkCharacters.size - 1//Set the length of the token

                    //var offset = searchBuffer.findLast(checkCharacters.copyOfRange(0, checkCharacters.size - 1)) - length
                    var offset = ((searchBuffer.size - length - index))//Calculate the relative offset

                    if(offset == 0){
                        copyCheckCharacters()
                        i++
                        checkCharacters = byteArrayOf()
                    }

                    else{

                        outputBuffer += byteArrayOf( ((offset shr 8) or (length shl 2)).toByte(), (offset and 0xFF.toInt()).toByte() )

                        flags = flags or flagPos
                        flagPos = flagPos shr 1

                        if(flagPos == 0x00 ){
                            output += byteArrayOf(flags.toByte()) + outputBuffer

                            flags = 0
                            flagPos = 0x80
                            outputBuffer = byteArrayOf()
                        }

                        checkCharacters = byteArrayOf(checkCharacters[checkCharacters.size - 1])
                        i++
                    }

                }

                else{
                    copyCheckCharacters()
                    i++
                    checkCharacters = byteArrayOf()
                }
            }
            else i++
        }

        if(outputBuffer.size != 0) output += byteArrayOf(flags.toByte()) + outputBuffer

        while(output.size % 0x10 != 0){
            output += byteArrayOf(0x0.toByte())
        }

        return(output)
    }

    fun encodeLZSS(input: ByteArray, maxSlidingWindowSize: Int = 1023, debug: Boolean = false): ByteArray {
        var flags = 0
        var flagPos = 0x80

        var searchBuffer: ByteArray = byteArrayOf()
        var checkCharacters: ByteArray = byteArrayOf()
        var outputBuffer: ByteArray = byteArrayOf()
        var output: ByteArray = byteArrayOf()
        var index: Int
        var inputIterator = input.iterator()

        var i = 0

        fun stepFlag(){
            flagPos = flagPos shr 1
            if(flagPos == 0x00 ){
                output += byteArrayOf(flags.toByte()) + outputBuffer

                flags = 0
                flagPos = 0x80
                outputBuffer = byteArrayOf()
            }
        }

        var nextByte = inputIterator.next()
        checkCharacters += nextByte
        i++

        while(inputIterator.hasNext()){
            if(debug) println()
            while(searchBuffer.size > maxSlidingWindowSize){
                searchBuffer = searchBuffer.copyOfRange(1, searchBuffer.size)
            }

            index = searchBuffer.findFirst(checkCharacters) //The index where the bytes appear in the search buffer

            //if(debug) println("search buffer: " + searchBuffer.toHex())
            if(debug) println("CheckCharacters: " + checkCharacters.toHex())
            //if(debug) println("Output: " + output.toHex())
            if(debug) println("index: $index")

            if(index == -1 || index > searchBuffer.size - 3 || checkCharacters.size > 63 || i >= input.size - 1){
                index = searchBuffer.findFirst(checkCharacters.copyOfRange(0, checkCharacters.size - 1))
                var length = checkCharacters.size - 1
                var offset = ((searchBuffer.size - index))

                if(length == 0){
                    outputBuffer += checkCharacters[0]

                    stepFlag()


                    searchBuffer += checkCharacters[0]
                    nextByte = inputIterator.next()
                    i++

                    checkCharacters = byteArrayOf(nextByte)

                }

                else if(length <= 2){
                    if(debug) println("    -> Adding byte to outputBuffer: " + "%02x".format(checkCharacters[0]))

                    outputBuffer += checkCharacters[0]
                    stepFlag()

                    searchBuffer += checkCharacters[0]
                    checkCharacters = checkCharacters.copyOfRange(1, checkCharacters.size)

                }

                else{
                    if(debug) println("    Creating tag length $length offset $offset index $index searchbuffersize " + searchBuffer.size)


                    outputBuffer += byteArrayOf( ((offset shr 8) or (length shl 2)).toByte(), (offset and 0xFF.toInt()).toByte() )
                    flags = flags or flagPos
                    stepFlag()

                    searchBuffer += checkCharacters.copyOfRange(0, checkCharacters.size - 1)
                    checkCharacters = byteArrayOf(checkCharacters[checkCharacters.size - 1])
                }
            }
            else{

                nextByte = inputIterator.next()
                i++
                checkCharacters += nextByte
            }

        }

        outputBuffer += checkCharacters
        stepFlag()

        while(outputBuffer.size != 0) {
            stepFlag()

        }

        while(output.size % 0x10 != 0){
            output += byteArrayOf(0x0.toByte())
        }

        return(output)
    }



    fun decodeLZSS(input: ByteArray, expectedSize: Int): ByteArray{

        var output: ByteArray = byteArrayOf()
        var dataIterator = input.iterator()

        fun bits(flagByte: UByte): IntArray{
            return intArrayOf(
                (flagByte.toInt() shr 7) and 1,
                (flagByte.toInt() shr 6) and 1,
                (flagByte.toInt() shr 5) and 1,
                (flagByte.toInt() shr 4) and 1,
                (flagByte.toInt() shr 3) and 1,
                (flagByte.toInt() shr 2) and 1,
                (flagByte.toInt() shr 1) and 1,
                (flagByte.toInt() and 1)
            )
        }

        fun readByte(): Byte{

            return dataIterator.nextByte()
        }

        fun readShort(): UInt{

            var a = dataIterator.nextByte().toUByte()

            var b = dataIterator.nextByte().toUByte()
            return ((a.toUInt() shl 8) or b.toUInt())
        }

        fun copyByte(){

            output += dataIterator.nextByte()
        }

        while(output.size < expectedSize){

            var flagByte = readByte().toUByte()
            var flags = bits(flagByte)

            for(flag in flags){

                if(flag == 0){
                    copyByte()
                }
                else if(flag == 1){
                    var sh = readShort()

                    var count = sh shr 10
                    var disp = sh and 0x3FF.toUInt()


                    for(i in 1..count.toInt()){
                        output += output[output.size - disp.toInt()]
                    }

                }
                else{
                    println("holy shit issues")
                    return output
                }

                if(expectedSize <= output.size)
                    return output

            }
        }

        return output

    }

    public class Sa2SeedKey(inputTape: ByteArray, seed: ByteArray) {
        var instructionPointer = 0
        var instructionTape = inputTape
        var register = seed.getUIntAt(0)
        var carry_flag: UInt = 0.toUInt()
        var for_pointers: ArrayDeque<Int> = ArrayDeque()
        var for_iterations: ArrayDeque<Int> = ArrayDeque()

        fun rsl(){
            println("rsl")
            carry_flag = register and 0x80000000.toUInt()
            register = register shl 1
            if(carry_flag != 0.toUInt())
                register = register or 0x1.toUInt()

            register = register and 0xFFFFFFFF.toUInt()
            instructionPointer += 1
        }

        fun rsr(){
            println("rsr")
            carry_flag = register and 0x1.toUInt()
            register = register shr 1

            if(carry_flag != 0.toUInt())
                register = register or 0x80000000.toUInt()

            instructionPointer += 1

        }

        fun add(){
            println("add")
            carry_flag = 0.toUInt()
            var operands = instructionTape.copyOfRange(instructionPointer + 1, instructionPointer + 5)

            var output_register = register + operands.getUIntAt(0)

            if (output_register > 0xffffffff.toUInt()){
                carry_flag = 1.toUInt()
                output_register = output_register and 0xffffffff.toUInt()
            }

            register = output_register

            instructionPointer += 5

        }

        fun sub(){
            println("sub")
            carry_flag = 0.toUInt()
            var operands = instructionTape.copyOfRange(instructionPointer + 1, instructionPointer + 5)
            var output_register = register - operands.getUIntAt(0)

            if (output_register < 0.toUInt()){
                carry_flag = 1.toUInt()
                output_register = output_register and 0xffffffff.toUInt()
            }

            register = output_register
            instructionPointer += 5
        }

        fun eor(){
            var operands = instructionTape.copyOfRange(instructionPointer + 1,instructionPointer + 5)
            register = register xor operands.getUIntAt(0)
            instructionPointer += 5
        }

        fun for_loop(){

            var operands = instructionTape.copyOfRange(instructionPointer + 1,instructionPointer + 2)
            for_iterations.addFirst(operands[0] - 1)
            instructionPointer += 2
            for_pointers.addFirst(instructionPointer)
        }

        fun next_loop(){

            if(for_iterations[0] > 0){
                for_iterations[0] -= 1
                instructionPointer = for_pointers[0]
            }
            else{
                for_iterations.removeFirst()
                for_pointers.removeFirst()
                instructionPointer += 1
            }

        }

        fun bcc(){

            var operands = instructionTape.copyOfRange(instructionPointer + 1,instructionPointer + 2)
            var skip_count = operands[0].toUByte().toInt() + 2
            if(carry_flag == 0.toUInt()){
                instructionPointer += skip_count
            }
            else{
                instructionPointer += 2
            }

        }

        fun bra(){
            var operands = instructionTape.copyOfRange(instructionPointer + 1,instructionPointer + 2)
            var skip_count = operands[0].toUByte().toInt() + 2
            instructionPointer += skip_count

        }

        fun finish(){
            instructionPointer += 1
        }

        fun execute(): ByteArray{
            val instructionSet = mapOf(
                0x81.toByte() to ::rsl,
                0x82.toByte() to ::rsr,
                0x93.toByte() to ::add,
                0x84.toByte() to ::sub,
                0x87.toByte() to ::eor,
                0x68.toByte() to ::for_loop,
                0x49.toByte() to ::next_loop,
                0x4A.toByte() to ::bcc,
                0x6B.toByte() to ::bra,
                0x4C.toByte() to ::finish,
            )


            while(instructionPointer < instructionTape.size){

                instructionSet[instructionTape[instructionPointer]]?.invoke()
            }

            return UIntToByteArray(register)

        }
    }



    fun ByteArray.findFirst(inner: ByteArray): Int{
        if(inner.isEmpty()) return -1
        if(size == 0) return -1

        for(i in 0..(size - 1)){
            if(this[i] == inner[0]){
                var j = 0
                while(j < inner.size ){
                    if(i + j == size) return -1
                    if(this[i + j] == inner[j]){
                        if(j == inner.size - 1) return i
                        j++
                    }
                    else{
                        j = inner.size
                    }
                }
            }
        }

        return -1
    }


    fun encrypt(bin: ByteArray, key: ByteArray, initVector: ByteArray ): ByteArray {
        try {
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            val iv = IvParameterSpec(initVector)

            val skeySpec = SecretKeySpec(key, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)

            return cipher.doFinal(bin)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return byteArrayOf()
    }

    fun buildWorkshopCode(bin: Array<ByteArray>, binAswVersion: COMPATIBLE_BOXCODE_VERSIONS): ByteArray {
        val calID = bin[5].copyOfRange(0x7A,0x7E)
        DebugLog.d(TAG, bin[5].copyOfRange(0,0x80).toHex())
        DebugLog.d(TAG,"Writing workshop code: " + calID.toHex())


        var workshopCode = byteArrayOf(
            convertToBCD(Calendar.getInstance().get(Calendar.YEAR) - 2000),
            convertToBCD(Calendar.getInstance().get(Calendar.MONTH) + 1),
            convertToBCD(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)),
            0x42.toByte()) +
            calID

        workshopCode += crc8Hash(workshopCode)

        return workshopCode
    }

    private fun byteArrayToInt(data: ByteArray): Int {
        return (data[3].toUByte().toInt() shl 0) or
                (data[2].toUByte().toInt() shl 8) or
                (data[1].toUByte().toInt() shl 16) or
                (data[0].toUByte().toInt() shl 24)
    }

    fun intToByteArray(data: Int): ByteArray {
        return byteArrayOf(
            (data shr 24).toByte(),
            (data shr 16).toByte(),
            (data shr 8).toByte(),
            (data shr 0).toByte()
        )
    }

    fun UIntToByteArray(data: UInt): ByteArray {
        return byteArrayOf(
            (data shr 24).toByte(),
            (data shr 16).toByte(),
            (data shr 8).toByte(),
            (data shr 0).toByte()
        )
    }


}


enum class UDS_RESPONSE(val str: String, val udsByte: Byte?) {
    POSITIVE_RESPONSE("Tester Present", 0x7E.toByte()),
    NEGATIVE_RESPONSE("Negative Response", 0x7f.toByte()),
    NO_RESPONSE("Positive Response", null),
    ROUTINE_ACCEPTED("Remote activation of routine, accepted", 0x71.toByte()),
    DOWNLOAD_ACCEPTED("Request download accepted", 0x74.toByte()),
    TRANSFER_DATA_ACCEPTED("Transfer data accepted", 0x76.toByte()),
    TRANSFER_EXIT_ACCEPTED("Transfer exit accepted", 0x77.toByte()),
    SECURITY_ACCESS_GRANTED("Security access granted", 0x67.toByte()),
    ECU_RESET_ACCEPTED("Ecu Reset Accepted", 0x51.toByte()),
    WRITE_IDENTIFIER_ACCEPTED("Write Identifier Accepted", 0x6e.toByte()),
    EXTENDED_DIAG_ACCEPTED("Extended diagnostics accepted", 0x50.toByte()),
    CLEAR_DTC_SUCCESSFUL("Clear DTC Successful", 0x44.toByte()),
    READ_IDENTIFIER("Read Identifier Response", 0x62.toByte()),
}

enum class UDS_COMMAND(val bytes: ByteArray){
    TESTER_PRESENT(byteArrayOf(0x3e.toByte(), 0x00.toByte())),
    RESET_ECU(byteArrayOf(0x11.toByte(), 0x01.toByte())),
    START_ROUTINE(byteArrayOf(0x31.toByte(), 0x01.toByte())),
    EXTENDED_DIAGNOSTIC(byteArrayOf(0x10.toByte())),
    READ_IDENTIFIER(byteArrayOf(0x22.toByte())),
    SECURITY_ACCESS(byteArrayOf(0x27.toByte())),
    REQUEST_DOWNLOAD(byteArrayOf(0x34.toByte())),
    TRANSFER_DATA(byteArrayOf(0x36.toByte())),
    TRANSFER_EXIT(byteArrayOf(0x37.toByte()))


}

enum class UDS_ROUTINE(val bytes: ByteArray){
    CHECK_PROGRAMMING_PRECONDITION(byteArrayOf(0x02.toByte(),0x03.toByte())),
    ERASE_BLOCK(byteArrayOf(0xFF.toByte(),0x00.toByte())),
    CHECKSUM_BLOCK(byteArrayOf(0x02.toByte(), 0x02.toByte())),

}

enum class UDS_DOWNLOAD_PROPERTIES(val bytes: ByteArray){
    ENCRYPTED_COMPRESSED(byteArrayOf(0xAA.toByte())),
    ENCRYPTED_UNCOMPRESSED(byteArrayOf(0x0A.toByte())),
    UNENCRYPTED_UNCOMPRESSED(byteArrayOf(0x00.toByte())),
    FOUR_ONE_ADDRESS_LENGTH(byteArrayOf(0x41.toByte())),

}


fun ByteArray.getUIntAt(idx: Int) =
    ((this[idx].toUInt() and 0xFFu) shl 24) or
            ((this[idx + 1].toUInt() and 0xFFu) shl 16) or
            ((this[idx + 2].toUInt() and 0xFFu) shl 8) or
            (this[idx + 3].toUInt() and 0xFFu)

fun convertToBCD(input: Int): Byte{
    var decimal = input
    var bcd = 0x00
    var place = 0

    while(decimal > 0){
        var nibble = decimal % 10
        bcd += nibble shl place
        decimal /= 10
        place += 4
    }

    return bcd.toByte()
}

fun convertFromBCD(input: Byte): Int{
    return ((input.toInt() and 0xF0) shr 4) * 10 + (input.toInt() and 0x0F)

}

fun crc8Hash(input: ByteArray): Byte{
    var sum = 0
    for(i in 0..input.size -1){
        sum = crc8Table[(sum.toUInt() xor input[i].toUInt()).toInt()]
    }

    return sum.toByte()
}


val crc8Table = arrayOf(
0x00,
0x07,
0x0E,
0x09,
0x1C,
0x1B,
0x12,
0x15,
0x38,
0x3F,
0x36,
0x31,
0x24,
0x23,
0x2A,
0x2D,
0x70,
0x77,
0x7E,
0x79,
0x6C,
0x6B,
0x62,
0x65,
0x48,
0x4F,
0x46,
0x41,
0x54,
0x53,
0x5A,
0x5D,
0xE0,
0xE7,
0xEE,
0xE9,
0xFC,
0xFB,
0xF2,
0xF5,
0xD8,
0xDF,
0xD6,
0xD1,
0xC4,
0xC3,
0xCA,
0xCD,
0x90,
0x97,
0x9E,
0x99,
0x8C,
0x8B,
0x82,
0x85,
0xA8,
0xAF,
0xA6,
0xA1,
0xB4,
0xB3,
0xBA,
0xBD,
0xC7,
0xC0,
0xC9,
0xCE,
0xDB,
0xDC,
0xD5,
0xD2,
0xFF,
0xF8,
0xF1,
0xF6,
0xE3,
0xE4,
0xED,
0xEA,
0xB7,
0xB0,
0xB9,
0xBE,
0xAB,
0xAC,
0xA5,
0xA2,
0x8F,
0x88,
0x81,
0x86,
0x93,
0x94,
0x9D,
0x9A,
0x27,
0x20,
0x29,
0x2E,
0x3B,
0x3C,
0x35,
0x32,
0x1F,
0x18,
0x11,
0x16,
0x03,
0x04,
0x0D,
0x0A,
0x57,
0x50,
0x59,
0x5E,
0x4B,
0x4C,
0x45,
0x42,
0x6F,
0x68,
0x61,
0x66,
0x73,
0x74,
0x7D,
0x7A,
0x89,
0x8E,
0x87,
0x80,
0x95,
0x92,
0x9B,
0x9C,
0xB1,
0xB6,
0xBF,
0xB8,
0xAD,
0xAA,
0xA3,
0xA4,
0xF9,
0xFE,
0xF7,
0xF0,
0xE5,
0xE2,
0xEB,
0xEC,
0xC1,
0xC6,
0xCF,
0xC8,
0xDD,
0xDA,
0xD3,
0xD4,
0x69,
0x6E,
0x67,
0x60,
0x75,
0x72,
0x7B,
0x7C,
0x51,
0x56,
0x5F,
0x58,
0x4D,
0x4A,
0x43,
0x44,
0x19,
0x1E,
0x17,
0x10,
0x05,
0x02,
0x0B,
0x0C,
0x21,
0x26,
0x2F,
0x28,
0x3D,
0x3A,
0x33,
0x34,
0x4E,
0x49,
0x40,
0x47,
0x52,
0x55,
0x5C,
0x5B,
0x76,
0x71,
0x78,
0x7F,
0x6A,
0x6D,
0x64,
0x63,
0x3E,
0x39,
0x30,
0x37,
0x22,
0x25,
0x2C,
0x2B,
0x06,
0x01,
0x08,
0x0F,
0x1A,
0x1D,
0x14,
0x13,
0xAE,
0xA9,
0xA0,
0xA7,
0xB2,
0xB5,
0xBC,
0xBB,
0x96,
0x91,
0x98,
0x9F,
0x8A,
0x8D,
0x84,
0x83,
0xDE,
0xD9,
0xD0,
0xD7,
0xC2,
0xC5,
0xCC,
0xCB,
0xE6,
0xE1,
0xE8,
0xEF,
0xFA,
0xFD,
0xF4,
0xF3,
)