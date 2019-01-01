package xyz.ismailnurudeen.whatsappstatusdownloader

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.ToxicBakery.viewpager.transforms.RotateDownTransformer
import kotlinx.android.synthetic.main.activity_preview.*
import xyz.ismailnurudeen.whatsappstatusdownloader.adapters.AltPreviewAdapter
import xyz.ismailnurudeen.whatsappstatusdownloader.adapters.PreviewAdapter
import xyz.ismailnurudeen.whatsappstatusdownloader.fragments.PreviewFragment
import xyz.ismailnurudeen.whatsappstatusdownloader.utils.AppUtil
import java.io.File
import java.util.*

class PreviewActivity : AppCompatActivity() {
    lateinit var pagerAdapter: PreviewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_preview)
        val appUtil = AppUtil(this)

        val extras = intent.extras
        val position = extras!!.getInt("POSITION")
        val fromAll = extras.getBoolean("FROM_ALL", true)
        val titles = ArrayList<String>()
        val statusList: MutableCollection<File>

        if (fromAll) {
            statusList = appUtil.allStatuses
            for (file in statusList) {
                titles.add(appUtil.getStatusTimeLeft(file))
            }
        } else {
            statusList = appUtil.savedStatuses
            for (file in statusList) {
                titles.add(file.name)
            }
        }
        val onSlideCompleteListener = object : PreviewAdapter.OnSlideCompleteListener {
            override fun onSlideComplete(pos: Int) {
                // Toast.makeText(this@PreviewActivity, "Slide Complete", Toast.LENGTH_SHORT).show()
                if (pos < statusList.size - 1) preview_pager.currentItem = pos + 1
            }
        }
        preview_pager.setPageTransformer(true, RotateDownTransformer())
//        pagerAdapter = PreviewAdapter(this, statusList, titles, onSlideCompleteListener)
//        preview_pager.adapter = pagerAdapter
//        preview_pager.offscreenPageLimit = 0
//        preview_pager.setCurrentItem(position)

        PreviewFragment.statusList = statusList
        val fragPagerAdapter = AltPreviewAdapter(supportFragmentManager)
        for (i in 0 until statusList.size) {
            fragPagerAdapter.addFragment(PreviewFragment.newInstance(i))
        }

        preview_pager.adapter = fragPagerAdapter
        preview_pager.currentItem = position
        preview_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(pos: Int) {
                (fragPagerAdapter.getItem(pos) as PreviewFragment).onPreviewReady(pos)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.preview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_share -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
    }

    interface PreviewReadyListener {
        fun onPreviewReady(pos: Int)
    }
}