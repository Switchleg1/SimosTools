package com.app.simoslogger

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class checksummedBin(var bin: ByteArray, var fileChecksum: String, var calculatedChecksum: String, var updated: Boolean)



object FlashUtilities {



    fun checksumSimos18(bin: ByteArray): checksummedBin{
        var currentChecksum = bin.copyOfRange(0x300, 0x308)
        var offset = (0xA0800000).toUInt()
        var startAddress1 = byteArrayToInt(bin.copyOfRange(0x30c, 0x30c + 4).reversedArray()).toUInt() - offset
        var endAddress1 = byteArrayToInt(bin.copyOfRange(0x310, 0x310 + 4).reversedArray()).toUInt() - offset
        var startAddress2 = byteArrayToInt(bin.copyOfRange(0x314, 0x314 + 4).reversedArray()).toUInt() - offset
        var endAddress2 = byteArrayToInt(bin.copyOfRange(0x318, 0x318 + 4).reversedArray()).toUInt() - offset


        var checksumData: ByteArray = bin.copyOfRange(startAddress1.toInt(), endAddress1.toInt() + 1) + bin.copyOfRange(startAddress2.toInt(), endAddress2.toInt() + 1)

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
            bin[0x300 + i] = checksumCalculated[i]
        }


        return(checksummedBin(bin,currentChecksum.toHex(), checksumCalculated.toHex(), true))
    }

    fun checksumECM3(bin: ByteArray): checksummedBin{
        var startAddress = 55724
        var endAddress = 66096

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
            if(searchBuffer.size > 2)
                index = searchBuffer.copyOfRange(0, searchBuffer.size - 2).findFirst(checkCharacters) //The index where the bytes appear in the search buffer

            else
                index = -1


            //if the searchBuffer does not contain this byteArray OR we're at the end of the file...
            if(index == -1 || i == input.size - 1 || checkCharacters.size > 63){

                //If our checkCharacters array is larger than our desired minimum size...
                if(checkCharacters.size > 3 && i < input.size - 1){
                    //index = searchBuffer.copyOfRange(0, searchBuffer.size - 2).findFirst(checkCharacters.copyOfRange(0, checkCharacters.size - 1))
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
        if(inner.isEmpty()) throw IllegalArgumentException("non-empty byte sequence is required")
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
            val cipher = Cipher.getInstance("AES_128/CBC/PKCS5PADDING")
            val iv = IvParameterSpec(initVector)

            val skeySpec = SecretKeySpec(key, "AES_128")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)

            return cipher.doFinal(bin)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return byteArrayOf()
    }

    private fun byteArrayToInt(data: ByteArray): Int {
        return (data[3].toUByte().toInt() shl 0) or
                (data[2].toUByte().toInt() shl 8) or
                (data[1].toUByte().toInt() shl 16) or
                (data[0].toUByte().toInt() shl 24)
    }

    private fun intToByteArray(data: Int): ByteArray {
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

}

fun ByteArray.getUIntAt(idx: Int) =
    ((this[idx].toUInt() and 0xFFu) shl 24) or
            ((this[idx + 1].toUInt() and 0xFFu) shl 16) or
            ((this[idx + 2].toUInt() and 0xFFu) shl 8) or
            (this[idx + 3].toUInt() and 0xFFu)