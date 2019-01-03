package xyz.ismailnurudeen.whatsappstatusdownloader.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_main_downloaded_status.*
import kotlinx.android.synthetic.main.layout_status_empty.*
import xyz.ismailnurudeen.whatsappstatusdownloader.Constant
import xyz.ismailnurudeen.whatsappstatusdownloader.PreviewActivity
import xyz.ismailnurudeen.whatsappstatusdownloader.R
import xyz.ismailnurudeen.whatsappstatusdownloader.adapters.StatusAdapter
import xyz.ismailnurudeen.whatsappstatusdownloader.utils.AppUtil
import java.io.File

class DownloadedStatusFragment : Fragment() {
    var savedStatuses: MutableCollection<File>? = null
    val TAG = "whatsappstatusstealer"
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var appUtil: AppUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = context!!.getSharedPreferences("STATUS_PREFS", Context.MODE_PRIVATE)
        appUtil = AppUtil(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.layout_main_downloaded_status, container, false)
    }

    fun loadStatuses() {
        val f = File(Constant.appFolder)
        if (!f.exists()) f.mkdir()
        appUtil = AppUtil(context!!)
        savedStatuses = appUtil.savedStatuses
        setupTabBadge()

        Log.i(TAG, "Downloaded Status list is ${savedStatuses!!.size}")

        val onClick = object : StatusAdapter.OnItemClickListener {
            override fun onItemClick(v: View, pos: Int, which: StatusAdapter.OnItemClickListener.Companion.ITEM_CLICKED_TYPE) {
                when (which) {
                    StatusAdapter.OnItemClickListener.Companion.ITEM_CLICKED_TYPE.ALL -> {
                    }
                    StatusAdapter.OnItemClickListener.Companion.ITEM_CLICKED_TYPE.STATUS_IMAGE -> {
                        val previewIntent = Intent(context, PreviewActivity::class.java)
                        previewIntent.putExtra("POSITION", pos)
                        previewIntent.putExtra("FROM_ALL", false)
                        startActivity(previewIntent)
                    }
                    StatusAdapter.OnItemClickListener.Companion.ITEM_CLICKED_TYPE.DELETE_BUTTON -> {
                        if (appUtil.deleteFile(savedStatuses!!.elementAt(pos))) {
                            savedStatuses!!.remove(savedStatuses!!.elementAt(pos))
                            downloaded_status_rv.adapter!!.notifyDataSetChanged()
                            setupTabBadge()
                            if (savedStatuses!!.isEmpty()) loadStatuses()
                            Toast.makeText(context, "Status Deleted...", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                    }
                }
            }

        }
        val onLongClick = object : StatusAdapter.OnItemLongClickListener {
            override fun onItemLongClick(view: View?, pos: Int) {
            }
        }
        downloaded_status_rv.layoutManager = LinearLayoutManager(context)
        if (savedStatuses!!.isNotEmpty()) {
            downloaded_status_rv.visibility = View.VISIBLE
            empty_layout.visibility = View.GONE
            val adapter = StatusAdapter(context!!, savedStatuses!!, onClick, onLongClick, false)
            downloaded_status_rv.adapter = adapter
            if (sharedPrefs.getBoolean("IS_FIRST_LAUNCH_DOWNLOADED_STATUS", true)) {
                Handler().postDelayed({
                    appUtil.showSavedStatusTapTarget(activity!!)
                }, 500)
                sharedPrefs.edit().putBoolean("IS_FIRST_LAUNCH_DOWNLOADED_STATUS", false).apply()
            }
            delete_all_downloads.setOnClickListener {
                val responseListener = object : AppUtil.OnUserDialogResponse {
                    override fun onResponse(status: Boolean) {
                        savedStatuses!!.clear()
                        Toast.makeText(context, "All Downloaded Status Deleted Successfully...", Toast.LENGTH_SHORT).show()
                        loadStatuses()
                    }
                }
                appUtil.deleteAllFiles(responseListener)

            }
        } else {
            downloaded_status_rv.visibility = View.GONE
            empty_layout.visibility = View.VISIBLE
            empty_view_tv.text = context!!.getString(R.string.no_downloaded_status_txt)
            refresh_btn.setOnClickListener {
                loadStatuses()
            }
            Glide.with(context!!)
                    .load(R.drawable.emoji_question_mark)
                    .into(no_status_iv)
        }
    }

    @SuppressLint("NewApi")
    private fun setupTabBadge() {
        val tab = activity?.findViewById<TabLayout>(R.id.main_tablayout)?.getTabAt(1)
        val badge = tab?.customView?.findViewById(R.id.badge) as TextView
        badge.text = "${savedStatuses!!.size}"
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (this.isVisible) {
            if (isVisibleToUser) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101)) {
                    loadStatuses()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.all_status_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_help -> {
                if (savedStatuses!!.isNotEmpty()) {
                    appUtil.showSavedStatusTapTarget(activity!!)
                } else {
                    Toast.makeText(context!!, "<-- Download a status from the ALL STATUS tab", Toast.LENGTH_LONG).show()
                }
            }
        }
        return false
    }

    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(context!!,
                            permission) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted")
                return true
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)) {
                    Toast.makeText(activity, "We need this permission for the app to work properly", Toast.LENGTH_LONG).show()
                    ActivityCompat.requestPermissions(activity!!, arrayOf(permission), requestCode)
                } else {
                    ActivityCompat.requestPermissions(activity!!, arrayOf(permission), requestCode)
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
}