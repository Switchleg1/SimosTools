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
import android.util.Log
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
    var mStarted: Boolean = false
    var mState: Int = STATE_NONE
    var mTask: Int = TASK_NONE
    var mDeviceName: String? = ""
    var mConnectionError: String? = ""
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var mViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if(!mViewModel.mStarted) {
            //Read config file
            ConfigFile.read(CFG_FILENAME, applicationContext)

            //Read pid files
            PIDCSVFile.write(CSV_3E_NAME, applicationContext, DIDs.list3E)
            PIDCSVFile.write(CSV_22_NAME, applicationContext, DIDs.list22)

            val pid3EList = PIDCSVFile.read(CSV_3E_NAME, applicationContext)
            if(pid3EList != null)
                DIDs.list3E = pid3EList

            val pid22List = PIDCSVFile.read(CSV_22_NAME, applicationContext)
            if(pid22List != null && pid22List.count() == 32)
                DIDs.list22 = pid22List
        }

        //Init view
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        if(!mViewModel.mStarted) {
            //Start our BT Service
            val serviceIntent = Intent(this, BTService::class.java)
            serviceIntent.action = BT_START_SERVICE.toString()
            ContextCompat.startForegroundService(this, serviceIntent)

            //get permissions
            getPermissions()
        }

        mViewModel.mStarted = true

        val eq = "x * 1000000"
        val x = 0.001f
        val new = eval(eq.replace("x", x.toString(), true))
        Log.i(TAG, new.toString())
        Log.i(TAG, eval(".001 * 1000000").toString())
    }

    override fun onResume() {
        super.onResume()

        setStatus()

        val filter = IntentFilter()
        filter.addAction(MESSAGE_STATE_CHANGE.toString())
        filter.addAction(MESSAGE_TASK_CHANGE.toString())
        filter.addAction(MESSAGE_WRITE_LOG.toString())
        filter.addAction(MESSAGE_TOAST.toString())
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
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    doConnect()
                }
            }
            REQUEST_READ_STORAGE -> {
                if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_STORAGE)) {
                    if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION)) {
                        doConnect()
                    }
                }
            }
            REQUEST_WRITE_STORAGE -> {
                if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION)) {
                    doConnect()
                }
            }
        }
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                MESSAGE_TASK_CHANGE.toString() -> {
                    when (intent.getIntExtra("newTask", -1)) {
                        TASK_RD_VIN -> {
                            mViewModel.mTask= TASK_RD_VIN
                        }
                        TASK_LOGGING -> {
                            mViewModel.mTask = TASK_LOGGING
                        }
                        TASK_NONE -> {
                            mViewModel.mTask = TASK_NONE
                        }
                    }
                    setStatus()
                }
                MESSAGE_STATE_CHANGE.toString() -> {
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
                        setActionBarColor(Settings.colorList[COLOR_ST_WRITING])
                    } else {
                        setActionBarColor(Settings.colorList[COLOR_ST_LOGGING])
                    }
                }
                MESSAGE_TOAST.toString() -> {
                    val nToast = intent.getStringExtra("newToast")
                    Toast.makeText(context, nToast, Toast.LENGTH_SHORT).show()
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
        when(mViewModel.mTask) {
            TASK_NONE -> {
                when(mViewModel.mState) {
                    STATE_CONNECTED -> {
                        newString = getString(R.string.title_connected_to, mViewModel.mDeviceName)
                        setActionBarColor(Settings.colorList[COLOR_ST_CONNECTED])
                    }
                    STATE_CONNECTING -> {
                        newString = getString(R.string.title_connecting)
                        setActionBarColor(Settings.colorList[COLOR_ST_CONNECTING])
                    }
                    STATE_NONE -> {
                        newString = getString(R.string.title_not_connected)
                        setActionBarColor(Settings.colorList[COLOR_ST_NONE])
                    }
                    STATE_ERROR -> {
                        newString = getString(R.string.title_error, mViewModel.mConnectionError)
                        setActionBarColor(Settings.colorList[COLOR_ST_ERROR])
                    }
                }
            }
            TASK_LOGGING -> {
                newString = "Logging"
                setActionBarColor(Settings.colorList[COLOR_ST_LOGGING])
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

        if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_STORAGE)) {
            if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_STORAGE)) {
                if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION)) {
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