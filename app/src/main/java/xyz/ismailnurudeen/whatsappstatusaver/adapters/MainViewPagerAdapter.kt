package xyz.ismailnurudeen.whatsappstatusaver.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import xyz.ismailnurudeen.whatsappstatusaver.fragments.AllStatusFragment
import xyz.ismailnurudeen.whatsappstatusaver.fragments.DownloadedStatusFragment

class MainViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    val fragments = arrayOf(AllStatusFragment(),DownloadedStatusFragment())
    val fragmentsTitles = arrayOf("All Status", "Downloaded")
    override fun getItem(i: Int): Fragment {
        return fragments[i]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentsTitles[position].toUpperCase()
    }
}