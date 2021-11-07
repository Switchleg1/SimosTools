package com.app.simostools

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SettingsMainFragment : Fragment() {
    private val TAG = "SettingsMain"
    private var mTabLayout: TabLayout?          = null
    private var mViewPager: ViewPager2?         = null
    private var mGeneralFragment: SettingsGeneralFragment?  = null
    private var mMode22Fragment: SettingsMode22Fragment?    = null
    private var mMode3EFragment: SettingsMode3EFragment?    = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        doLoad()
        buildLayouts()
        doReset()
    }

    private fun doSetColor() {
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
    }

    private fun doReset() {
        //Set colors
        doSetColor()
    }

    fun doLoad() {
        mMode22Fragment?.doLoad()

    }

    private fun doSave() {
        mGeneralFragment?.doSave()
        mMode22Fragment?.doSave()
        mMode3EFragment?.doSave()

        //Stop all tasks
        val serviceIntent = Intent(context, BTService::class.java)
        serviceIntent.action = BTServiceTask.DO_STOP_TASK.toString()
        ContextCompat.startForegroundService(this.requireContext(), serviceIntent)

        //Save CSVs
        TempPIDS.save(context)

        //Read pid files
        UDSLoggingMode.values().forEach { mode ->
            val pidList = PIDCSVFile.read(getString(R.string.filename_pid_csv, mode.cfgName), context, mode.addressMin, mode.addressMax)
            if (pidList != null)
                PIDs.setList(mode, pidList)
        }

        // Write config
        ConfigFile.write(getString(R.string.filename_config), context)
        ConfigFile.read(getString(R.string.filename_config), context)

        //Reset colors
        ColorSettings.resetColors()

        //Set colors
        doSetColor()
    }

    private fun buildLayouts() {
        mTabLayout = requireActivity().findViewById(R.id.tabLayoutSettings)
        mViewPager = requireActivity().findViewById(R.id.viewPagerSettings)

        mTabLayout?.let { tabs->
            mViewPager?.let { pager ->
                //pager.removeAllViews()
                val adapter = ViewPagerAdapter(requireActivity())
                mGeneralFragment = SettingsGeneralFragment()
                adapter.addFragment(mGeneralFragment!!, "General")

                mMode22Fragment = SettingsMode22Fragment()
                adapter.addFragment(mMode22Fragment!!, "Mode22")
                mMode3EFragment = SettingsMode3EFragment()
                adapter.addFragment(mMode3EFragment!!, "Mode3E")

                pager.adapter = adapter
                TabLayoutMediator(tabs, pager) { tab, position ->
                    tab.text = adapter.getName(position)
                }.attach()

                TabLayoutMediator(tabs, pager) { tab, position ->
                    tab.text = adapter.getName(position)
                }.attach()
            }
        }
        DebugLog.d(TAG, "Built settings fragments")
    }
}

