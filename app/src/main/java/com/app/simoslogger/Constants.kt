/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.simoslogger

import android.bluetooth.BluetoothGatt
import android.graphics.Color
import android.os.Environment
import java.util.*

// Message types sent from the BluetoothChatService Handler
enum class GUIMessage {
    STATE_CHANGE,
    TASK_CHANGE,
    TOAST,
    READ,
    ECU_INFO,
    CLEAR_DTC,
    READ_LOG,
    WRITE_LOG,
    FLASH_INFO,
}

// Constants that indicate the current connection state
enum class BLEConnectionState {
    ERROR,
    NONE,
    CONNECTING,
    CONNECTED;

    var errorMessage: String = ""
    var deviceName: String = ""
}

//List of available tasks
enum class UDSTask {
    NONE,
    LOGGING,
    FLASHING,
    INFO,
    DTC
}

//BT functions
enum class BTServiceTask {
    STOP_SERVICE,
    START_SERVICE,
    DO_CONNECT,
    DO_DISCONNECT,
    DO_START_LOG,
    DO_START_FLASH,
    DO_GET_INFO,
    DO_CLEAR_DTC,
    DO_STOP_TASK
}

//Intent constants
enum class RequiredPermissions {
    LOCATION,
    READ_STORAGE,
    WRITE_STORAGE,
}

//ISOTP bridge command flags
enum class BLECommandFlags(val value: Int) {
    PER_ENABLE(1),
    PER_CLEAR(2),
    PER_ADD(4),
    SPLIT_PK(8),
    SET_GET(64),
    SETTINGS(128)
}

//ISOTP bridge internal settings
enum class BLESettings(val value: Int) {
    ISOTP_STMIN(1),
    LED_COLOR(2),
    PERSIST_DELAY(3),
    PERSIST_Q_DELAY(4),
    BLE_SEND_DELAY(5),
    BLE_MULTI_DELAY(6)
}

//Color List
enum class ColorList(var value: Int, val cfgName: String) {
    BG_NORMAL(Color.rgb(255, 255, 255), "BGNormal"),
    BG_WARN(Color.rgb(127, 127, 255),"BGWarn"),
    TEXT(Color.rgb(110,   140,   255), "Text"),
    GAUGE_NORMAL(Color.rgb(0,   255, 0), "GaugeNormal"),
    GAUGE_WARN(Color.rgb(255, 0,   0), "GaugeWarn"),
    GAUGE_BG(Color.rgb(0, 0,   0), "GaugeBG"),
    ST_ERROR(Color.rgb(255, 0,   0), "StateError"),
    ST_NONE(Color.rgb(100, 0,   255), "StateNone"),
    ST_CONNECTING(Color.rgb(100, 100, 255), "StateConnecting"),
    ST_CONNECTED(Color.rgb(0,   0,   255), "StateConncted"),
    ST_LOGGING(Color.rgb(255, 255, 0), "StateLogging"),
    ST_WRITING(Color.rgb(0,   255, 0), "StateWriting");

    val key = "Color"
}

//Logging modes
enum class UDSLoggingMode(val cfgName: String, val addressMin: Long, val addressMax: Long) {
    MODE_22("22", 0x1000.toLong(), 0xFFFF.toLong()),
    MODE_3E("3E", 0x10000000.toLong(), 0xFFFFFFFF);

    val key = "UDSLoggingMode"
}

// UDS return codes
enum class UDSReturn {
    OK,
    ERROR_RESPONSE,
    ERROR_NULL,
    ERROR_HEADER,
    ERROR_CMDSIZE,
    ERROR_UNKNOWN,
}

enum class GearRatios(val gear: String, var ratio: Float) {
    GEAR1("1", 2.92f),
    GEAR2("2",1.79f),
    GEAR3("3",1.14f),
    GEAR4("4",0.78f),
    GEAR5("5",0.58f),
    GEAR6("6",0.46f),
    GEAR7("7",0.0f),
    FINAL("Final",4.77f);

    val key = "GearRatio"
}

enum class DirectoryList(val cfgName: String, val location: String) {
    APP("App",""),
    DOWNLOADS("Downloads", Environment.DIRECTORY_DOWNLOADS),
    DOCUMENTS("Documents", Environment.DIRECTORY_DOCUMENTS);

