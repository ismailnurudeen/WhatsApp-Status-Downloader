package xyz.ismailnurudeen.whatsappstatusdownloader

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.about_dialog_layout.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_no_permission.*
import xyz.ismailnurudeen.whatsappstatusdownloader.adapters.MainViewPagerAdapter
import xyz.ismailnurudeen.whatsappstatusdownloader.utils.AppUtil
import java.io.File
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    private val TAG = "whatsappstatusstealer"
    lateinit var headerFont: Typeface
    lateinit var bodyFont: Typeface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        headerFont = Typeface.createFromAsset(assets, "fonts/HelveticaNeueBd.ttf")
        bodyFont = Typeface.createFromAsset(assets, "fonts/HelveticaNeueLt.ttf")

        setSupportActionBar(main_toolbar)

        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101)) {
            //Create Neccessary app folders
            setUpAppDirectories()

            try {
                AppUtil(this).allStatuses
            } catch (iae: IllegalArgumentException) {
                //Log.e(TAG, iae.message)
                displayNoPermissionView(false)
                return
            }
            setupMainViewPager()
        }

        main_adView.loadAd(AdRequest.Builder()
                .addTestDevice("EE61FFC39B2F91254A201499649C0082")
                .build())
        main_adView.adListener = object : AdListener() {
            override fun onAdOpened() {
                //TODO:Add margin bottom to View pager
            }

            override fun onAdClosed() {
                //TODO:Add Remove margin bottom from View pager
            }
        }
    }

    private fun setupMainViewPager() {
        no_permission_view.visibility = View.GONE
        main_tablayout.visibility = View.VISIBLE
        main_viewpager.visibility = View.VISIBLE

        main_viewpager.adapter = MainViewPagerAdapter(supportFragmentManager)
        main_tablayout.setupWithViewPager(main_viewpager)
        setupTabBadge(main_tablayout, 2)

        main_tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val tabText = tab?.customView?.findViewById(android.R.id.text1) as TextView
                val badge = tab.customView?.findViewById(R.id.badge) as TextView

                tabText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.unselected_tab_color))
                try {
                    AppUtil(this@MainActivity).setBackgroundTint(badge, R.color.unselected_tab_color)
                } catch (e: Exception) {
                    //e.printStackTrace()
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabText = tab?.customView?.findViewById(android.R.id.text1) as TextView
                val badge = tab.customView?.findViewById(R.id.badge) as TextView

                tabText.setTextColor(Color.WHITE)
                try {
                    AppUtil(this@MainActivity).setBackgroundTint(badge, android.R.color.white)
                } catch (e: Exception) {
                }
            }
        })
    }

    private fun setupTabBadge(tablayout: TabLayout, length: Int) {
        try {
            val aUtils = AppUtil(this)
            val count = arrayOf(aUtils.allStatuses.size, aUtils.savedStatuses.size)
            for (i in 0 until length) {
                val tab = tablayout.getTabAt(i)
                tab?.setCustomView(R.layout.custom_tab_view)
                val tabText = tab?.customView?.findViewById(android.R.id.text1) as TextView
                tabText.typeface = headerFont

                if (tab.customView != null) {
                    val badge = tab.customView?.findViewById(R.id.badge) as TextView
                    badge.text = "${count[i]}"
                    badge.typeface = headerFont
                    if (i == 0) {
                        tabText.setTextColor(Color.WHITE)
                        AppUtil(this@MainActivity).setBackgroundTint(badge, android.R.color.white)
                    }
                }
            }
        } catch (iae: IllegalArgumentException) {
            displayNoPermissionView(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.menu_share -> {
                val downloadLink = "Download this awesome app and save all your favourite whatsapp status easily\n" +
                        "PlayStore Link: ${getString(R.string.app_playstore_link_template)}$packageName"
                val shareTitle = "Share this app"
                val shareIntent = ShareCompat.IntentBuilder.from(this)
                shareIntent.setChooserTitle(shareTitle)
                        .setType("*/*")
                        .setText(downloadLink)
                val apkFile = AppUtil(this).getApkFile()
                if (apkFile != null) shareIntent.setStream(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", apkFile))
                shareIntent.startChooser()
            }
            R.id.menu_about -> {
                showAboutDialog()
            }
            R.id.menu_feedback -> {
                rateThisApp()
            }
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    private fun showAboutDialog() {
        val v = LayoutInflater.from(this).inflate(R.layout.about_dialog_layout, null)
        val builder = AlertDialog.Builder(this)
                .setView(v)
        val dialog = builder.create()
        val p = Pattern.compile("here")
        Linkify.addLinks(v.about_me, p, null, null) { _, _ ->
            getString(R.string.my_website)
        }
        v.version_code.text = "v${BuildConfig.VERSION_NAME}"
        v.ok_btn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun rateThisApp() {
        val uri = Uri.parse("getString(R.string.market_id_template)$packageName")
        val rateIntent = Intent(Intent.ACTION_VIEW, uri)
        rateIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY.or(Intent.FLAG_ACTIVITY_CLEAR_TASK).or(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(rateIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${getString(R.string.app_playstore_link_template)}$packageName")))
        }
    }

    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                            permission) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted")
                return true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
                Log.v(TAG, "Permission is revoked")
                return false
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0])
                setupMainViewPager()
            } else {
                displayNoPermissionView()
            }
        }
    }

    fun setUpAppDirectories() {
        val visibleFolder = File(Constant.appFolder)
        val invisibleFolder = File("${Constant.hiddenAppFolder}/$packageName/.DownloadedWhatsAppStatuses")
        if (!visibleFolder.exists()) visibleFolder.mkdir()
        if (!invisibleFolder.exists()) invisibleFolder.mkdir()
    }

    fun displayNoPermissionView(isPermission: Boolean = true) {
        no_permission_view.visibility = View.VISIBLE
        main_tablayout.visibility = View.GONE
        Glide.with(this)
                .load(R.drawable.emoji_head_scratch)
                .into(no_permission_iv)
        if (isPermission) {
            permission_btn.visibility = View.VISIBLE
            download_whatsApp_btn.visibility = View.GONE
            no_permission_tv.text = getString(R.string.no_permission_txt)
            permission_btn.setOnClickListener {
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101)
            }
        } else {
            no_permission_tv.text = getString(R.string.no_whatsapp_txt)
            permission_btn.visibility = View.GONE
            download_whatsApp_btn.visibility = View.VISIBLE
            download_whatsApp_btn.setOnClickListener {
                val uri = Uri.parse("getString(R.string.market_id_template)com.whatsapp")
                val rateIntent = Intent(Intent.ACTION_VIEW, uri)
                rateIntent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY.or(Intent.FLAG_ACTIVITY_CLEAR_TASK).or(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(rateIntent)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${getString(R.string.app_playstore_link_template)}com.whatsapp")))
                }
            }
        }
    }

}
