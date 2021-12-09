package com.app.simostools

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SettingsMainFragment : Fragment() {
    private val TAG = "SettingsMain"
    private var mTabLayout: TabLayout?                      = null
    private var mViewPager: ViewPager2?                     = null
    private var mViewAdapter: SettingsViewPagerAdapter?     = null
    private var mGeneralFragment: SettingsGeneralFragment?  = null
    private var mCarFragment: SettingsCarFragment?          = null
    private var mMode22Fragment: SettingsMode22Fragment?    = null
    private var mMode3EFragment: SettingsMode3EFragment?    = null
    private var mModeDSGFragment: SettingsModeDSGFragment?  = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DebugLog.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<SwitchButton>(R.id.buttonSettingsBack)
        backButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                findNavController().navigateUp()
            }
        }

        val saveButton = view.findViewById<SwitchButton>(R.id.buttonSettingsSave)
        saveButton.apply {
            paintBG.color = ColorList.BT_BG.value
            paintRim.color = ColorList.BT_RIM.value
            setTextColor(ColorList.BT_TEXT.value)
            setOnClickListener {
                doSave()
                findNavController().navigateUp()
            }
        }

        mTabLayout = view.findViewById(R.id.tabLayoutSettings)
        mViewPager = view.findViewById(R.id.viewPagerSettings)

        mTabLayout?.let { tabs ->
            mViewPager?.let { pager ->
                mViewAdapter = SettingsViewPagerAdapter(this)
                mViewAdapter?.let { adapter ->
                    if (mGeneralFragment == null) {
                        mGeneralFragment = SettingsGeneralFragment()
                        mGeneralFragment?.setLoadCallback { doLoad() }
                    }
                    adapter.addFragment(mGeneralFragment!!, "General")
                    if (mCarFragment == null) {
                        mCarFragment = SettingsCarFragment()
                    }
                    adapter.addFragment(mCarFragment!!, "Car")
                    if (mMode22Fragment == null) {
                        mMode22Fragment = SettingsMode22Fragment()
                    }
                    adapter.addFragment(mMode22Fragment!!, "Mode22")
                    if (mMode3EFragment == null) {
                        mMode3EFragment = SettingsMode3EFragment()
                    }
                    adapter.addFragment(mMode3EFragment!!, "Mode3E")
                    if (mModeDSGFragment == null) {
                        mModeDSGFragment = SettingsModeDSGFragment()
                    }
                    adapter.addFragment(mModeDSGFragment!!, "ModeDSG")

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

        doLoad()

        //Set colors
        doSetColor()

        DebugLog.d(TAG, "onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mTabLayout          = null
        mViewPager          = null
        DebugLog.d(TAG, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()

        mGeneralFragment?.onDestroy()
        mGeneralFragment    = null
        mCarFragment?.onDestroy()
        mCarFragment        = null
        mMode22Fragment?.onDestroy()
        mMode22Fragment     = null
        mMode3EFragment?.onDestroy()
        mMode3EFragment     = null
        mModeDSGFragment?.onDestroy()
        mModeDSGFragment     = null

        DebugLog.d(TAG, "onDestroy")
    }

    private fun doSetColor() {
        mGeneralFragment?.doSetColor()
        mCarFragment?.doSetColor()
        mMode22Fragment?.doSetColor()
        mMode3EFragment?.doSetColor()
        mModeDSGFragment?.doSetColor()

        view?.let { currentView ->
            //Set button color
            val backButton = currentView.findViewById<SwitchButton>(R.id.buttonSettingsBack)
            backButton.apply {
                paintBG.color = ColorList.BT_BG.value
                paintRim.color = ColorList.BT_RIM.value
                setTextColor(ColorList.BT_TEXT.value)
            }

            val saveButton = currentView.findViewById<SwitchButton>(R.id.buttonSettingsSave)
            saveButton.apply {
                paintBG.color = ColorList.BT_BG.value
                paintRim.color = ColorList.BT_RIM.value
                setTextColor(ColorList.BT_TEXT.value)
            }

            //Set background color
            currentView.setBackgroundColor(ColorList.BG_NORMAL.value)
        }

        mTabLayout?.setBackgroundColor(ColorList.BT_BG.value)
        mTabLayout?.setTabTextColors(ColorList.BT_TEXT.value, ColorList.BT_TEXT.value)

        DebugLog.d(TAG, "doSetColor")
    }

    fun doLoad() {
        mMode22Fragment?.doLoad()
        mMode3EFragment?.doLoad()
        mModeDSGFragment?.doLoad()

        DebugLog.d(TAG, "doLoad")
    }

    private fun doSave() {
        mGeneralFragment?.doSave()
        mCarFragment?.doSave()
        mMode22Fragment?.doSave()
        mMode3EFragment?.doSave()
        mModeDSGFragment?.doSave()

        //Stop all tasks
        sendServiceMessage(BTServiceTask.DO_STOP_TASK.toString())

        //Save CSVs
        TempPIDS.save(context)

        //Read pid files
        UDSLoggingMode.values().forEach { mode ->
            val pidList = PIDCSVFile.read(getString(R.string.filename_pid_csv, mode.cfgName), context, mode.addressMin, mode.addressMax)
            if (pidList != null)
                PIDs.setList(mode, pidList)
        }

        val pidList = PIDCSVFile.read(getString(R.string.filename_pid_csv, "DSG"), context, UDSLoggingMode.MODE_22.addressMin, UDSLoggingMode.MODE_22.addressMax)
        if (pidList != null)
            PIDs.setDSGList(pidList)

        // Write config
        ConfigFile.write(getString(R.string.filename_config), context)
        ConfigFile.read(getString(R.string.filename_config), context)

        //Reset colors
        ColorSettings.resetColors()

        //Set colors
        doSetColor()

        DebugLog.d(TAG, "doSave")
    }

    private fun sendServiceMessage(type: String) {
        activity?.let {
            val serviceIntent = Intent(it, BTService::class.java)
            serviceIntent.action = type
            startForegroundService(it, serviceIntent)
        }
    }
}

