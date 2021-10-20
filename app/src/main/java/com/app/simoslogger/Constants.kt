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

//Color index
enum class ColorIndex {
    BG_NORMAL,
    BG_WARN,
    TEXT,
    BAR_NORMAL,
    BAR_WARN,
    ST_ERROR,
    ST_NONE,
    ST_CONNECTING,
    ST_CONNECTED,
    ST_LOGGING,
    ST_WRITING
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

//ISOTP bridge command flags
val BLE_COMMAND_FLAG_PER_ENABLE = 1
val BLE_COMMAND_FLAG_PER_CLEAR  = 2
val BLE_COMMAND_FLAG_PER_ADD    = 4
val BLE_COMMAND_FLAG_SPLIT_PK   = 8
val BLE_COMMAND_FLAG_SET_GET    = 64
val BLE_COMMAND_FLAG_SETTINGS   = 128

//ISOTP bridge internal settings
val BRG_SETTING_ISOTP_STMIN     = 1
val BRG_SETTING_LED_COLOR       = 2
val BRG_SETTING_PERSIST_DELAY   = 3
val BRG_SETTING_PERSIST_Q_DELAY = 4
val BRG_SETTING_BLE_SEND_DELAY  = 5
val BRG_SETTING_BLE_MULTI_DELAY = 6

// UDS22Logger errors
val UDS_OK                      = 0
val UDS_ERROR_RESPONSE          = 1
val UDS_ERROR_NULL              = 2
val UDS_ERROR_HEADER            = 3
val UDS_ERROR_CMDSIZE           = 4
val UDS_ERROR_UNKNOWN           = 5

//Logging modes
val UDS_LOGGING_22              = 0
val UDS_LOGGING_3E              = 1

//PID Index
val PID_LIST_A                  = 0
val PID_LIST_B                  = 1
val PID_LIST_C                  = 2

//CSV PID Bitmask
val CSV_22_ADD_MIN              = 0x1000.toLong()
val CSV_22_ADD_MAX              = 0xFFFF.toLong()
val CSV_3E_ADD_MIN              = 0x10000000.toLong()
val CSV_3E_ADD_MAX              = 0xFFFFFFFF

val MAX_PIDS                    = 100
val CSV_CFG_LINE                = "Name,Unit,Equation,Format,Address,Length,Signed,ProgMin,ProgMax,WarnMin,WarnMax,Smoothing"
val CSV_VALUE_COUNT             = 12
val CFG_FILENAME                = "config.cfg"
val DEBUG_FILENAME              = "debug.log"

//Log files
val LOG_NONE                    = 0
val LOG_INFO                    = 1
val LOG_WARNING                 = 2
val LOG_DEBUG                   = 4
val LOG_EXCEPTION               = 8
val LOG_COMMUNICATIONS          = 16

//Default settings
val DEFAULT_KEEP_SCREEN_ON      = true
val DEFAULT_INVERT_CRUISE       = false
val DEFAULT_UPDATE_RATE         = 4
val DEFAULT_DIRECTORY           = Environment.DIRECTORY_DOWNLOADS
val DEFAULT_PERSIST_DELAY       = 20
val DEFAULT_PERSIST_Q_DELAY     = 10
val DEFAULT_CALCULATE_HP        = true
val DEFAULT_USE_MS2             = true
val DEFAULT_TIRE_DIAMETER       = 0.632f
val DEFAULT_CURB_WEIGHT         = 1500f
val DEFAULT_DRAG_COEFFICIENT    = 0.000002
val DEFAULT_GEAR_RATIOS         = floatArrayOf(2.92f, 1.79f, 1.14f, 0.78f, 0.58f, 0.46f, 0.0f, 4.77f)
val DEFAULT_COLOR_LIST          = intArrayOf(Color.rgb(255, 255, 255),
                                            Color.rgb(127, 127, 255),
                                            Color.rgb(0,   0,   0),
                                            Color.rgb(0,   255, 0),
                                            Color.rgb(255, 0,   0),
                                            Color.rgb(255, 0,   0),
                                            Color.rgb(100, 0,   255),
                                            Color.rgb(100, 100, 255),
                                            Color.rgb(0,   0,   255),
                                            Color.rgb(255, 255, 0),
                                            Color.rgb(0,   255, 0))
val DEFAULT_ALWAYS_PORTRAIT     = false
val DEFAULT_DISPLAY_SIZE        = 1f
val DEFAULT_LOG_FLAGS           = LOG_INFO or LOG_WARNING or LOG_EXCEPTION

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
fun Long.toArray4(): ByteArray = byteArrayOf((this and 0xFF000000 shr 24).toByte(), (this and 0xFF0000 shr 16).toByte(), (this and 0xFF00 shr 8).toByte(), (this and 0xFF).toByte())
fun ByteArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }