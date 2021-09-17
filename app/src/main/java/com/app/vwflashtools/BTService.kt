package com.app.vwflashtools

import android.app.*
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Semaphore

// Header we expect to receive on BLE packets
class BLEHeader {
    var hdID: Int = BLE_HEADER_ID
    var cmdFlags: Int = 0
    var rxID: Int = BLE_HEADER_RX
    var txID: Int = BLE_HEADER_TX
    var cmdSize: Int = 0
    var tickCount: Int = 0

    fun isValid(): Boolean {
        return hdID == BLE_HEADER_ID
    }

    fun toByteArray(): ByteArray {
        val bArray = ByteArray(8)
        bArray[0] = (hdID and 0xFF).toByte()
        bArray[1] = (cmdFlags and 0xFF).toByte()
        bArray[2] = (rxID and 0xFF).toByte()
        bArray[3] = ((rxID and 0xFF00) shr 8).toByte()
        bArray[4] = (txID and 0xFF).toByte()
        bArray[5] = ((txID and 0xFF00) shr 8).toByte()
        bArray[6] = (cmdSize and 0xFF).toByte()
        bArray[7] = ((cmdSize and 0xFF00) shr 8).toByte()

        return bArray
    }

    fun fromByteArray(bArray: ByteArray) {
        hdID = bArray[0] and 0xFF
        cmdFlags = bArray[1] and 0xFF
        rxID = ((bArray[3] and 0xFF) shl 8) + (bArray[2] and 0xFF)
        txID = ((bArray[5] and 0xFF) shl 8) + (bArray[4] and 0xFF)
        cmdSize = ((bArray[7] and 0xFF) shl 8) + (bArray[6] and 0xFF)
        tickCount = ((rxID  and 0xFFFF) shl 16) + (txID  and 0xFFFF)
    }
}

class BTService: Service() {
    //constants
    val TAG = "BTService"

    // Member fields
    private var mScanning: Boolean = false
    private var mState: Int = STATE_NONE
    private var mErrorStatus: String = ""
    private val mWriteSemaphore: Semaphore = Semaphore(1)
    private val mReadQueue = ConcurrentLinkedQueue<ByteArray>()
    private val mWriteQueue = ConcurrentLinkedQueue<ByteArray>()
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothDevice: BluetoothDevice? = null
    private var mConnectionThread: ConnectionThread? = null

