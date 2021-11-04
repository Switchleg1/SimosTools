package com.app.simostools

import android.app.*
import android.bluetooth.*
import android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH
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
import android.widget.Toast
import java.util.*
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

    fun size(): Int {
        return 8
    }

    fun size_partial(): Int {
        return 2
    }
}

class BTService: Service() {
    //constants
    val TAG = "BTService"

    // Member fields
    private var mScanning: Boolean                              = false
    private var mConnectionState: BLEConnectionState            = BLEConnectionState.NONE
    private val mWriteSemaphore: Semaphore                      = Semaphore(1)
    private val mReadQueue: ConcurrentLinkedQueue<ByteArray>    = ConcurrentLinkedQueue<ByteArray>()
    private val mWriteQueue: ConcurrentLinkedQueue<ByteArray>   = ConcurrentLinkedQueue<ByteArray>()
    private var mBluetoothGatt: BluetoothGatt?                  = null
    private var mBluetoothDevice: BluetoothDevice?              = null
    private var mConnectionThread: ConnectionThread?            = null
    private var mLogWriteState: Boolean                         = false
    private var mScanningTimer: Timer?                          = null
    private var mMTUSize: Int                                   = 23

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
            BTServiceTask.STOP_SERVICE.toString()   -> doStopService()
            BTServiceTask.START_SERVICE.toString()  -> doStartService()
            BTServiceTask.REQ_STATUS.toString()     -> sendStatus()
            BTServiceTask.DO_CONNECT.toString()     -> doConnect()
            BTServiceTask.DO_DISCONNECT.toString()  -> doDisconnect()
            BTServiceTask.DO_START_LOG.toString()   -> mConnectionThread?.setTaskState(UDSTask.LOGGING)
            BTServiceTask.DO_START_FLASH.toString() -> mConnectionThread?.setTaskState(UDSTask.FLASHING)
            BTServiceTask.DO_GET_INFO.toString()    -> mConnectionThread?.setTaskState(UDSTask.INFO)
            BTServiceTask.DO_CLEAR_DTC.toString()   -> mConnectionThread?.setTaskState(UDSTask.DTC)
            BTServiceTask.DO_STOP_TASK.toString()   -> mConnectionThread?.setTaskState(UDSTask.NONE)
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
                DebugLog.i(TAG, "Found BLE device ${device.name}")

