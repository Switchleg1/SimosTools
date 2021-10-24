package com.app.simoslogger

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    private var mFragmentList: Array<Fragment> = arrayOf()
    private var mNameList: Array<String> = arrayOf()

    override fun getItemCount(): Int {
        return mFragmentList.count()
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun getName(position: Int): String {
        return mNameList[position]
    }

    fun addFragment(frag: Fragment, name: String) {
        mFragmentList += frag
        mNameList += name
    }
}