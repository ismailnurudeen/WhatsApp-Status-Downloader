package xyz.ismailnurudeen.whatsappstatusdownloader.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import xyz.ismailnurudeen.whatsappstatusdownloader.fragments.AllStatusFragment
import xyz.ismailnurudeen.whatsappstatusdownloader.fragments.DownloadedStatusFragment

class MainViewPagerAdapter(context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager) {
    val fragments = arrayOf(AllStatusFragment(), DownloadedStatusFragment())
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