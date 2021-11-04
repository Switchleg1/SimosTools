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
    private var mTabLayout: TabLayout?          = null
    private var mViewPager: ViewPager2?         = null
    private var mFragments: Array<Fragment?>?   = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logging_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buildLayouts()
    }


    private fun buildLayouts() {
        mTabLayout = requireActivity().findViewById(R.id.tabLayout)
        mViewPager = requireActivity().findViewById(R.id.viewPager)

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
        DebugLog.d(TAG, "Built layouts")
    }
}