                if (mBluetoothDevice == null && device.name.contains(BLE_DEVICE_NAME, true)) {
                    mBluetoothDevice = device

                    stopScanning()

                    DebugLog.i(TAG, "Initiating connection to ${device.name}")
                    device.connectGatt(applicationContext, false, mGattCallback, 2)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            DebugLog.w(TAG, "onScanFailed: code $errorCode")
        }
    }

    private val mGattCallback = object : BluetoothGattCallback() {
        val TAG = "BTGATTCallback"

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            //get device name
            val deviceName = gatt.device.name

            //If we are connected to the wrong device close and return
            if(mBluetoothDevice != gatt.device) {
                DebugLog.w(TAG, "Connection made to wrong device, connection closed: $deviceName")
                gatt.safeClose()
                return
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    DebugLog.i(TAG, "Successfully connected to $deviceName")

                    try {
                        //made connection, store our gatt
                        mBluetoothGatt = gatt

                        //discover gatt table
                        Handler(Looper.getMainLooper()).post {
                            gatt.discoverServices()
                        }
                    } catch (e: Exception) {
                        DebugLog.e(TAG, "Exception while requesting to discover services: ", e)
                        doDisconnect()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    DebugLog.i(TAG, "Successfully disconnected from $deviceName")

                    //disable the read notification
                    disableNotifications(gatt.getService(BLE_SERVICE_UUID).getCharacteristic(BLE_DATA_RX_UUID))

                    //If gatt doesn't match ours make sure we close it
                    if(gatt != mBluetoothGatt) {
                        gatt.safeClose()
                    }

                    //Do a full disconnect
                    doDisconnect()
                }
            } else {
                DebugLog.i(TAG, "Error $status encountered for $deviceName! Disconnecting...")

                //If gatt doesn't match ours make sure we close it
                if(gatt != mBluetoothGatt) {
                    gatt.safeClose()
                }

                //Set new connection error state
                val bleState = BLEConnectionState.ERROR
                bleState.errorMessage = status.toString()

                //Do a full disconnect
                doDisconnect(bleState)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)

            //If gatt doesn't match ours make sure we close it
            if(gatt != mBluetoothGatt) {
                gatt.safeClose()
                return
            }

            //If success request MTU
            if(status == BluetoothGatt.GATT_SUCCESS) {
                //Request new MTU
                with(gatt) {
                    DebugLog.i(TAG, "Discovered ${services.size} services for ${device.name}")

                    printGattTable()
                    try {
                        requestMtu(BLE_GATT_MTU_SIZE)
                    } catch (e: Exception) {
                        DebugLog.e(TAG,"Exception while discovering services:", e)
                        doDisconnect()
                    }
                }
            } else {
                DebugLog.w(TAG, "Failed to discover services for ${gatt.device.name}")

                //Set new connection error state
                val bleState = BLEConnectionState.ERROR
                bleState.errorMessage = status.toString()

                //Do a full disconnect
                doDisconnect(bleState)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)

            DebugLog.d(TAG, "ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")

            //get device name
            val deviceName = gatt.device.name
            if(status == BluetoothGatt.GATT_SUCCESS) {
                //Make sure we are on the right connection
                if(gatt != mBluetoothGatt) {
                    DebugLog.i(TAG, "Gatt does not match mBluetoothGatt, closing connection to $deviceName")

                    gatt.safeClose()
                    return
                }

                //Store MTU Size
                mMTUSize = mtu

                //Set new connection state
                setConnectionState(BLEConnectionState.CONNECTED)
                try {
                    gatt.requestConnectionPriority(CONNECTION_PRIORITY_HIGH)
                    enableNotifications(gatt.getService(BLE_SERVICE_UUID)!!.getCharacteristic(BLE_DATA_RX_UUID))
                } catch (e: Exception) {
                    DebugLog.e(TAG,"Exception enabling ble notifications.", e)
                    doDisconnect()
                }
            } else {
                //If gatt doesn't match ours make sure we close it
                if(gatt != mBluetoothGatt) {
                    gatt.safeClose()
                }

                //Set new connection error state
                val newState = BLEConnectionState.ERROR
                newState.errorMessage = status.toString()

                //Do a full disconnect
                doDisconnect(newState)
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    DebugLog.d("onDescWrite", "success ${descriptor.toString()}")
                }
                else -> {
                    DebugLog.w("onDescWrite", "failed ${descriptor.toString()}")
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        DebugLog.d(TAG, "Read characteristic $uuid | length: ${value.count()}")
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        DebugLog.w(TAG, "Read not permitted for $uuid!")
                    }
                    else -> {
                        DebugLog.w(TAG, "Characteristic read failed for $uuid, error: $status")
                    }
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        DebugLog.d("BluetoothGattCallback", "Wrote to characteristic $uuid | length: ${value.count()}")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        DebugLog.w("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        DebugLog.w("BluetoothGattCallback", "Write not permitted for $uuid!")
                    }
                    else -> {
                        DebugLog.w("BluetoothGattCallback", "Characteristic write failed for $uuid, error: $status")
                    }
                }
            }
            mWriteSemaphore.release()
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            with(characteristic) {
                DebugLog.d("BluetoothGattCallback", "Read from characteristic $uuid | length: ${value.count()}")

                //parse packet and check for multiple responses
                val bleHeader = BLEHeader()
                while(value.count() > 0) {
                    bleHeader.fromByteArray(value)
                    value = if(bleHeader.cmdSize+8 <= value.count()) {
                        mReadQueue.add(value.copyOfRange(0, bleHeader.cmdSize + 8))
                        value.copyOfRange(bleHeader.cmdSize + 8, value.count())
                    } else {
                        byteArrayOf()
                    }
                }
            }
        }
    }

    private fun BluetoothGatt.safeClose() {
        //get device name
        val deviceName = this.device.name

        DebugLog.i(TAG, "Closing connection to $deviceName")

        try {
            this.close()
        } catch(e: Exception){
            DebugLog.e(TAG, "Exception while closing connection to $deviceName", e)
        }
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            DebugLog.w("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(separator = "\n|--", prefix = "|--") {
                it.uuid.toString()
            }
            DebugLog.d("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable")
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
                DebugLog.w("ConnectionManager", "${characteristic.uuid} doesn't support notifications/indications")
                return
            }
        }

        characteristic.getDescriptor(BLE_CCCD_UUID)?.let { cccDescriptor ->
            if (mBluetoothGatt?.setCharacteristicNotification(characteristic, true) == false) {
                DebugLog.w("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }
            writeDescriptor(cccDescriptor, payload)
        } ?: DebugLog.w("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
    }

    private fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            DebugLog.w("ConnectionManager", "${characteristic.uuid} doesn't support indications/notifications")
            return
        }

        characteristic.getDescriptor(BLE_CCCD_UUID)?.let { cccDescriptor ->
            if (mBluetoothGatt?.setCharacteristicNotification(characteristic, false) == false) {
                DebugLog.w("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }
            writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        } ?: DebugLog.w("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
    }

    @Synchronized
    private fun stopScanning() {
        if(mScanning) {
            //Disable scan timer
            mScanningTimer?.cancel()
            mScanningTimer?.purge()
            mScanningTimer = null

            DebugLog.i(TAG, "Stop Scanning")
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

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.app_name))
            .setSmallIcon(R.drawable.simostools)
            .build()

        // Notification ID cannot be 0.
        startForeground(1, notification)
    }

    @Synchronized
    private fun doConnect() {
        doDisconnect()

        DebugLog.i(TAG, "Searching for BLE device.")

        val filter = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BLE_SERVICE_UUID.toString()))
                .build()
        )

        //Disable current scan timer
        mScanningTimer?.cancel()
        mScanningTimer?.purge()
        mScanningTimer = null

        //start scanning timer
        mScanningTimer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                doTimeout()
            }
        }
        mScanningTimer?.schedule(task, BLE_SCAN_PERIOD)

        //Set new connection status
        setConnectionState(BLEConnectionState.CONNECTING)

        //Start scanning for BLE devices
        val settings = ScanSettings.Builder().build()
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner.startScan(filter, settings, mScanCallback)
        mScanning = true
    }

    @Synchronized
    private fun doDisconnect(newState: BLEConnectionState = BLEConnectionState.NONE) {
        stopScanning()
        closeConnectionThread()

        //get device name
        mBluetoothDevice?.let {
            DebugLog.i(TAG, "Disconnecting from BLE device: ${it.name}")
            mBluetoothDevice = null
        }

        //if we have gatt, close it
        mBluetoothGatt?.let {
            it.safeClose()
            mBluetoothGatt = null
        }

        //Set new connection status
        setConnectionState(newState)
    }

    @Synchronized
    private fun doTimeout() {
        stopScanning()

        if(mConnectionState != BLEConnectionState.CONNECTED) {
            //Set new connection status
            setConnectionState(BLEConnectionState.NONE)
        }
    }

    @Synchronized
    private fun closeConnectionThread() {
        mConnectionThread?.cancel()
        mConnectionThread = null
    }

    @Synchronized
    private fun createConnectionThread() {
        closeConnectionThread()

        mConnectionThread = ConnectionThread()
        mConnectionThread?.let { thread ->
            thread.priority = BLE_THREAD_PRIORITY
            thread.start()
        }
    }

    @Synchronized
    private fun setConnectionState(newState: BLEConnectionState)
    {
        if(mConnectionState == newState)
            return

        when(newState) {
            BLEConnectionState.ERROR -> closeConnectionThread()
            BLEConnectionState.NONE -> closeConnectionThread()
            BLEConnectionState.CONNECTING -> {}
            BLEConnectionState.CONNECTED -> createConnectionThread()
        }

        //Broadcast a new message
        mConnectionState = newState
        mConnectionState.errorMessage = newState.errorMessage
        mConnectionState.deviceName = mBluetoothGatt?.device?.name ?: ""
        val intentMessage = Intent(GUIMessage.STATE_CONNECTION.toString())
        intentMessage.putExtra(GUIMessage.STATE_CONNECTION.toString(), mConnectionState)
        sendBroadcast(intentMessage)
    }

    @Synchronized
    private fun sendStatus() {
        if(mConnectionThread != null) {
            mConnectionThread?.sendTaskState()
        } else {
            val intentMessage = Intent(GUIMessage.STATE_CONNECTION.toString())
            intentMessage.putExtra(GUIMessage.STATE_CONNECTION.toString(), mConnectionState)
            sendBroadcast(intentMessage)
        }
    }

    private inner class ConnectionThread: Thread() {
        private var mTask: UDSTask      = UDSTask.NONE
        private var mTaskNext: UDSTask  = UDSTask.NONE
        private var mTaskTick: Int      = 0
        private var mTaskTime: Long     = 0
        private var mTaskTimeNext: Long = 0
        private var mTaskTimeOut: Long  = 0

        init {
            setTaskState(UDSTask.NONE)
            DebugLog.d(TAG, "create ConnectionThread")
        }

        override fun run() {
            DebugLog.d(TAG, "BEGIN mConnectionThread")

            while (mConnectionState == BLEConnectionState.CONNECTED && !currentThread().isInterrupted) {
                //See if there are any packets waiting to be sent
                if (!mWriteQueue.isEmpty() && mWriteSemaphore.tryAcquire()) {
                    try {
                        val buff = mWriteQueue.poll()
                        buff?.let {
                            DebugLog.c(TAG, buff,true)

                            mBluetoothGatt?.let { gatt ->
                                val txChar = gatt.getService(BLE_SERVICE_UUID)!!.getCharacteristic(BLE_DATA_TX_UUID)
                                val writeType = when {
                                    txChar.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                                    txChar.isWritableWithoutResponse() -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                                    else -> error("Characteristic ${txChar.uuid} cannot be written to")
                                }
                                txChar.writeType = writeType
                                txChar.value = it
                                gatt.writeCharacteristic(txChar)
                            } ?: error("Not connected to a BLE device!")
                        }
                    } catch (e: Exception) {
                        DebugLog.e(TAG, "Exception during write", e)
                        mWriteSemaphore.release()
                        cancel()
                        break
                    }
                }

                //See if there are any packets waiting to be read
                if (!mReadQueue.isEmpty()) {
                    try {
                        val buff = mReadQueue.poll()
                        buff?.let {
                            DebugLog.c(TAG, buff, false)

                            //Process packet
                            processPacket(buff)
                        }
                    } catch (e: Exception) {
                        DebugLog.e(TAG, "Exception during read", e)
                        cancel()
                        break
                    }
                }

                //Ready for next task?
                if(mTaskNext != UDSTask.NONE) {
                    if(mTaskTimeNext < System.currentTimeMillis()) {
                        DebugLog.i(TAG, "Task finished.")
                        startNextTask()
                    } else if(mTaskTimeOut < System.currentTimeMillis()) {
                        //Write debug log
                        DebugLog.w(TAG, "Task failed to finish.")
                        startNextTask()
                    }
                } else {
                    //Have we sat idle waiting without receiving a packet?
                    if(mTaskTimeNext < System.currentTimeMillis()) {
                        DebugLog.d(TAG, "Task timeout.")

                        //Process packet
                        processPacket(null)
                    }
                }
            }
            DebugLog.d(TAG, "END mConnectionThread")
        }

        fun cancel() {
            interrupt()
        }

        @Synchronized
        fun setTaskState(newTask: UDSTask)
        {
            //if we are not connected abort
            if (mConnectionState != BLEConnectionState.CONNECTED) {
                mTask = UDSTask.NONE
                return
            }

            //queue up next task and set start time
            mTaskTimeNext   = System.currentTimeMillis() + TASK_END_DELAY
            mTaskTimeOut    = System.currentTimeMillis() + TASK_END_TIMEOUT
            mTaskNext       = newTask

            //If we are doing something call for a stop
            if(mTask != UDSTask.NONE) {
                stopTask()
            }
        }

        @Synchronized
        fun sendTaskState() {
            if(UDSLogger.isEnabled()) {
                val intentMessage = Intent(GUIMessage.WRITE_LOG.toString())
                intentMessage.putExtra(GUIMessage.WRITE_LOG.toString(), UDSLogger.isEnabled())
                sendBroadcast(intentMessage)
            } else {
                val intentMessage = Intent(GUIMessage.STATE_TASK.toString())
                intentMessage.putExtra(GUIMessage.STATE_TASK.toString(), mTask)
                sendBroadcast(intentMessage)
            }
        }

        private fun writePacket(buff: ByteArray?) {
            buff?.let {
                try {
                    //Store buff to local variable
                    var buffer = it

                    //Make sure we have a header
                    if (buffer.count() < 8) {
                        DebugLog.w(TAG, "Unable to write empty packet.")
                        return
                    }

                    //Do we need to split the packet?
                    var packetSize = mMTUSize - 3
                    if(buffer.count() > packetSize) {
                        //Set split packet flag
                        it[1] = ((it[1].toInt() or BLECommandFlags.SPLIT_PK.value) and 0xFF).toByte()

                        //Add the first split packet
                        mWriteQueue.add(buffer.copyOfRange(0, packetSize))
                        buffer = buffer.copyOfRange(packetSize, buffer.count())

                        //Remaining packets
                        packetSize -= BLEHeader().size_partial()
                        var packetCount = 1
                        while (buffer.count() > 0) {
                            val dataSize = if(buffer.count() > packetSize) packetSize
                                            else buffer.count()
                            mWriteQueue.add(byteArrayOf(BLE_HEADER_PT.toByte(), (packetCount++ and 0xFF).toByte()) + buffer.copyOfRange(0, dataSize))
                            buffer = buffer.copyOfRange(dataSize, buffer.count())
                        }
                    } else {
                        //Packet fits MTU
                        mWriteQueue.add(buffer)
                    }
                } catch(e: Exception) {
                    DebugLog.e(TAG, "Exception while writing packet.", e)
                }
            } ?: DebugLog.w(TAG, "Unable to write null packet.")
        }

        private fun startNextTask() {
            mTaskTimeNext   = System.currentTimeMillis() + TASK_BUMP_DELAY
            mTask           = mTaskNext
            mTaskNext       = UDSTask.NONE
            mTaskTick       = 0
            mTaskTime       = System.currentTimeMillis()

            //Write debug log
            DebugLog.i(TAG, "Task started: $mTask")

            sendTaskState()

            when (mTask) {
                UDSTask.LOGGING     -> startTaskLogging()
                UDSTask.FLASHING    -> startTaskFlashing()
                UDSTask.INFO        -> startTaskGetInfo()
                UDSTask.DTC         -> startTaskClearDTC()
                UDSTask.NONE        -> {}
            }
        }

        private fun stopTask() {
            //Write debug log
            DebugLog.i(TAG, "Task stopped: $mTask")

            //set task to none
            mTask = UDSTask.NONE

            sendTaskState()

            //Set LED to green
            setBridgeLED(0,0x80, 0)

            //clear current persist messages
            clearBridgePersist()
        }

        private fun startTaskLogging(){
            //set connection settings
            setBridgePersistDelay(ConfigSettings.PERSIST_DELAY.toInt())
            setBridgePersistQDelay(ConfigSettings.PERSIST_Q_DELAY.toInt())

            //Write first frame
            writePacket(UDSLogger.startTask(0))
        }

        private fun startTaskFlashing(){
            DebugLog.d(TAG,"Setting stmin to 550")
            setBridgeSTMIN(350)
            writePacket(UDSFlasher.startTask(0))
        }

        private fun startTaskGetInfo(){
            writePacket(UDSInfo.startTask(0))
        }

        private fun startTaskClearDTC() {
            writePacket(UDSdtc.startTask(0))
        }

        private fun processPacket(buff: ByteArray?) {
            when (mTask) {
                UDSTask.NONE     -> processPacketNone(buff)
                UDSTask.LOGGING  -> processPacketLogging(buff)
                UDSTask.FLASHING -> processPacketFlashing(buff)
                UDSTask.INFO     -> processPacketGetInfo(buff)
                UDSTask.DTC      -> processPacketClearDTC(buff)
            }

            //Only increment task packet count when buffer isn't empty
            buff?.let {
                if(it.count() >= BLEHeader().size())
                    mTaskTick++
            }

            //check if we are ready to switch to a new task
            if (mTaskNext != UDSTask.NONE) {
                mTaskTimeNext = System.currentTimeMillis() + TASK_END_DELAY

                //Write debug log
                DebugLog.d(TAG, "Packet extended task start delay.")
            } else {
                mTaskTimeNext = System.currentTimeMillis() + TASK_BUMP_DELAY
            }
        }

        private fun processPacketNone(buff: ByteArray?) {
            buff?.let {
                if(buff.count() > 8) {
                    //Broadcast a new message
                    val intentMessage = Intent(GUIMessage.READ.toString())
                    intentMessage.putExtra(
                        GUIMessage.READ.toString(),
                        buff.copyOfRange(8, buff.size)
                    )
                    sendBroadcast(intentMessage)
                }
            }
        }

        private fun processPacketLogging(buff: ByteArray?) {
            buff?.let {
                //Process frame
                val result = UDSLogger.processPacket(mTaskTick, buff, applicationContext)

                //Are we still sending initial frames?
                if (mTaskTick < UDSLogger.frameCount()-1) {
                    //If we failed init abort
                    if (result != UDSReturn.OK) {
                        DebugLog.w(TAG, "Unable to initialize logging, UDS Error: $result")
                        setTaskState(UDSTask.NONE)
                    } else { //else continue init
                        writePacket(UDSLogger.startTask(mTaskTick+1))
                    }
                } else { //We are receiving data
                    if (result != UDSReturn.OK) {
                        DebugLog.w(TAG, "Logging data error , UDS Error: $result")
                        setTaskState(UDSTask.NONE)
                    } else {
                        //Broadcast new PID data
                        if (mTaskTick % ConfigSettings.UPDATE_RATE.toInt() == 0) {
                            val intentMessage = Intent(GUIMessage.READ_LOG.toString())
                            intentMessage.putExtra("readCount", mTaskTick)
                            intentMessage.putExtra(
                                "readTime",
                                System.currentTimeMillis() - mTaskTime
                            )
                            intentMessage.putExtra("readResult", result)
                            sendBroadcast(intentMessage)
                        }

                        //If we changed logging write states broadcast a new message and set LED color
                        if (UDSLogger.isEnabled() != mLogWriteState) {
                            //Broadcast new message
                            val intentMessage = Intent(GUIMessage.WRITE_LOG.toString())
                            intentMessage.putExtra(
                                GUIMessage.WRITE_LOG.toString(),
                                UDSLogger.isEnabled()
                            )
                            sendBroadcast(intentMessage)

                            if (UDSLogger.isEnabled()) {
                                setBridgeLED(0, 0, 0x80)
                            } else {
                                setBridgeLED(0, 0x80, 0)
                            }

                            //Update current write state
                            mLogWriteState = UDSLogger.isEnabled()
                        }
                    }
                }
            } ?: if(UDSLogger.processPacket(mTaskTick, buff, applicationContext) != UDSReturn.OK) {
                DebugLog.w(TAG, "Logging timeout.")
                setTaskState(UDSTask.NONE)
            }
        }

        private fun processPacketFlashing(buff: ByteArray?) {

            if(buff != null) {
                var response = buff!!.copyOfRange(8, buff.size)

                var flashStatus = UDSFlasher.processFlashCAL(mTaskTick, response)

                if (UDSFlasher.getInfo() != "") {
                    DebugLog.d(
                        TAG,
                        "Received status message from UDSFlash: ${UDSFlasher.getInfo()}"
                    )
                    val intentMessage = Intent(GUIMessage.FLASH_INFO.toString())
                    intentMessage.putExtra(GUIMessage.FLASH_INFO.toString(), UDSFlasher.getInfo())
                    sendBroadcast(intentMessage)
                }

                var progress = UDSFlasher.getProgress()

                if(progress > 0){
                    DebugLog.d(TAG, "Total Progress: $progress")

                    val intentMessage = Intent(GUIMessage.FLASH_PROGRESS_SHOW.toString())
                    intentMessage.putExtra(GUIMessage.FLASH_PROGRESS_SHOW.toString(), true)
                    sendBroadcast(intentMessage)

                    val intentMessage2 = Intent(GUIMessage.FLASH_PROGRESS.toString())
                    intentMessage2.putExtra(GUIMessage.FLASH_PROGRESS.toString(), progress)
                    sendBroadcast(intentMessage2)
                }
                else{
                    val intentMessage = Intent(GUIMessage.FLASH_PROGRESS_SHOW.toString())
                    intentMessage.putExtra(GUIMessage.FLASH_PROGRESS_SHOW.toString(), false)
                    sendBroadcast(intentMessage)
                }
                if(progress >= 100){
                    val intentMessage = Intent(GUIMessage.FLASH_INFO_CLEAR.toString())
                    sendBroadcast(intentMessage)
                }


                when (flashStatus) {
                    UDSReturn.OK -> {

                    }
                    UDSReturn.CLEAR_DTC_REQUEST -> {

                            //Send clear request
                            val bleHeader = BLEHeader()
                            bleHeader.rxID = 0x7E8
                            bleHeader.txID = 0x700
                            bleHeader.cmdSize = 1
                            bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value
                            val dataBytes = byteArrayOf(0x04.toByte())
                            val buf = bleHeader.toByteArray() + dataBytes
                            mWriteQueue.add(buf)

                    }
                    UDSReturn.COMMAND_QUEUED -> {
                        var queuedCommand = buildBLEFrame(UDSFlasher.getCommand())
                        //DebugLog.d(TAG,"UDSFlash, built BLE frame: " + queuedCommand.toHex())

                        writePacket(queuedCommand)

                    }
                    else -> {
                        DebugLog.d(TAG, "Received ${flashStatus} from UDSFlash")
                        setTaskState(UDSTask.NONE)
                    }
                }
            }
            else{
                if(UDSFlasher.getSubtask() == FLASH_ECU_CAL_SUBTASK.FLASH_BLOCK){
                    //Do NOTHING
                }
                else {
                    DebugLog.d(TAG, "Sending tester present.... Flasher is idle")
                    mWriteQueue.add(buildBLEFrame(UDS_COMMAND.TESTER_PRESENT.bytes))
                }
            }
        }

        private fun processPacketGetInfo(buff: ByteArray?) {
            if(UDSInfo.processPacket(mTaskTick, buff) == UDSReturn.OK) {
                val intentMessage = Intent(GUIMessage.FLASH_INFO.toString())
                intentMessage.putExtra(GUIMessage.FLASH_INFO.toString(), UDSInfo.getInfo())
                sendBroadcast(intentMessage)

                if(mTaskTick < UDSInfo.getStartCount()-1) {
                    writePacket(UDSInfo.startTask(mTaskTick + 1))
                } else {
                    setTaskState(UDSTask.NONE)
                }
            } else {
                setTaskState(UDSTask.NONE)
            }
        }

        private fun processPacketClearDTC(buff: ByteArray?) {
            if(UDSdtc.processPacket(mTaskTick, buff) == UDSReturn.OK) {
                val intentMessage = Intent(GUIMessage.FLASH_INFO.toString())
                intentMessage.putExtra(GUIMessage.FLASH_INFO.toString(), UDSdtc.getInfo())
                sendBroadcast(intentMessage)

                if(mTaskTick < UDSdtc.getStartCount()) {
                    writePacket(UDSdtc.startTask(mTaskTick + 1))
                } else {
                    setTaskState(UDSTask.NONE)
                }
            } else {
                setTaskState(UDSTask.NONE)
            }
        }

        private fun clearBridgePersist() {
            //Disable persist mode
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 0
            bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value
            writePacket(bleHeader.toByteArray())
        }

        private fun setBridgePersistDelay(delay: Int) {
            //Set persist delay
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 2
            bleHeader.cmdFlags = BLECommandFlags.SETTINGS.value or BLESettings.PERSIST_DELAY.value
            val dataBytes = byteArrayOf((delay and 0xFF).toByte(), ((delay and 0xFF00) shr 8).toByte())
            val buff = bleHeader.toByteArray() + dataBytes
            writePacket(buff)
        }

        private fun setBridgePersistQDelay(delay: Int) {
            //Set persist Q delay
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 2
            bleHeader.cmdFlags = BLECommandFlags.SETTINGS.value or BLESettings.PERSIST_Q_DELAY.value
            val dataBytes = byteArrayOf((delay and 0xFF).toByte(), ((delay and 0xFF00) shr 8).toByte())
            val buff = bleHeader.toByteArray() + dataBytes
            writePacket(buff)
        }

        private fun setBridgeLED(r: Int, g: Int, b: Int) {
            //Set LED color
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 4
            bleHeader.cmdFlags = BLECommandFlags.SETTINGS.value or BLESettings.LED_COLOR.value
            val dataBytes = byteArrayOf((b and 0xFF).toByte(), (r and 0xFF).toByte(), (g and 0xFF).toByte(), 0x00.toByte())
            val buff = bleHeader.toByteArray() + dataBytes
            writePacket(buff)
        }

        private fun setBridgeSTMIN(amount: Int) {
            //set STMIN
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 2
            bleHeader.cmdFlags = BLECommandFlags.SETTINGS.value or BLESettings.ISOTP_STMIN.value
            //val buff = bleHeader.toByteArray() + amount.toArray2()
            val buff = bleHeader.toByteArray() + byteArrayOf(
                (amount shr 0).toByte(),
                (amount shr 8).toByte(),

                )
            writePacket(buff)
        }

        private fun buildBLEFrame(udsCommand: ByteArray): ByteArray{
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = udsCommand.size
            bleHeader.cmdFlags = BLECommandFlags.PER_CLEAR.value

            return bleHeader.toByteArray() + udsCommand
        }
    }
}
