package com.app.simostools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
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

class LoggingViewModel : ViewModel() {
    var currentTask: UDSTask = UDSTask.NONE
}

class LoggingMainFragment : Fragment() {
    private val TAG = "LoggingMainFragment"
    private var mTabLayout: TabLayout?                      = null
    private var mViewPager: ViewPager2?                     = null
    private var mFragments: Array<LoggingBaseFragment?>?    = null
    private var mLastEnabled                                = false
    private var mPackCount: TextView?                       = null
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

        mFragments?.let { fragments ->
            for(i in 0 until fragments.count()) {
                fragments[i]?.onDestroy()
                fragments[i] = null
            }

        }
        mFragments = null

        DebugLog.d(TAG, "onDestroy")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(this).get(LoggingViewModel::class.java)

        val backButton = view.findViewById<SwitchButton>(R.id.buttonLoggingBack)
        backButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                findNavController().navigateUp()
            }
        }

        //Set packet textview
        mPackCount = view.findViewById(R.id.textViewPackCount)
        mPackCount?.setTextColor(ColorList.GAUGE_NORMAL.value)

        mTabLayout = view.findViewById(R.id.tabLayoutLogging)
        mViewPager = view.findViewById(R.id.viewPagerLogging)

        mTabLayout?.let { tabs->
            mViewPager?.let { pager ->
                //only create fragments if null
                if(mFragments == null) {
                    mFragments = arrayOfNulls(5)
                    mFragments?.let { fragments ->
                        fragments[0] = LoggingFullFragment()
                        fragments[1] = LoggingCustomFragment1()
                        fragments[2] = LoggingCustomFragment2()
                        fragments[3] = LoggingCustomFragment3()
                        fragments[4] = LoggingCustomFragment4()
                    }
                    DebugLog.d(TAG, "Created Logging Fragments.")
                }

                //add fragments to pager
                val adapter = ViewPagerAdapter(requireActivity())
                mFragments?.let { fragments ->
                    fragments.forEach { fragment ->
                        fragment?.let {
                            adapter.addFragment(it, it.getName())
                        }
                    }
                }

                pager.adapter = adapter
                TabLayoutMediator(tabs, pager) { tab, position ->
                    tab.text = adapter.getName(position)
                }.attach()

                TabLayoutMediator(tabs, pager) { tab, position ->
                    tab.text = adapter.getName(position)
                }.attach()
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
        context?.registerReceiver(mBroadcastReceiver, filter)

        //Set background color
        mTabLayout?.setBackgroundColor(ColorList.BT_BG.value)
        mTabLayout?.setTabTextColors(ColorList.BT_TEXT.value, ColorList.BT_TEXT.value)
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)

        DebugLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()

        context?.unregisterReceiver(mBroadcastReceiver)

        DebugLog.d(TAG, "onPause")
    }

    private fun doUpdate(readCount: Int, readTime: Long) {
        //Clear stats are startup
        if(readCount < 50) {
            PIDs.resetData()
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

        //update child fragments
        mFragments?.let { fragments ->
            fragments.forEach {
                it?.updateGauges()
            }
        }
    }

    private fun sendServiceMessage(type: String) {
        context?.let {
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