package com.app.simoslogger

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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProvider
import java.lang.Exception

class MainViewModel : ViewModel() {
    var started: Boolean = false
    var connectionState: BLEConnectionState = BLEConnectionState.NONE
    var currentTask: UDSTask = UDSTask.NONE
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var mViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (!mViewModel.started) {
            //Start debuglog
            DebugLog.create(DEBUG_FILENAME, applicationContext)

            //Read config file
            ConfigFile.read(CFG_FILENAME, applicationContext)

            //Write pid default files
            PIDCSVFile.write(getString(R.string.filename_3E_csv, "a"), applicationContext, PIDs.getList(UDS_LOGGING_3E, PID_LIST_A), false)
            PIDCSVFile.write(getString(R.string.filename_3E_csv, "b"), applicationContext, PIDs.getList(UDS_LOGGING_3E, PID_LIST_B), false)
            PIDCSVFile.write(getString(R.string.filename_3E_csv, "c"), applicationContext, PIDs.getList(UDS_LOGGING_3E, PID_LIST_C), false)
            PIDCSVFile.write(getString(R.string.filename_22_csv, "a"), applicationContext, PIDs.getList(UDS_LOGGING_22, PID_LIST_A), false)
            PIDCSVFile.write(getString(R.string.filename_22_csv, "b"), applicationContext, PIDs.getList(UDS_LOGGING_22, PID_LIST_B), false)
            PIDCSVFile.write(getString(R.string.filename_22_csv, "c"), applicationContext, PIDs.getList(UDS_LOGGING_22, PID_LIST_C), false)

            //Read pid files
            var pid3EList = PIDCSVFile.read(getString(R.string.filename_3E_csv, "a"), applicationContext, CSV_3E_ADD_MIN, CSV_3E_ADD_MAX)
            if (pid3EList != null)
                PIDs.setList(UDS_LOGGING_3E, PID_LIST_A, pid3EList)

            pid3EList = PIDCSVFile.read(getString(R.string.filename_3E_csv, "b"), applicationContext, CSV_3E_ADD_MIN, CSV_3E_ADD_MAX)
            if (pid3EList != null)
                PIDs.setList(UDS_LOGGING_3E, PID_LIST_B, pid3EList)

            pid3EList = PIDCSVFile.read(getString(R.string.filename_3E_csv, "c"), applicationContext, CSV_3E_ADD_MIN, CSV_3E_ADD_MAX)
            if (pid3EList != null)
                PIDs.setList(UDS_LOGGING_3E, PID_LIST_C, pid3EList)

            var pid22List = PIDCSVFile.read(getString(R.string.filename_22_csv, "a"), applicationContext, CSV_22_ADD_MIN, CSV_22_ADD_MAX)
            if (pid22List != null)
                PIDs.setList(UDS_LOGGING_22, PID_LIST_A, pid22List)

            pid22List = PIDCSVFile.read(getString(R.string.filename_22_csv, "b"), applicationContext, CSV_22_ADD_MIN, CSV_22_ADD_MAX)
            if (pid22List != null)
                PIDs.setList(UDS_LOGGING_22, PID_LIST_B, pid22List)

            pid22List = PIDCSVFile.read(getString(R.string.filename_22_csv, "c"), applicationContext, CSV_22_ADD_MIN, CSV_22_ADD_MAX)
            if (pid22List != null)
                PIDs.setList(UDS_LOGGING_22, PID_LIST_C, pid22List)

            //Start our BT Service
            val serviceIntent = Intent(this, BTService::class.java)
            serviceIntent.action = BTServiceTask.START_SERVICE.toString()
            ContextCompat.startForegroundService(this, serviceIntent)

            //get permissions
            getPermissions()

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_connect)

        if (mViewModel.connectionState > BLEConnectionState.NONE) {
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
                when(mViewModel.connectionState) {
                    BLEConnectionState.NONE -> doConnect()
                    BLEConnectionState.ERROR -> doConnect()
                    BLEConnectionState.CONNECTING -> doDisconnect()
                    BLEConnectionState.CONNECTED -> doDisconnect()
                }
                return true
            }
            R.id.SettingsFragment -> {
                val navController = findNavController(this, R.id.nav_host_fragment)
                return item.onNavDestinationSelected(navController)
            }
            R.id.LoggingFragment -> {
                val navController = findNavController(this, R.id.nav_host_fragment)
                return item.onNavDestinationSelected(navController)
            }
            R.id.FlashingFragment -> {
                val navController = findNavController(this, R.id.nav_host_fragment)
                return item.onNavDestinationSelected(navController)
            }
            R.id.action_exit -> {
                val serviceIntent = Intent(this, BTService::class.java)
                serviceIntent.action = BTServiceTask.STOP_SERVICE.toString()
                ContextCompat.startForegroundService(this, serviceIntent)
                finish()

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
                        setActionBarColor(Settings.colorList[ColorIndex.ST_WRITING.ordinal])
                    } else {
                        setActionBarColor(Settings.colorList[ColorIndex.ST_LOGGING.ordinal])
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

    var resultBTLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            doConnect()
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
                        setActionBarColor(Settings.colorList[ColorIndex.ST_ERROR.ordinal])
                    }
                    BLEConnectionState.NONE -> {
                        newString = getString(R.string.title_not_connected)
                        setActionBarColor(Settings.colorList[ColorIndex.ST_NONE.ordinal])
                    }
                    BLEConnectionState.CONNECTING -> {
                        newString = getString(R.string.title_connecting)
                        setActionBarColor(Settings.colorList[ColorIndex.ST_CONNECTING.ordinal])
                    }
                    BLEConnectionState.CONNECTED -> {
                        newString = getString(R.string.title_connected_to, mViewModel.connectionState.deviceName)
                        setActionBarColor(Settings.colorList[ColorIndex.ST_CONNECTED.ordinal])
                    }
                }
            }
            UDSTask.LOGGING -> {
                newString = "Logging"
                setActionBarColor(Settings.colorList[ColorIndex.ST_LOGGING.ordinal])
            }
            UDSTask.FLASHING -> {
                newString = "Flashing"
                setActionBarColor(Settings.colorList[ColorIndex.ST_LOGGING.ordinal])
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