    //Gatt additional properties
    private fun BluetoothGattCharacteristic.isReadable(): Boolean = containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)
    private fun BluetoothGattCharacteristic.isWritable(): Boolean = containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)
    private fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean = containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
    private fun BluetoothGattCharacteristic.isIndicatable(): Boolean = containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)
    private fun BluetoothGattCharacteristic.isNotifiable(): Boolean = containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)
    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean = properties and property != 0

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent.action) {
            BT_START_SERVICE.toString() -> {
                doStartService()
            }
            BT_STOP_SERVICE.toString() -> {
                doStopService()
            }
            BT_DO_CONNECT.toString() -> {
                doConnect()
            }
            BT_DO_DISCONNECT.toString() -> {
                doDisconnect()
            }
            BT_DO_SEND_STATUS.toString() -> {
                doSendStatus()
            }
            BT_DO_CHECK_VIN.toString() -> {
                mConnectionThread?.setTaskState(TASK_RD_VIN)
            }
            BT_DO_CHECK_PID.toString() -> {
                mConnectionThread?.setTaskState(TASK_LOGGING)
            }
            BT_DO_STOP_PID.toString() -> {
                mConnectionThread?.setTaskState(TASK_NONE)
            }
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
        doDisconnect()
    }

    private val mScanCallback = object : ScanCallback() {
        val TAG = "mScanCallback"

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            result.device?.let { device ->
                if (mBluetoothDevice == null) {
                    mBluetoothDevice = device

                    if (mScanning)
                        stopScanning()

                    Log.i(TAG, "Found BLE device! ${device.name}")

                    if (mBluetoothGatt == null) {
                        mBluetoothGatt =
                            device.connectGatt(applicationContext, false, mGattCallback, 2)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "onScanFailed: code $errorCode")
        }
    }

    private val mGattCallback = object : BluetoothGattCallback() {
        val TAG = "BTGATTCallback"

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w(TAG, "Successfully connected to $deviceAddress")

                    mBluetoothGatt = gatt

                    Handler(Looper.getMainLooper()).post {
                        mBluetoothGatt?.discoverServices()
                    }

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(TAG, "Successfully disconnected from $deviceAddress")

                    //Set new connection state
                    setConnectionState(STATE_NONE)

                    disableNotifications(gatt.getService(BT_SERVICE_UUID).getCharacteristic(BT_DATA_RX_UUID))

                    gatt.close()
                    mBluetoothDevice = null
                    mBluetoothGatt = null
                }
            } else {
                Log.w(TAG, "Error $status encountered for $deviceAddress! Disconnecting...")

                mErrorStatus = status.toString()

                //Set new connection state
                setConnectionState(STATE_ERROR, true)

                gatt.close()
                mBluetoothDevice = null
                mBluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if(status == BluetoothGatt.GATT_SUCCESS) {
                with(gatt) {
                    Log.w(TAG, "Discovered ${services.size} services for ${device.address}")
                    printGattTable() // See implementation just above this section
                    mBluetoothGatt?.requestMtu(GATT_MAX_MTU_SIZE)
                    // Consider connection setup as complete here
                }
            } else {
                mErrorStatus = status.toString()

                //Set new connection state
                setConnectionState(STATE_ERROR, true)

                gatt.close()
                mBluetoothDevice = null
                mBluetoothGatt = null
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.w(TAG,"ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")

            if(status == BluetoothGatt.GATT_SUCCESS) {
                //Set new connection state
                setConnectionState(STATE_CONNECTED)
                mBluetoothGatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)

                enableNotifications(mBluetoothGatt?.getService(BT_SERVICE_UUID)!!.getCharacteristic(BT_DATA_RX_UUID))
            } else {
                mErrorStatus = status.toString()

                //Set new connection state
                setConnectionState(STATE_ERROR, true)

                gatt.close()
                mBluetoothDevice = null
                mBluetoothGatt = null
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d("onDescWrite", "success ${descriptor.toString()}")
                }
                else -> {
                    Log.d("onDescWrite", "failed ${descriptor.toString()}")
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(TAG, "Read characteristic $uuid:\n${value}")

                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e(TAG, "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e(TAG, "Characteristic read failed for $uuid, error: $status")
                    }
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback", "Wrote to characteristic $uuid | value: $value")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for $uuid!")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback", "Characteristic write failed for $uuid, error: $status")
                    }
                }
            }
            mWriteSemaphore.release()
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            with(characteristic) {
                Log.i("BluetoothGattCallback", "Characteristic $uuid changed | value: $value")

                mReadQueue.add(value)
            }
        }
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(separator = "\n|--", prefix = "|--") {
                it.uuid.toString()
            }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable")
        }
    }

    private fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        mBluetoothGatt?.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        } ?: error("Not connected to a BLE device!")
    }

    private fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e("ConnectionManager", "${characteristic.uuid} doesn't support notifications/indications")
                return
            }
        }

        characteristic.getDescriptor(BT_CCCD_UUID)?.let { cccDescriptor ->
            if (mBluetoothGatt?.setCharacteristicNotification(characteristic, true) == false) {
                Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }
            writeDescriptor(cccDescriptor, payload)
        } ?: Log.e("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
    }

    private fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            Log.e("ConnectionManager", "${characteristic.uuid} doesn't support indications/notifications")
            return
        }

        characteristic.getDescriptor(BT_CCCD_UUID)?.let { cccDescriptor ->
            if (mBluetoothGatt?.setCharacteristicNotification(characteristic, false) == false) {
                Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }
            writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        } ?: Log.e("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
    }

    @Synchronized
    private fun stopScanning() {
        Log.i(TAG, "Stop Scanning")
        if (mScanning) {
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner.stopScan(mScanCallback)
            mScanning = false
        }
    }

    @Synchronized
    private fun doStopService() {
        LogFile.close()
        doDisconnect()
        stopForeground(true)
        stopSelf()
    }

    @Synchronized
    private fun doStartService() {
        val serviceChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.app_name))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        // Notification ID cannot be 0.
        startForeground(1, notification)
    }

    @Synchronized
    private fun doConnect() {
        doDisconnect()

        Log.w(TAG, "Searching for BLE device.")

        val filter = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BT_SERVICE_UUID.toString()))
                .build()
        )
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        //set delay to stop scanning
        Handler(Looper.getMainLooper()).postDelayed({
            doTimeout()
        }, SCAN_PERIOD)

        //Set new connection status
        setConnectionState(STATE_CONNECTING)

        //Start scanning for BLE devices
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner.startScan(filter, settings, mScanCallback)
        mScanning = true
    }

    @Synchronized
    private fun doDisconnect() {
        Log.w(TAG, "Disconnecting from BLE device.")
        if (mScanning)
            stopScanning()

        closeConnectionThread()

        if (mBluetoothGatt != null) {
            mBluetoothGatt!!.close()
            mBluetoothGatt = null
        }

        mBluetoothDevice = null

        //Set new connection status
        setConnectionState(STATE_NONE)
    }

    @Synchronized
    private fun doTimeout() {
        if(mScanning)
            stopScanning()

        if(mState != STATE_CONNECTED) {
            //Set new connection status
            setConnectionState(STATE_NONE)
        }
    }

    @Synchronized
    private fun closeConnectionThread() {
        if(mConnectionThread != null) {
            mConnectionThread!!.cancel()
            mConnectionThread = null
        }
    }

    @Synchronized
    private fun createConnectionThread() {
        mConnectionThread = ConnectionThread()
        mConnectionThread?.priority = Thread.NORM_PRIORITY
        mConnectionThread?.start()
    }

    @Synchronized
    private fun setConnectionState(newState: Int, errorMessage: Boolean = false)
    {
        when(newState) {
            STATE_CONNECTED -> {
                closeConnectionThread()
                createConnectionThread()
            }
            STATE_NONE -> {
                closeConnectionThread()
            }
            STATE_ERROR -> {
                closeConnectionThread()
            }
        }
        //Broadcast a new message
        mState = newState
        val intentMessage = Intent(MESSAGE_STATE_CHANGE.toString())
        intentMessage.putExtra("newState", mState)
        intentMessage.putExtra("cDevice", mBluetoothGatt?.device?.address)
        if(errorMessage)
            intentMessage.putExtra("newError", mErrorStatus)
        sendBroadcast(intentMessage)
    }



    @Synchronized
    private fun doSendStatus() {
        //Broadcast a new message
        val intentMessage = Intent(MESSAGE_STATE_CHANGE.toString())
        intentMessage.putExtra("newState", mState)
        intentMessage.putExtra("newError", mErrorStatus)
        intentMessage.putExtra("cDevice", mBluetoothGatt?.device?.address)
        sendBroadcast(intentMessage)
    }

    private inner class ConnectionThread: Thread() {
        //variables
        private var mTask: Int = TASK_NONE
        private var mTaskCount: Int = 0
        private var mTaskTime: Long = 0

        override fun run() {
            Log.i(TAG, "BEGIN mConnectionThread")

            while (mState == STATE_CONNECTED && !currentThread().isInterrupted) {
                //See if there are any packets waiting to be sent
                if (!mWriteQueue.isEmpty() && mWriteSemaphore.tryAcquire()) {
                    try {
                        val txChar = mBluetoothGatt!!.getService(BT_SERVICE_UUID)!!
                            .getCharacteristic(BT_DATA_TX_UUID)
                        val writeType = when {
                            txChar.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            txChar.isWritableWithoutResponse() -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                            else -> error("Characteristic ${txChar.uuid} cannot be written to")
                        }

                        val buff = mWriteQueue.poll()
                        mBluetoothGatt?.let { gatt ->
                            txChar.writeType = writeType
                            txChar.value = buff
                            gatt.writeCharacteristic(txChar)
                        } ?: error("Not connected to a BLE device!")
                    } catch (e: IOException) {
                        Log.e(TAG, "Exception during write", e)
                        mWriteSemaphore.release()
                        cancel()
                        break
                    }
                }

                //See if there are any packets waiting to be sent
                if (!mReadQueue.isEmpty()) {
                    try {
                        when (mTask) {
                            TASK_NONE -> {
                                //Broadcast a new message
                                val buff = mReadQueue.poll()
                                val intentMessage = Intent(MESSAGE_READ.toString())
                                intentMessage.putExtra("readBuffer", buff)
                                sendBroadcast(intentMessage)
                            }
                            TASK_RD_VIN -> {
                                //Broadcast a new message
                                val buff = mReadQueue.poll()
                                val intentMessage = Intent(MESSAGE_READ_VIN.toString())
                                intentMessage.putExtra("readBuffer", buff)
                                sendBroadcast(intentMessage)

                                setTaskState(TASK_NONE)
                            }
                            TASK_LOGGING -> {
                                mTaskCount++

                                //Broadcast a new message
                                val buff = mReadQueue.poll()
                                val result = UDS22Logger.processFrame(buff)

                                val intentMessage = Intent(MESSAGE_READ_LOG.toString())
                                intentMessage.putExtra("readBuffer", buff)
                                intentMessage.putExtra("readCount", mTaskCount)
                                intentMessage.putExtra("readTime", mTaskTime)
                                intentMessage.putExtra("readResult", result)
                                sendBroadcast(intentMessage)
                            }
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Exception during read", e)
                        cancel()
                        break
                    }
                }
            }
        }

        fun cancel() {
            try {
                interrupt()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during cancel", e)
            }
        }

        fun setTaskState(newTask: Int)
        {
            if (mState != STATE_CONNECTED)
                return

            //Broadcast a new message
            mTaskCount = 0
            mTaskTime = System.currentTimeMillis()
            mTask = newTask
            val intentMessage = Intent(MESSAGE_TASK_CHANGE.toString())
            intentMessage.putExtra("newTask", mTask)
            sendBroadcast(intentMessage)

            when (mTask) {
                TASK_LOGGING -> {
                    //Make sure we enable be using cruise control PID
                    UDS22Logger.didEnable = DIDs.getDID(0x203c)
                    UDS22Logger.didList = byteArrayOf(8, 9, 10, 11, 12, 13, 14, 15)

                    val bleHeader = BLEHeader()
                    bleHeader.cmdSize = 1
                    bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_ADD or BLE_COMMAND_FLAG_PER_CLEAR

                    var buff: ByteArray = byteArrayOf(0x22.toByte())
                    for(i in 0 until 8) {
                        val did: DIDStruct = DIDList[i]
                        bleHeader.cmdSize += 2
                        buff += ((did.address and 0xFF00)shr 8).toByte()
                        buff += (did.address and 0xFF).toByte()
                    }
                    //buff = bleHeader.toByteArray() + buff
                    //mWriteQueue.add(buff)

                    bleHeader.cmdSize = 1
                    bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_ADD or BLE_COMMAND_FLAG_PER_ENABLE

                    buff = byteArrayOf(0x22.toByte())
                    for(i in 8 until 16) {
                        val did: DIDStruct = DIDList[i]
                        bleHeader.cmdSize += 2
                        buff += ((did.address and 0xFF00)shr 8).toByte()
                        buff += (did.address and 0xFF).toByte()
                    }
                    buff = bleHeader.toByteArray() + buff
                    mWriteQueue.add(buff)
                }
                TASK_RD_VIN -> {
                    val bleHeader = BLEHeader()
                    bleHeader.cmdSize = 3
                    bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_CLEAR

                    val dataBytes = byteArrayOf(0x22.toByte(), 0xF1.toByte(), 0x90.toByte())

                    val buf = bleHeader.toByteArray() + dataBytes
                    mWriteQueue.add(buf)
                }
                TASK_NONE -> {
                    UDS22Logger.didEnable = null
                    UDS22Logger.didList = null

                    val bleHeader = BLEHeader()
                    bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_CLEAR

                    val buf = bleHeader.toByteArray()
                    mWriteQueue.add(buf)
                }
            }
        }

        init {
            Log.d(TAG, "create ConnectionThread")

            // Get the BluetoothSocket input and output streams
            try {

            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
        }
    }
}

