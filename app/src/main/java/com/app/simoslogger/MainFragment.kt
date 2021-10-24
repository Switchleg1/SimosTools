package com.app.simoslogger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainFragment : Fragment() {
    private val TAG = "Main"
    private var mTabLayout: TabLayout? = null
    private var mViewPager: ViewPager2? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTabLayout = requireActivity().findViewById(R.id.tabLayout)
        mViewPager = requireActivity().findViewById(R.id.viewPager)

        mTabLayout?.let { tabs->
            mViewPager?.let { pager ->
                val adapter = ViewPagerAdapter(requireActivity())
                adapter.addFragment(LoggingFragment(), "Logging")
                adapter.addFragment(FlashingFragment(), "Flashing")
                adapter.addFragment(SettingsFragment(), "Settings")
                pager.adapter = adapter
                TabLayoutMediator(tabs, pager) { tab, position ->
                    tab.text = adapter.getName(position)
                }.attach()
            }
        }
    }
}