    val key = "OutputDirectory"
}

enum class ECUInfo(val str: String, val address: ByteArray) {
    VIN("VIN", byteArrayOf(0xf1.toByte(), 0x90.toByte())),
    ODX_IDENTIFIER("ASAM/ODX File Identifier", byteArrayOf(0xF1.toByte(), 0x9E.toByte())),
    ODX_VERSION("ASAM/ODX File Version", byteArrayOf(0xF1.toByte(), 0xA2.toByte())),
    VEHICLE_SPEED("Vehicle Speed", byteArrayOf(0xF4.toByte(), 0x0D.toByte())),
    CAL_NUMBER("Calibration Version Numbers", byteArrayOf(0xF8.toByte(), 0x06.toByte())),
    PART_NUMBER("VW Spare part Number", byteArrayOf(0xF1.toByte(), 0x87.toByte())),
    ASW_VERSION("VW ASW Version", byteArrayOf(0xF1.toByte(), 0x89.toByte())),
    HW_NUMBER("ECU Hardware Number", byteArrayOf(0xF1.toByte(), 0x91.toByte())),
    HW_VERSION("ECU Hardware Version Number", byteArrayOf(0xF1.toByte(), 0xA3.toByte())),
    ENGINE_CODE("Engine Code", byteArrayOf(0xF1.toByte(), 0xAD.toByte())),
    WORKSHOP_NAME("VW Workshop Name", byteArrayOf(0xF1.toByte(), 0xAA.toByte())),
    FLASH_STATE("State of Flash Mem", byteArrayOf(0x04.toByte(), 0x05.toByte())),
    CODE_VALUE("VW Coding Value", byteArrayOf(0x06.toByte(), 0x00.toByte()))
}

enum class FLASH_ECU_CAL_SUBTASK {
    NONE,
    GET_ECU_BOX_CODE,
    READ_FILE_FROM_STORAGE,
    CHECK_FILE_COMPAT,
    CHECKSUM_BIN,
    COMPRESS_BIN,
    ENCRYPT_BIN,
    CLEAR_DTC,
    OPEN_EXTENDED_DIAGNOSTIC,
    CHECK_PROGRAMMING_PRECONDITION, //routine 0x0203
    SA2SEEDKEY,
    WRITE_WORKSHOP_LOG,
    FLASH_BLOCK,
    CHECKSUM_BLOCK, //0x0202
    VERIFY_PROGRAMMING_DEPENDENCIES,
    RESET_ECU;

    fun next(): FLASH_ECU_CAL_SUBTASK {
        val vals = values()
        return vals[(this.ordinal+1) % vals.size];
    }
}

enum class DisplayType(val cfgName: String) {
    BAR("BarGraph"),
    ROUND("Round");

    val key = "GaugeType"
}

enum class CSVItems(val csvName: String) {
    NAME("Name"),
    UNIT("Unit"),
    EQUATION("Equation"),
    FORMAT("Format"),
    ADDRESS("Address"),
    LENGTH("Length"),
    SIGNED("Signed"),
    PROG_MIN("ProgMin"),
    PROG_MAX("ProgMax"),
    WARN_MIN("WarnMin"),
    WARN_MAX("WarnMax"),
    SMOOTHING("Smoothing"),
    ENABLED("Enabled"),
    TABS("Tabs");

    fun getHeader(): String {
        var header = ""
        values().forEachIndexed {  i, item ->
            header += item.csvName
            if(i != values().count() - 1)
                header += ","
        }

        return header
    }
}

val TASK_END_DELAY              = 500
val TASK_END_TIMEOUT            = 3000

//Service info
val CHANNEL_ID                  = "BTService"
val CHANNEL_NAME                = "BTService"

//BLE settings
val BLE_DEVICE_NAME             = "BLE_TO_ISOTP"
val BLE_GATT_MTU_SIZE           = 512
val BLE_SCAN_PERIOD             = 5000L
val BLE_CONNECTION_PRIORITY     = BluetoothGatt.CONNECTION_PRIORITY_HIGH
val BLE_THREAD_PRIORITY         = 5 //Priority (max is 10)

