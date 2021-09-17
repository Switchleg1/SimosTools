package com.app.vwflashtools

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var mState: Int = STATE_NONE
    private var mTask: Int = TASK_NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        getPermissions()

        val serviceIntent = Intent(this, BTService::class.java)
        serviceIntent.action = BT_START_SERVICE.toString()
        ContextCompat.startForegroundService(this, serviceIntent)


        /*// Send test
        UDS22Logger.didEnable = DIDs.getDID(0x203c)
        UDS22Logger.didEnable?.value = 2f
        UDS22Logger.didList = byteArrayOf(8, 9, 10, 11, 12, 13, 14, 15)

        val bleHeader = BLEHeader()
        bleHeader.cmdSize = 1
        bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_ADD or BLE_COMMAND_FLAG_PER_ENABLE

        var buff = byteArrayOf(0x22.toByte())
        for(i in 8 until 16) {
            val did: DIDStruct = DIDList[i]
            bleHeader.cmdSize += 2
            buff += ((did.address and 0xFF00)shr 8).toByte()
            buff += (did.address and 0xFF).toByte()
        }
        buff = bleHeader.toByteArray() + buff

        var sData = "Sending:"
        for(i in 0 until buff.count()) {
            sData += buff[i].toHexS()
        }
        Log.i(TAG, sData)

        //Receive test
        bleHeader.cmdSize = 31
        buff = bleHeader.toByteArray() + byteArrayOf(0x62, 0x20, 0x27, 0x0F, 0x0F,
                                                        0x20, 0x3c, 0x0F, 0x0F,
                                                        0x20, 0x3f, 0x0F, 0x0F,
                                                        0x20, 0x6d, 0x0F, 0x0F,
                                                        0x29, 0x3b, 0x0F, 0x0F,
                                                        0x29, 0x5c, 0x0F,
                                                        0x29, 0x5d, 0x0F,
                                                        0x29, 0x32, 0x0F, 0x0F)

        sData = "Receiving:"
        for(i in 0 until buff.count()) {
            sData += buff[i].toHexS()
        }
        Log.i(TAG, sData)

        val result = UDS22Logger.processFrame(buff)
        Log.i(TAG, "result: $result")

        val intentMessage = Intent(MESSAGE_READ_LOG.toString())
        intentMessage.putExtra("readCount", 100)
        intentMessage.putExtra("readResult", result)
        sendBroadcast(intentMessage)*/
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(MESSAGE_STATE_CHANGE.toString())
        filter.addAction(MESSAGE_TASK_CHANGE.toString())
        registerReceiver(mBroadcastReceiver, filter)

        val serviceIntent = Intent(baseContext, BTService::class.java)
        serviceIntent.action = BT_DO_SEND_STATUS.toString()
        startForegroundService(serviceIntent)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_connect)

        if (mState > STATE_NONE) {
            item.title = getString(R.string.action_disconnect)
        } else {
            item.title = getString(R.string.action_connect)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_connect -> {
                when(mState) {
                    STATE_ERROR -> {
                        doConnect()
                    }
                    STATE_NONE -> {
                        doConnect()
                    }
                    STATE_CONNECTING -> {
                        doDisconnect()
                    }
                    STATE_CONNECTED -> {
                        doDisconnect()
                    }
                }

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.firstOrNull() != PackageManager.PERMISSION_DENIED) {
                   doConnect()
                }
            }
            REQUEST_READ_STORAGE -> {
            }
            REQUEST_WRITE_STORAGE -> {
            }
        }
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                MESSAGE_TASK_CHANGE.toString() -> {
                    when (intent.getIntExtra("newTask", -1)) {
                        TASK_RD_VIN -> {
                            setStatus("Getting VIN")
                            mTask= TASK_RD_VIN
                        }
                        TASK_LOGGING -> {
                            setStatus("Logging")
                            mTask = TASK_LOGGING
                        }
                    }
                }
                MESSAGE_STATE_CHANGE.toString() -> {
                    when (intent.getIntExtra("newState", -1)) {
                        STATE_CONNECTED -> {
                            val cDevice = intent.getStringExtra("cDevice")
                            setStatus(getString(R.string.title_connected_to, cDevice))
                            mState = STATE_CONNECTED
                        }
                        STATE_CONNECTING -> {
                            setStatus(getString(R.string.title_connecting))
                            mState = STATE_CONNECTING
                        }
                        STATE_NONE -> {
                            setStatus(getString(R.string.title_not_connected))
                            mState = STATE_NONE
                        }
                        STATE_ERROR -> {
                            val newError = intent.getStringExtra("newError")
                            setStatus(getString(R.string.title_error, newError))
                            mState = STATE_ERROR
                        }
                    }
                    invalidateOptionsMenu()
                }
            }
        }
    }

    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            Log.i(TAG, "Checking permission $permission")
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            return false
        }

        Log.i(TAG, "Already granted $permission")

        return true
    }

    var resultBTLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            doConnect()
        }
    }

    private fun doConnect() {
        if (!(getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultBTLauncher.launch(intent)
            return
        }

        if(!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION)) {
            return
        }

        val serviceIntent = Intent(baseContext, BTService::class.java)
        serviceIntent.action = BT_DO_CONNECT.toString()
        startForegroundService(serviceIntent)
    }

    private fun doDisconnect() {
        val serviceIntent = Intent(baseContext, BTService::class.java)
        serviceIntent.action = BT_DO_DISCONNECT.toString()
        startForegroundService(serviceIntent)
    }

    private fun setStatus(string: String)
    {
        supportActionBar?.title = getString(R.string.app_name) + " - " + string
    }

    private fun getPermissions() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }

        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_STORAGE)
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_STORAGE)
    }
}