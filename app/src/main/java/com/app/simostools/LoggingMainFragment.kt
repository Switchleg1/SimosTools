package com.app.simostools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.util.DisplayMetrics

import android.view.Display

import android.view.WindowManager
import androidx.core.view.size

class LoggingViewModel : ViewModel() {
    var currentTask: UDSTask = UDSTask.NONE
}

class LoggingMainFragment : Fragment() {
    private val TAG = "LoggingMainFragment"
    private var mTabLayout: TabLayout?                  = null
    private var mViewPager: ViewPager2?                 = null
    private var mViewAdapter: LoggingViewPagerAdapter?  = null
    private var mLastEnabled                            = false
    private var mPackCount: TextView?                   = null
    private lateinit var mViewModel: LoggingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DebugLog.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_main, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()

        DebugLog.d(TAG, "onDestroy")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(LoggingViewModel::class.java)

        val backButton = view.findViewById<SwitchButton>(R.id.buttonBack)
        backButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                findNavController().navigateUp()
            }
        }

        val quickViewButton = view.findViewById<SwitchButton>(R.id.buttonQuickView)
        quickViewButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                gLogViewerLoadLast = true
                findNavController().navigate(R.id.action_LoggingFragment_to_LogViewer)
            }

            setOnLongClickListener {
                findNavController().navigate(R.id.action_LoggingFragment_to_CockpitFragment)

                return@setOnLongClickListener true
            }
        }

        //Set packet textview
        mPackCount = view.findViewById(R.id.textViewPackCount)
        mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)

        mTabLayout = view.findViewById(R.id.tabLayoutLogging)
        mViewPager = view.findViewById(R.id.viewPagerLogging)

        mTabLayout?.let { tabs->
            mViewPager?.let { pager ->
                mViewAdapter = LoggingViewPagerAdapter(this)
                mViewAdapter?.let { adapter ->
                    //Add tabs
                    if(PIDs.getTabs().contains("Default"))
                        adapter.add("Default")
                    PIDs.getTabs().toSortedMap().forEach {
                        if(it.key != "" && it.key != "Default")
                            adapter.add(it.key)
                    }
                    adapter.add("ECU")
                    if(ConfigSettings.LOG_DSG.toBoolean())
                        adapter.add("DSG")

                    adapter.add("Cockpit")

                    pager.adapter = adapter
                    TabLayoutMediator(tabs, pager) { tab, position ->
                        tab.text = adapter.getName(position)
                    }.attach()

                    TabLayoutMediator(tabs, pager) { tab, position ->
                        tab.text = adapter.getName(position)
                    }.attach()
                }
            }
        }

        DebugLog.d(TAG, "onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mPackCount = null
        mTabLayout = null
        mViewPager = null

        DebugLog.d(TAG, "onDestroyView")
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.READ_LOG.toString())
        filter.addAction(GUIMessage.STATE_CONNECTION.toString())
        filter.addAction(GUIMessage.STATE_TASK.toString())
        activity?.registerReceiver(mBroadcastReceiver, filter)

        //Set background color
        mTabLayout?.setBackgroundColor(ColorList.BT_BG.value)
        mTabLayout?.setTabTextColors(ColorList.BT_TEXT.value, ColorList.BT_TEXT.value)
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)

        DebugLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()

        activity?.unregisterReceiver(mBroadcastReceiver)

        DebugLog.d(TAG, "onPause")
    }

    fun doUpdate(readCount: Int, readTime: Long) {
        //Clear stats are startup
        if(readCount < 50) {
            PIDs.resetData()
            if(UDSLogger.getModeDSG())
                PIDs.resetData(true)
        }

        //Update fps
        val fps = readCount.toFloat() / (readTime.toFloat() / 1000.0f)
        mPackCount?.text = getString(R.string.textview_fps, "%03.1f".format(fps))
        if (UDSLogger.isEnabled()) {
            //Highlight packet count in red since we are logging
            if(!mLastEnabled) {
                mPackCount?.setTextColor(ColorList.GAUGE_WARN.value)
            }
        } else {
            //Not logging set packet count to black
            if(mLastEnabled) {
                mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)
            }
        }
        mLastEnabled = UDSLogger.isEnabled()
    }

    private fun sendServiceMessage(type: String) {
        activity?.let {
            val serviceIntent = Intent(it, BTService::class.java)
            serviceIntent.action = type
            startForegroundService(it, serviceIntent)
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