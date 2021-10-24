package com.app.simoslogger

import android.Manifest
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
    var started: Boolean = false
    var connectionState: BLEConnectionState = BLEConnectionState.NONE
    var currentTask: UDSTask = UDSTask.NONE
    var guiTimer: Timer? = null
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
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
            val serviceIntent = Intent(this, BTService::class.java)
            serviceIntent.action = BTServiceTask.START_SERVICE.toString()
            ContextCompat.startForegroundService(this, serviceIntent)

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
                mViewModel.guiTimer?.scheduleAtFixedRate(task, 5000, 5000)
            }

            //Save started
            mViewModel.started = true
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onResume() {
        super.onResume()

        setStatus()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.STATE_CHANGE.toString())
        filter.addAction(GUIMessage.TASK_CHANGE.toString())
        filter.addAction(GUIMessage.WRITE_LOG.toString())
        filter.addAction(GUIMessage.TOAST.toString())
        registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RequiredPermissions.LOCATION.ordinal -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    doConnect()
                }
            }
            RequiredPermissions.READ_STORAGE.ordinal -> {
                if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, RequiredPermissions.WRITE_STORAGE.ordinal)) {
                    if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, RequiredPermissions.LOCATION.ordinal)) {
                        doConnect()
                    }
                }
            }
            RequiredPermissions.WRITE_STORAGE.ordinal -> {
                if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, RequiredPermissions.LOCATION.ordinal)) {
                    doConnect()
                }
            }
        }
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                GUIMessage.TASK_CHANGE.toString() -> {
                    mViewModel.currentTask = intent.getSerializableExtra(GUIMessage.TASK_CHANGE.toString()) as UDSTask
                    setStatus()
                }
                GUIMessage.STATE_CHANGE.toString() -> {
                    val connectionState = intent.getSerializableExtra(GUIMessage.STATE_CHANGE.toString()) as BLEConnectionState
                    mViewModel.connectionState = connectionState
                    mViewModel.currentTask = UDSTask.NONE
                    invalidateOptionsMenu()
                    setStatus()
                }
                GUIMessage.WRITE_LOG.toString() -> {
                    if(intent.getBooleanExtra(GUIMessage.WRITE_LOG.toString(), false)) {
                        setActionBarColor(ColorList.ST_WRITING.value)
                    } else {
                        setActionBarColor(ColorList.ST_LOGGING.value)
                    }
                }
                GUIMessage.TOAST.toString() -> {
                    val nToast = intent.getStringExtra(GUIMessage.TOAST.toString())
                    Toast.makeText(context, nToast, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            DebugLog.i(TAG, "Asking for permission $permission")
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            return false
        }

        DebugLog.d(TAG, "Already granted $permission")

        return true
    }

    private fun timerCallback() {
        when(mViewModel.connectionState) {
            BLEConnectionState.ERROR     -> doConnect()
            BLEConnectionState.NONE      -> doConnect()
            BLEConnectionState.CONNECTED -> {
                if(mViewModel.currentTask == UDSTask.NONE) {
                    //Lets start logging
                    val serviceIntent = Intent(this, BTService::class.java)
                    serviceIntent.action = BTServiceTask.DO_START_LOG.toString()
                    ContextCompat.startForegroundService(this, serviceIntent)
                }
            }
            else -> {}
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

        //If we don't have permission ask
        if(!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, RequiredPermissions.LOCATION.ordinal)) {
            return
        }

        //Tell service to connect
        val serviceIntent = Intent(baseContext, BTService::class.java)
        serviceIntent.action = BTServiceTask.DO_CONNECT.toString()
        startForegroundService(serviceIntent)
    }

    private fun doDisconnect() {
        val serviceIntent = Intent(baseContext, BTService::class.java)
        serviceIntent.action = BTServiceTask.DO_DISCONNECT.toString()
        startForegroundService(serviceIntent)
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
                newString = "Logging"
                setActionBarColor(ColorList.ST_LOGGING.value)
            }
            UDSTask.FLASHING -> {
                newString = "Flashing"
                setActionBarColor(ColorList.ST_LOGGING.value)
            }
            UDSTask.INFO -> newString = "Getting ECU Info"
            UDSTask.DTC -> newString = "Clearing DTC"
        }
        supportActionBar?.title = getString(R.string.app_name) + " - " + newString
    }

    private fun getPermissions() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }

        if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, RequiredPermissions.READ_STORAGE.ordinal)) {
            if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, RequiredPermissions.WRITE_STORAGE.ordinal)) {
                if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, RequiredPermissions.LOCATION.ordinal)) {
                    doConnect()
                }
            }
        }
    }

    private fun setActionBarColor(color: Int) {
        val colorDrawable = ColorDrawable(color)
        supportActionBar?.setBackgroundDrawable(colorDrawable)
    }
}