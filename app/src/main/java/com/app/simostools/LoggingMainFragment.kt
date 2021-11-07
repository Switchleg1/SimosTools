package com.app.simostools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    private var mTabLayout: TabLayout?          = null
    private var mViewPager: ViewPager2?         = null
    private var mFragments: Array<Fragment?>?   = null
    private var mLastEnabled                    = false
    private var mPackCount: TextView?           = null
    private lateinit var mViewModel: LoggingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_main, container, false)
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

        buildLayouts()
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(GUIMessage.READ_LOG.toString())
        filter.addAction(GUIMessage.STATE_CONNECTION.toString())
        filter.addAction(GUIMessage.STATE_TASK.toString())
        this.activity?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        this.activity?.unregisterReceiver(mBroadcastReceiver)
    }

    override fun onStart() {
        super.onStart()

        //Set background color
        view?.setBackgroundColor(ColorList.BG_NORMAL.value)
    }

    private fun buildLayouts() {
        mTabLayout = requireActivity().findViewById(R.id.tabLayoutLogging)
        mViewPager = requireActivity().findViewById(R.id.viewPagerLogging)

        mTabLayout?.let { tabs->
            mViewPager?.let { pager ->
                //pager.removeAllViews()
                val adapter = ViewPagerAdapter(requireActivity())
                mFragments = arrayOfNulls(5)
                mFragments?.let { fragments ->
                    fragments[0] = LoggingFullFragment()
                    fragments[1] = LoggingCustomFragment1()
                    fragments[2] = LoggingCustomFragment2()
                    fragments[3] = LoggingCustomFragment3()
                    fragments[4] = LoggingCustomFragment4()

                    fragments.forEach { fragment ->
                        fragment?.let {
                            adapter.addFragment(it, (it as LoggingBaseFragment).getName())
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
        DebugLog.d(TAG, "Built logging fragments")
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
    }

    private fun sendServiceMessage(type: String) {
        val serviceIntent = Intent(requireActivity(), BTService::class.java)
        serviceIntent.action = type
        ContextCompat.startForegroundService(requireActivity(), serviceIntent)
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