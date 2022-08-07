package com.example.clicktorun.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ViewPager(manager: FragmentManager) : FragmentStatePagerAdapter(
    manager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {
    private val fragmentList = mutableListOf<Fragment>()
    private val fragmentTitleList = mutableListOf<String>()

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }

    override fun getCount() = fragmentList.size
    override fun getItem(position: Int) = fragmentList[position]
    override fun getPageTitle(position: Int) = fragmentTitleList[position]
}