//ISOTP bridge UUIDS
val BLE_CCCD_UUID               = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
val BLE_SERVICE_UUID            = UUID.fromString("0000abf0-0000-1000-8000-00805f9b34fb")
val BLE_DATA_TX_UUID            = UUID.fromString("0000abf1-0000-1000-8000-00805f9b34fb")
val BLE_DATA_RX_UUID            = UUID.fromString("0000abf2-0000-1000-8000-00805f9b34fb")
val BLE_CMD_TX_UUID             = UUID.fromString("0000abf3-0000-1000-8000-00805f9b34fb")
val BLE_CMD_RX_UUID             = UUID.fromString("0000abf4-0000-1000-8000-00805f9b34fb")

//ISOTP bridge BLE header defaults
val BLE_HEADER_ID               = 0xF1
val BLE_HEADER_PT               = 0xF2
val BLE_HEADER_RX               = 0x7E8
val BLE_HEADER_TX               = 0x7E0

val MAX_PIDS                    = 100

//Log files
val DEBUG_LOG_NONE              = 0
val DEBUG_LOG_INFO              = 1
val DEBUG_LOG_WARNING           = 2
val DEBUG_LOG_DEBUG             = 4
val DEBUG_LOG_EXCEPTION         = 8
val DEBUG_LOG_COMMUNICATIONS    = 16

//Default settings
val DEFAULT_KEEP_SCREEN_ON      = true
val DEFAULT_INVERT_CRUISE       = false
val DEFAULT_UPDATE_RATE         = 4
val DEFAULT_PERSIST_DELAY       = 20
val DEFAULT_PERSIST_Q_DELAY     = 10
val DEFAULT_CALCULATE_HP        = true
val DEFAULT_USE_MS2             = true
val DEFAULT_TIRE_DIAMETER       = 0.632f
val DEFAULT_CURB_WEIGHT         = 1500f
val DEFAULT_DRAG_COEFFICIENT    = 0.000005
val DEFAULT_ALWAYS_PORTRAIT     = false
val DEFAULT_DISPLAY_SIZE        = 1f
val DEFAULT_DEBUG_LOG_FLAGS     = DEBUG_LOG_INFO or DEBUG_LOG_INFO or DEBUG_LOG_INFO

//TQ/HP Calculations
val KG_TO_N                     = 9.80665f
val TQ_CONSTANT                 = 16.3f

//Additional properties
infix fun Byte.shl(that: Int): Int = this.toInt().shl(that)
infix fun Short.shl(that: Int): Int = this.toInt().shl(that)
infix fun Byte.shr(that: Int): Int = this.toInt().shr(that)
infix fun Short.shr(that: Int): Int = this.toInt().shr(that)
infix fun Byte.and(that: Int): Int = this.toInt().and(that)
infix fun Short.and(that: Int): Int = this.toInt().and(that)
fun Byte.toHex(): String = "%02x".format(this)
fun Byte.toHexS(): String = " %02x".format(this)
fun Short.toHex(): String = "%04x".format(this)
fun Int.toHex(): String = "%08x".format(this)
fun Int.toColorInverse(): Int = Color.WHITE xor this or 0xFF000000.toInt()
fun Int.toColorHex(): String = "%06x".format(this and 0xFFFFFF)
fun Int.toTwo(): String = "%02d".format(this)
fun Int.toArray2(): ByteArray = byteArrayOf((this and 0xFF00 shr 8).toByte(), (this and 0xFF).toByte())
fun Long.toColorInt(): Int = (this.toInt() and 0xFFFFFF) or 0xFF000000.toInt()
fun Long.toHex2(): String = "%04x".format(this)
fun Long.toHex4(): String = "%08x".format(this)
fun Long.toHex(): String = "%16x".format(this)
fun Long.toArray2(): ByteArray = byteArrayOf((this and 0xFF00 shr 8).toByte(), (this and 0xFF).toByte())
fun Long.toArray4(): ByteArray = byteArrayOf((this and 0xFF000000 shr 24).toByte(), (this and 0xFF0000 shr 16).toByte(), (this and 0xFF00 shr 8).toByte(), (this and 0xFF).toByte())
fun ByteArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }