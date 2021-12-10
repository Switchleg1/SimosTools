package com.app.simostools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import java.lang.Exception

class CockpitViewModel : ViewModel() {
    var currentTask: UDSTask = UDSTask.NONE
}

class CockpitFragment : Fragment() {
    private val TAG = "CockpitFragment"
    private var mCockpitView: SwitchCockpit?    = null
    private var mPIDVelocity                    = -1
    private var mPIDRPM                         = -1
    private var mPIDBoost                       = -1
    private var mPIDAccelerationLatitude        = -1
    private var mPIDAccelerationLongitude       = -1
    private lateinit var mViewModel: CockpitViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cockpit, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()

        DebugLog.d(TAG, "onDestroy")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(CockpitViewModel::class.java)

        mCockpitView = view.findViewById(R.id.cockpitMain)
        mCockpitView?.apply {
            setOnClickListener() {
                findNavController().navigateUp()
            }
        }

        //Build our list of PIDS in this layout
        PIDs.getList()?.let { list ->
            //get list of custom PIDS
            for (i in 0 until list.count()) {
                list[i]?.let { pid ->
                    when (pid.address) {
                        0xf40C.toLong() -> mPIDRPM = i
                        0xd0012400      -> mPIDRPM = i

                        0x2033.toLong() -> mPIDVelocity = i
                        0xd00155b6      -> mPIDVelocity = i

                        0x39c0.toLong() -> mPIDBoost = i
                        0xd00098cc      -> mPIDBoost = i

                        0xd000ee2a      -> mPIDAccelerationLatitude = i
                        0xd00141ba      -> mPIDAccelerationLongitude = i
                    }
                }
            }
        }

        /*mCockpitView?.apply {
            dataRPM = 7000f
            mCockpitView?.doDraw()
        }*/

        DebugLog.d(TAG, "onViewCreated")
    }

    override fun onResume() {
        super.onResume()

        //Do we keep the screen on?
        view?.keepScreenOn = ConfigSettings.KEEP_SCREEN_ON.toBoolean()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.READ_LOG.toString())
        filter.addAction(GUIMessage.STATE_CONNECTION.toString())
        filter.addAction(GUIMessage.STATE_TASK.toString())
        activity?.registerReceiver(mBroadcastReceiver, filter)

        DebugLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()

        //Do we keep the screen on?
        view?.keepScreenOn = false

        activity?.unregisterReceiver(mBroadcastReceiver)

        DebugLog.d(TAG, "onPause")
    }

    private fun sendServiceMessage(type: String) {
        activity?.let {
            val serviceIntent = Intent(it, BTService::class.java)
            serviceIntent.action = type
            startForegroundService(it, serviceIntent)
        }
    }

    fun doUpdate(readCount: Int, readTime: Long) {
        //Clear stats are startup
        if(readCount < 50) {
            PIDs.resetData()
            if(UDSLogger.getModeDSG())
                PIDs.resetData(true)
        }

        PIDs.getList()?.let { list ->
            mCockpitView?.apply {
                if(mPIDAccelerationLatitude > -1)
                    dataAccelerationLatitude = list[mPIDAccelerationLatitude]?.value?:0f

                if(mPIDAccelerationLongitude > -1)
                    dataAccelerationLongitude = list[mPIDAccelerationLongitude]?.value?:0f

                if(mPIDVelocity > -1)
                    dataVelocity = list[mPIDVelocity]?.value?:0f

                if(mPIDBoost > -1)
                    dataBoost = list[mPIDBoost]?.value?:0f

                if(mPIDRPM > -1)
                    dataRPM = list[mPIDRPM]?.value?:0f

                doDraw()
            }
        }
    }

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                GUIMessage.STATE_TASK.toString() -> mViewModel.currentTask =
                    intent.getSerializableExtra(GUIMessage.STATE_TASK.toString()) as UDSTask
                GUIMessage.STATE_CONNECTION.toString() -> {
                    mViewModel.currentTask = UDSTask.NONE
                    sendServiceMessage(BTServiceTask.DO_START_LOG.toString())
                }
                GUIMessage.READ_LOG.toString() -> {
                    val readCount = intent.getIntExtra("readCount", 0)
                    val readTime = intent.getLongExtra("readTime", 0)
                    val readResult = intent.getSerializableExtra("readResult") as UDSReturn

                    //Make sure we received an ok
                    if (readResult != UDSReturn.OK) {
                        return
                    }

                    //Update callback
                    doUpdate(readCount, readTime)
                }
                else -> { }
            }
        }
    }
}