package xyz.ismailnurudeen.whatsappstatusdownloader

import android.Manifest
import android.annotation.SuppressLint
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
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_no_permission.*
import xyz.ismailnurudeen.whatsappstatusdownloader.adapters.MainViewPagerAdapter
import xyz.ismailnurudeen.whatsappstatusdownloader.utils.AppUtil


class MainActivity : AppCompatActivity() {
    val TAG = "whatsappstatusstealer"
    var headerFont: Typeface? = null
    var bodyFont: Typeface? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_name), MODE_PRIVATE)
//        val prefsEdit = sharedPrefs.edit()
//        prefsEdit.putBoolean("ALT_USE_DEFAULT", true).apply()
//        val shpWorking = if (sharedPrefs.getBoolean("ALT_USE_DEFAULT", false)) {
//            "SharedPreferences Working...."
//        } else {
//            "Not Working...."
//        }
//        Toast.makeText(this, shpWorking, Toast.LENGTH_SHORT).show()

        headerFont = Typeface.createFromAsset(assets, "fonts/Helvetica Neu Bold.ttf")
        bodyFont = Typeface.createFromAsset(assets, "fonts/HelveticaNeue Medium.ttf")

        setSupportActionBar(main_toolbar)
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101)) {
            setupMainViewPager()
        }

        main_adView.loadAd(AdRequest.Builder().build())
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
        main_viewpager.adapter = MainViewPagerAdapter(this, supportFragmentManager)
        setupTabBadge(main_tablayout, 2)
        main_tablayout.setupWithViewPager(main_viewpager)
        main_tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            @SuppressLint("NewApi")
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val tabText = tab?.customView?.findViewById(android.R.id.text1) as TextView
                val badge = tab.customView?.findViewById(R.id.badge) as TextView

                tabText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.unselected_tab_color))
                badge.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.unselected_tab_color)
            }

            @SuppressLint("NewApi")
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabText = tab?.customView?.findViewById(android.R.id.text1) as TextView
                val badge = tab.customView?.findViewById(R.id.badge) as TextView

                tabText.setTextColor(Color.WHITE)
                badge.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, android.R.color.white)
            }
        })
    }

    private fun setupTabBadge(tablayout: TabLayout, length: Int) {
        val aUtils = AppUtil(this)
        val count = arrayOf(aUtils.allStatuses.size, aUtils.savedStatuses.size)
        for (i in 0 until length) {
            val tab = tablayout.getTabAt(i)
            tab!!.setCustomView(R.layout.custom_tab_view)
            val tabText = tab.customView?.findViewById(android.R.id.text1) as TextView
            tabText.typeface = headerFont
            if (tab.customView != null) {
                val badge = tab.customView?.findViewById(R.id.badge) as TextView
                badge.text = "${count[i]}"
                badge.typeface = headerFont
            }
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
                val downloadLink = "Download this awesome app and save all your favourite whatsapp status easily playstore/myapp_link"
                val shareTitle = "Share this app"
                ShareCompat.IntentBuilder.from(this)
                        .setChooserTitle(shareTitle)
                        .setType("text/plain")
                        .setText(downloadLink).startChooser()
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setType("*/*")
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(AppUtil(this).getApkFile()))
                shareIntent.putExtra(Intent.EXTRA_TEXT, downloadLink)
//                startActivity(Intent.createChooser(shareIntent, shareTitle))
            }
            R.id.menu_help -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                            permission) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted")
                return true
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
                }
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
                no_permission_view.visibility = View.VISIBLE
                main_tablayout.visibility = View.GONE
                Glide.with(this)
                        .load(R.drawable.emoji_head_scratch)
                        .into(no_permission_iv)
                permission_btn.setOnClickListener {
                    checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101)
                }
            }
        }
    }
}
