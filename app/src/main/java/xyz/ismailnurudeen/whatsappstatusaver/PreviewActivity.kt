package xyz.ismailnurudeen.whatsappstatusaver

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import com.ToxicBakery.viewpager.transforms.RotateDownTransformer
import kotlinx.android.synthetic.main.activity_preview.*
import xyz.ismailnurudeen.whatsappstatusaver.adapters.AltPreviewAdapter
import xyz.ismailnurudeen.whatsappstatusaver.fragments.PreviewFragment
import xyz.ismailnurudeen.whatsappstatusaver.utils.AppUtil
import java.io.File
import java.util.*

class PreviewActivity : AppCompatActivity() {
    private val titles = ArrayList<String>()
    lateinit var statusList: MutableCollection<File>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_preview)
        val appUtil: AppUtil
        try {
            appUtil = AppUtil(this)
        } catch (iae: IllegalArgumentException) {
            Toast.makeText(this, "WhatsApp folder could not be found!", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val extras = intent.extras
        val position = extras!!.getInt("POSITION")
        val fromAll = extras.getBoolean("FROM_ALL", true)

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
        val onSlideCompleteListener = object : PreviewFragment.OnSlideCompleteListener {
            override fun onSlideComplete(pos: Int) {
                if (pos < statusList.size - 1) {
                    preview_pager.currentItem = pos + 1
                } else {
                    finish()
                }
            }
        }

        preview_pager.setPageTransformer(true, RotateDownTransformer())
        val fragPagerAdapter = AltPreviewAdapter(supportFragmentManager)
        for (i in 0 until statusList.size) {
            fragPagerAdapter.addFragment(PreviewFragment.newInstance(i, fromAll, onSlideCompleteListener))
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
                val shareTitle = "Share File With"
                ShareCompat.IntentBuilder.from(this)
                        .setChooserTitle(shareTitle)
                        .setType("*/*")
                        .setStream(Uri.fromFile(statusList.elementAt(preview_pager.currentItem)))
                        .startChooser()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    interface PreviewReadyListener {
        fun onPreviewReady(pos: Int)
    }
}