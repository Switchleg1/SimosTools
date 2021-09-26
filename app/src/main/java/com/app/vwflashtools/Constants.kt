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

package com.app.vwflashtools

import java.util.*

// Message types sent from the BluetoothChatService Handler
val MESSAGE_STATE_CHANGE    = 1
val MESSAGE_TASK_CHANGE     = 2
val MESSAGE_READ            = 3
val MESSAGE_WRITE           = 4
val MESSAGE_TOAST           = 5
val MESSAGE_READ_VIN        = 6
val MESSAGE_READ_LOG        = 7

// Constants that indicate the current connection state
val STATE_ERROR         = -1 // we're doing nothing
val STATE_NONE          = 0 // we're doing nothing
val STATE_CONNECTING    = 1 // now initiating an outgoing connection
val STATE_CONNECTED     = 2 // now connected to a remote device

val TASK_NONE       = 0
val TASK_FLASHING   = 1
val TASK_LOGGING    = 2 // uploading to remote device
val TASK_RD_VIN     = 3 // download from remote device

//UUIDS
val BT_CCCD_UUID    = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
val BT_SERVICE_UUID = UUID.fromString("0000abf0-0000-1000-8000-00805f9b34fb")
val BT_DATA_TX_UUID = UUID.fromString("0000abf1-0000-1000-8000-00805f9b34fb")
val BT_DATA_RX_UUID = UUID.fromString("0000abf2-0000-1000-8000-00805f9b34fb")
val BT_CMD_TX_UUID  = UUID.fromString("0000abf3-0000-1000-8000-00805f9b34fb")
val BT_CMD_RX_UUID  = UUID.fromString("0000abf4-0000-1000-8000-00805f9b34fb")

//set MAX MTU SIZE
val GATT_MAX_MTU_SIZE = 512

//Intent constants
val REQUEST_LOCATION_PERMISSION = 1
val REQUEST_READ_STORAGE = 2
val REQUEST_WRITE_STORAGE = 3

//Timers
val SCAN_PERIOD = 10000L

//Priority (max is 10)
val THREAD_PRIORITY_CONNECTION = 5

val CHANNEL_ID = "BTService"
val CHANNEL_NAME = "BTService"

//BT functions
val BT_STOP_SERVICE     = 0
val BT_START_SERVICE    = 1
val BT_DO_CONNECT       = 2
val BT_DO_DISCONNECT    = 3
val BT_DO_SEND_STATUS   = 4
val BT_DO_CHECK_VIN     = 5
val BT_DO_CHECK_PID     = 6
val BT_DO_STOP_PID      = 7

// BLE Header defaults
val BLE_HEADER_ID = 0xF1
val BLE_HEADER_TX = 0x7E0
val BLE_HEADER_RX = 0x7E8

// Command flags
val BLE_COMMAND_FLAG_PER_ENABLE     = 1
val BLE_COMMAND_FLAG_PER_CLEAR		= 2
val BLE_COMMAND_FLAG_PER_ADD		= 4
val BLE_COMMAND_FLAG_MULT_PK		= 8
val BLE_COMMAND_FLAG_MULT_END		= 16

// UDS22Logger errors
val UDS_OK              = 0
val UDS_ERROR_RESPONSE  = 1
val UDS_ERROR_NULL      = 2
val UDS_ERROR_HEADER    = 3
val UDS_ERROR_CMDSIZE   = 4
val UDS_ERROR_UNKNOWN   = 5
val UDS_NOT_ENABLED     = 6

// Logging display modes
val DISPLAY_BARS = 0
val DISPLAY_GRAPH = 1

//Logging modes
val UDS_LOGGING_22 = 0
val UDS_LOGGING_3E = 1

//Log communications?
val LOG_COMMUNICATIONS  = false
val LOG_FILENAME        = "logging.cfg"

//Some defaults for Settings
val DEFAULT_UPDATE_RATE = 4

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
fun Int.toTwo(): String = "%02d".format(this)
fun Int.toArray2(): ByteArray = byteArrayOf((this and 0xFF00 shr 8).toByte(), (this and 0xFF).toByte())
fun Long.toHex2(): String = "%04x".format(this)
fun Long.toHex4(): String = "%08x".format(this)
fun Long.toArray4(): ByteArray = byteArrayOf((this and 0xFF000000 shr 24).toByte(), (this and 0xFF0000 shr 16).toByte(), (this and 0xFF00 shr 8).toByte(), (this and 0xFF).toByte())
fun ByteArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }


