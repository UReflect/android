package io.ureflect.app.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class ListFragmentPagerAdapter(fm: FragmentManager, private val list: List<Fragment>) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? = list[position]

    override fun getCount(): Int = list.size

    override fun getPageTitle(position: Int): CharSequence? = position.toString()
}

