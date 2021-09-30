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
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import android.graphics.Color

import android.graphics.drawable.ColorDrawable

import android.R.attr.name

class MainViewModel : ViewModel() {
    var mState: Int = STATE_NONE
    var mTask: Int = TASK_NONE
    var mDeviceName: String? = ""
    var mConnectionError: String? = ""
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        getPermissions()

        //Start our BT Service
        val serviceIntent = Intent(this, BTService::class.java)
        serviceIntent.action = BT_START_SERVICE.toString()
        ContextCompat.startForegroundService(this, serviceIntent)

        //Read config file
        ConfigFile.read(LOG_FILENAME, applicationContext)

        //Auto connect
        doConnect()
    }

    override fun onResume() {
        super.onResume()

        setStatus()

        val filter = IntentFilter()
        filter.addAction(MESSAGE_STATE_CHANGE.toString())
        filter.addAction(MESSAGE_TASK_CHANGE.toString())
        filter.addAction(MESSAGE_WRITE_LOG.toString())
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

        val mViewModel: MainViewModel by viewModels()
        if (mViewModel.mState > STATE_NONE) {
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
        val mViewModel: MainViewModel by viewModels()
        return when (item.itemId) {
            R.id.action_connect -> {
                when(mViewModel.mState) {
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
                serviceIntent.action = BT_STOP_SERVICE.toString()
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
                            val mViewModel: MainViewModel by viewModels()
                            mViewModel.mTask= TASK_RD_VIN
                        }
                        TASK_LOGGING -> {
                            val mViewModel: MainViewModel by viewModels()
                            mViewModel.mTask = TASK_LOGGING
                        }
                        TASK_NONE -> {
                            val mViewModel: MainViewModel by viewModels()
                            mViewModel.mTask = TASK_NONE
                        }
                    }
                    setStatus()
                }
                MESSAGE_STATE_CHANGE.toString() -> {
                    val mViewModel: MainViewModel by viewModels()
                    when (intent.getIntExtra("newState", -1)) {
                        STATE_CONNECTED -> {
                            mViewModel.mDeviceName = intent.getStringExtra("cDevice")
                            mViewModel.mState = STATE_CONNECTED
                        }
                        STATE_CONNECTING -> {
                            mViewModel.mState = STATE_CONNECTING
                        }
                        STATE_NONE -> {
                            mViewModel.mState = STATE_NONE
                        }
                        STATE_ERROR -> {
                            mViewModel.mConnectionError = intent.getStringExtra("newError")
                            mViewModel.mState = STATE_ERROR
                        }
                    }
                    mViewModel.mTask = TASK_NONE
                    invalidateOptionsMenu()
                    setStatus()
                }
                MESSAGE_WRITE_LOG.toString() -> {
                    if(intent.getBooleanExtra("enabled", false)) {
                        setActionBarColor(Color.GREEN)
                    } else {
                        setActionBarColor(Color.YELLOW)
                    }
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
        //If we are already connecting abort
        val mViewModel: MainViewModel by viewModels()
        if(mViewModel.mState > STATE_NONE)
            return

        //if BT is off ask to enable
        if (!(getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultBTLauncher.launch(intent)
            return
        }

        //If we don't have permission ask
        if(!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION)) {
            return
        }

        //Tell service to connect
        val serviceIntent = Intent(baseContext, BTService::class.java)
        serviceIntent.action = BT_DO_CONNECT.toString()
        startForegroundService(serviceIntent)
    }

    private fun doDisconnect() {
        val serviceIntent = Intent(baseContext, BTService::class.java)
        serviceIntent.action = BT_DO_DISCONNECT.toString()
        startForegroundService(serviceIntent)
    }

    private fun setStatus() {
        var newString = ""
        val mViewModel: MainViewModel by viewModels()
        when(mViewModel.mTask) {
            TASK_NONE -> {
                when(mViewModel.mState) {
                    STATE_CONNECTED -> {
                        newString = getString(R.string.title_connected_to, mViewModel.mDeviceName)
                        setActionBarColor(Color.BLUE)
                    }
                    STATE_CONNECTING -> {
                        newString = getString(R.string.title_connecting)
                        setActionBarColor(Color.CYAN)
                    }
                    STATE_NONE -> {
                        newString = getString(R.string.title_not_connected)
                        setActionBarColor(Color.RED)
                    }
                    STATE_ERROR -> {
                        newString = getString(R.string.title_error, mViewModel.mConnectionError)
                        setActionBarColor(Color.RED)
                    }
                }
            }
            TASK_LOGGING -> {
                newString = "Logging"
                setActionBarColor(Color.YELLOW)
            }
            TASK_RD_VIN -> {
                newString = "Getting VIN"
            }
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

        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_STORAGE)
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_STORAGE)
    }

    private fun setActionBarColor(color: Int) {
        val colorDrawable = ColorDrawable(color)
        supportActionBar?.setBackgroundDrawable(colorDrawable)
    }
}