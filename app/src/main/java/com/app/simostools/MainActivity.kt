package com.app.simostools

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProvider
import java.util.*


class MainViewModel : ViewModel() {
    var started: Boolean                    = false
    var connectionState: BLEConnectionState = BLEConnectionState.NONE
    var currentTask: UDSTask                = UDSTask.NONE
    var guiTimer: Timer?                    = null
    var writeLog: Boolean                   = false
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var mAskingPermission = false
    private lateinit var mViewModel: MainViewModel

    var resultBTLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            doConnect()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (!mViewModel.started) {
            //Start debuglog
            DebugLog.create(getString(R.string.filename_debug_log), applicationContext)

            //Read config file
            ConfigFile.read(getString(R.string.filename_config), applicationContext)

            //Build default PID data in case we don't load a csv
            PIDs.init()

            //Write pid default files
            UDSLoggingMode.values().forEach { mode ->
                //write default
                PIDCSVFile.write(getString(R.string.filename_pid_csv, mode.cfgName), applicationContext, PIDs.getList(mode), false)

                //Read pid files
                val pidList = PIDCSVFile.read(getString(R.string.filename_pid_csv, mode.cfgName), applicationContext, mode.addressMin, mode.addressMax)
                if (pidList != null)
                    PIDs.setList(mode, pidList)
            }

            //Start our BT Service
            sendServiceMessage(BTServiceTask.START_SERVICE.toString())

            //get permissions
            getPermissions()

            //start GUI timer
            if(mViewModel.guiTimer == null) {
                // creating timer task, timer
                mViewModel.guiTimer = Timer()

                val task = object : TimerTask() {
                    override fun run() {
                        timerCallback()
                    }
                }
                mViewModel.guiTimer?.scheduleAtFixedRate(task, 1000, 1000)
            }

            //Save started
            mViewModel.started = true
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        window.statusBarColor = ColorList.BT_BG.value
        window.navigationBarColor = ColorList.BT_BG.value
    }

