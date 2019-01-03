package xyz.ismailnurudeen.whatsappstatusdownloader.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
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
import kotlinx.android.synthetic.main.layout_main_all_status.*
import kotlinx.android.synthetic.main.layout_status_empty.*
import kotlinx.android.synthetic.main.layout_status_item.view.*
import xyz.ismailnurudeen.whatsappstatusdownloader.Constant
import xyz.ismailnurudeen.whatsappstatusdownloader.PreviewActivity
import xyz.ismailnurudeen.whatsappstatusdownloader.R
import xyz.ismailnurudeen.whatsappstatusdownloader.adapters.StatusAdapter
import xyz.ismailnurudeen.whatsappstatusdownloader.utils.AppUtil
import java.io.File

class AllStatusFragment : Fragment() {
    var savedStatuses: MutableCollection<File>? = null
    var allStatuses: MutableCollection<File>? = null
    private lateinit var sharedPrefs: SharedPreferences
    lateinit var appUtil: AppUtil
    private var _hasLoadedOnce = false
    val TAG = "whatsappstatusstealer"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUtil = AppUtil(context!!)
        sharedPrefs = context!!.getSharedPreferences("STATUS_PREFS", Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_main_all_status, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        setHasOptionsMenu(true)
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101)) {
            // setupMainTabBadge(activity!!.findViewById(R.id.main_tablayout), 2)
            loadStatuses()
        }
    }

    private fun loadStatuses() {
        val f = File(Constant.appFolder)
        if (!f.exists()) f.mkdir()

        allStatuses = appUtil.allStatuses
        savedStatuses = appUtil.savedStatuses
        setupTabBadge()

        Log.i(TAG, "Status list is ${allStatuses!!.size}")

        val onClick = object : StatusAdapter.OnItemClickListener {
            override fun onItemClick(v: View, pos: Int, which: StatusAdapter.OnItemClickListener.Companion.ITEM_CLICKED_TYPE) {
                when (which) {
                    StatusAdapter.OnItemClickListener.Companion.ITEM_CLICKED_TYPE.ALL -> {
                    }
                    StatusAdapter.OnItemClickListener.Companion.ITEM_CLICKED_TYPE.STATUS_IMAGE -> {
                        val previewIntent = Intent(context, PreviewActivity::class.java)
                        previewIntent.putExtra("POSITION", pos)
                        previewIntent.putExtra("FROM_ALL", true)
                        startActivity(previewIntent)
                    }
                    StatusAdapter.OnItemClickListener.Companion.ITEM_CLICKED_TYPE.DOWNLOAD_BUTTON -> {
                        val responseListener = object : AppUtil.OnUserDialogResponse {
                            override fun onResponse(status: Boolean) {
                                savedStatuses = appUtil.savedStatuses
                                val tab2 = activity?.findViewById<TabLayout>(R.id.main_tablayout)?.getTabAt(1)
                                val badge = tab2?.customView?.findViewById(R.id.badge) as TextView
                                badge.text = "${savedStatuses!!.size}"
                                v.status_download_btn.setColorFilter(context!!.resources.getColor(R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN)
                            }
                        }
                        appUtil.renameFileAndDownload(pos, responseListener)
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

        status_rv.layoutManager = LinearLayoutManager(context)
        if (allStatuses!!.isNotEmpty()) {
            status_rv.visibility = View.VISIBLE
            empty_layout.visibility = View.GONE
            status_rv.adapter = StatusAdapter(context!!, allStatuses!!, onClick, onLongClick)
            if (sharedPrefs.getBoolean("IS_FIRST_LAUNCH_ALL_STATUS", true)) {
                Handler().postDelayed({
                    appUtil.showAllStatusTapTarget(activity!!)
                }, 500)
                sharedPrefs.edit().putBoolean("IS_FIRST_LAUNCH_ALL_STATUS", false).apply()
            }
        } else {
            status_rv.visibility = View.GONE
            empty_layout.visibility = View.VISIBLE
            refresh_btn.setOnClickListener {
                loadStatuses()
            }
            Glide.with(context!!)
                    .load(R.drawable.emoji_question_mark)
                    .into(no_status_iv)
        }
        save_all.setOnClickListener {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101)) {
                appUtil.downloadAllStatus(object : AppUtil.OnUserDialogResponse {
                    override fun onResponse(status: Boolean) {
                        Toast.makeText(context, "All Status Downloaded Successfully...", Toast.LENGTH_SHORT).show()
                        loadStatuses()
                    }

                })
            }
        }
    }

    @SuppressLint("NewApi")
    private fun setupTabBadge() {
        val tab = activity?.findViewById<TabLayout>(R.id.main_tablayout)?.getTabAt(0) ?: return
        val tabText = tab.customView?.findViewById(android.R.id.text1) as TextView
        val badge = tab.customView?.findViewById(R.id.badge) as TextView
        badge.text = "${allStatuses!!.size}"

        tabText.setTextColor(Color.WHITE)
        badge.backgroundTintList = ContextCompat.getColorStateList(context!!, android.R.color.white)
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

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (this.isVisible) {
            if (isVisibleToUser && _hasLoadedOnce) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101)) {
                    loadStatuses()
                }
            }
            _hasLoadedOnce = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.all_status_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_help -> appUtil.showAllStatusTapTarget(activity!!)
        }
        return false
    }

}