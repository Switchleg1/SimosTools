package com.app.simostools

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LoggingViewPagerAdapter(fm: Fragment) : FragmentStateAdapter(fm) {
    private var mNameList: Array<String> = arrayOf()

    override fun getItemCount(): Int {
        return mNameList.count()
    }

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = LoggingBaseFragment()
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putString(LAYOUT_NAME, mNameList[position])
        }
        return fragment
    }

    fun getName(position: Int): String {
        return mNameList[position]
    }

    fun add(name: String) {
        mNameList += name
    }
}