    override fun onResume() {
        super.onResume()

        setStatus()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.STATE_CONNECTION.toString())
        filter.addAction(GUIMessage.STATE_TASK.toString())
        filter.addAction(GUIMessage.WRITE_LOG.toString())
        filter.addAction(GUIMessage.TOAST.toString())
        registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(mBroadcastReceiver)
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                GUIMessage.STATE_TASK.toString() -> {
                    mViewModel.currentTask = intent.getSerializableExtra(GUIMessage.STATE_TASK.toString()) as UDSTask
                    setStatus()
                }
                GUIMessage.STATE_CONNECTION.toString() -> {
                    val connectionState = intent.getSerializableExtra(GUIMessage.STATE_CONNECTION.toString()) as BLEConnectionState
                    mViewModel.connectionState = connectionState
                    mViewModel.currentTask = UDSTask.NONE
                    setStatus()
                }
                GUIMessage.WRITE_LOG.toString() -> {
                    if(intent.getBooleanExtra(GUIMessage.WRITE_LOG.toString(), false)) {
                        mViewModel.writeLog = true
                        setStatus()
                    } else {
                        mViewModel.writeLog = false
                        setStatus()
                    }
                }
                GUIMessage.TOAST.toString() -> {
                    val nToast = intent.getStringExtra(GUIMessage.TOAST.toString())
                    Toast.makeText(context, nToast, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun timerCallback() {
        when(mViewModel.connectionState) {
            BLEConnectionState.ERROR      -> doConnect()
            BLEConnectionState.NONE       -> doConnect()
            BLEConnectionState.CONNECTING -> { }
            BLEConnectionState.CONNECTED  -> {
                if(ConfigSettings.AUTO_LOG.toBoolean() && mViewModel.currentTask == UDSTask.NONE) {
                    sendServiceMessage(BTServiceTask.DO_START_LOG.toString())
                }
            }
        }
        sendServiceMessage(BTServiceTask.REQ_STATUS.toString())
    }

    private fun sendServiceMessage(type: String) {
        applicationContext?.let {
            val serviceIntent = Intent(it, BTService::class.java)
            serviceIntent.action = type
            startForegroundService(serviceIntent)
        }
    }

    private fun doConnect() {
        //If we are already connecting abort
        if(mViewModel.connectionState > BLEConnectionState.NONE)
            return

        //if BT is off ask to enable
        if (!(getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultBTLauncher.launch(intent)
            return
        }

        //check permissions
        var havePermissions = true
        RequiredPermissions.values().forEach {
            if(it.required && it.result == PackageManager.PERMISSION_DENIED)
                havePermissions = false
        }

        if(havePermissions) {
            //Tell service to connect
            sendServiceMessage(BTServiceTask.DO_CONNECT.toString())
        } else {
            checkNextPermission(0, true)
        }
    }

    private fun doDisconnect() {
        sendServiceMessage(BTServiceTask.DO_DISCONNECT.toString())
    }

    private fun setStatus() {
        var newString = ""
        when(mViewModel.currentTask) {
            UDSTask.NONE -> {
                when(mViewModel.connectionState) {
                    BLEConnectionState.ERROR -> {
                        newString = getString(R.string.title_error, mViewModel.connectionState.errorMessage)
                        setActionBarColor(ColorList.ST_ERROR.value)
                    }
                    BLEConnectionState.NONE -> {
                        newString = getString(R.string.title_not_connected)
                        setActionBarColor(ColorList.ST_NONE.value)
                    }
                    BLEConnectionState.CONNECTING -> {
                        newString = getString(R.string.title_connecting)
                        setActionBarColor(ColorList.ST_CONNECTING.value)
                    }
                    BLEConnectionState.CONNECTED -> {
                        newString = getString(R.string.title_connected_to, mViewModel.connectionState.deviceName)
                        setActionBarColor(ColorList.ST_CONNECTED.value)
                    }
                }
            }
            UDSTask.LOGGING -> {
                if(mViewModel.writeLog) {
                    newString = "Logging"
                    setActionBarColor(ColorList.ST_WRITING.value)
                } else {
                    newString = "Polling"
                    setActionBarColor(ColorList.ST_LOGGING.value)
                }
            }
            UDSTask.FLASHING -> {
                newString = "Flashing"
                setActionBarColor(ColorList.ST_LOGGING.value)
            }
            UDSTask.INFO        -> newString = "Getting ECU Info"
            UDSTask.DTC_GET     -> newString = "Getting DTC"
            UDSTask.DTC_CLEAR   -> newString = "Clearing DTC"
            UDSTask.SET_ADAPTER -> newString = "Setting Adapter Name"
        }
        supportActionBar?.title = getString(R.string.app_name) + " - " + newString
    }

    private fun setActionBarColor(color: Int) {
        val colorDrawable = ColorDrawable(color)
        supportActionBar?.setBackgroundDrawable(colorDrawable)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        mAskingPermission = false

        if(requestCode < RequiredPermissions.values().count())
        {
            RequiredPermissions.values()[requestCode].result = grantResults.firstOrNull() ?: PackageManager.PERMISSION_DENIED
            if(RequiredPermissions.values()[requestCode].required && RequiredPermissions.values()[requestCode].result == PackageManager.PERMISSION_DENIED) {
                DebugLog.i(TAG, "Permission was denied and is required ${RequiredPermissions.values()[requestCode].permission}.")
                checkNextPermission(requestCode)
            } else {
                if (requestCode == RequiredPermissions.values().count() - 1) {
                    doConnect()
                } else {
                    checkNextPermission(requestCode + 1)
                }
            }
        }
    }

    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        if(mAskingPermission)
            return false

        if (Build.VERSION.SDK_INT >= RequiredPermissions.values()[requestCode].version &&
            ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            DebugLog.i(TAG, "Asking for permission $permission")
            mAskingPermission = true
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            return false
        }

        RequiredPermissions.values()[requestCode].result = PackageManager.PERMISSION_GRANTED
        DebugLog.i(TAG, "Already granted $permission")

        return true
    }

    private fun checkNextPermission(permission: Int, required: Boolean = false): Boolean {
        for(i in permission until RequiredPermissions.values().count()) {
            if(required && !RequiredPermissions.values()[i].required)
                continue

            if(!checkPermission(RequiredPermissions.values()[i].permission, i))
                return false
        }

        return true
    }

    private fun getPermissions() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }


        if (checkNextPermission(0))
            doConnect()
    }
}
