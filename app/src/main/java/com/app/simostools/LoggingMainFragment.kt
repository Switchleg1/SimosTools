package com.app.simostools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LoggingMainFragment : Fragment() {
    private val TAG = "LoggingMainFragment"
    private var mTabLayout: TabLayout? = null
    private var mViewPager: ViewPager2? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTabLayout = requireActivity().findViewById(R.id.tabLayout)
        mViewPager = requireActivity().findViewById(R.id.viewPager)

        mTabLayout?.let { tabs->
            mViewPager?.let { pager ->
                val adapter = ViewPagerAdapter(requireActivity())
                adapter.addFragment(LoggingFullFragment(), "All")
                adapter.addFragment(LoggingCustomFragment1(), "Layout1")
                adapter.addFragment(LoggingCustomFragment2(), "Layout2")
                adapter.addFragment(LoggingCustomFragment3(), "Layout3")
                adapter.addFragment(LoggingCustomFragment4(), "Layout4")
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
}