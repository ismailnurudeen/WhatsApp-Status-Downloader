package xyz.ismailnurudeen.whatsappstatusdownloader.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import kotlinx.android.synthetic.main.layout_main_all_status.*
import kotlinx.android.synthetic.main.layout_main_downloaded_status.*
import kotlinx.android.synthetic.main.layout_rename_dialog.view.*
import kotlinx.android.synthetic.main.layout_status_item.view.*
import org.apache.commons.io.FileUtils
import xyz.ismailnurudeen.whatsappstatusdownloader.Constant
import xyz.ismailnurudeen.whatsappstatusdownloader.R
import xyz.ismailnurudeen.whatsappstatusdownloader.ResponseStatus
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.concurrent.TimeUnit

class AppUtil(val context: Context) {
    val TAG = "whatsappstatusstealer"

    val allStatuses: MutableCollection<File> = try {
        FileUtils.listFiles(File(Constant.whatsAppStatusDir), arrayOf("jpg", "png", "jpeg", "mp4"), true)!!
    } catch (iae: IllegalArgumentException) {
        throw IllegalArgumentException()
    }

    val savedStatuses: MutableCollection<File> = try {
        FileUtils.listFiles(File(Constant.appFolder), arrayOf("jpg", "png", "jpeg", "mp4"), true)!!
    } catch (iae: IllegalArgumentException) {
        File(Constant.appFolder).mkdir()
        FileUtils.listFiles(File(Constant.appFolder), arrayOf("jpg", "png", "jpeg", "mp4"), true)!!
    }

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)!!

    @SuppressLint("InflateParams")
    fun renameFileAndDownload(pos: Int, listener: OnUserDialogResponse) {
        var name = allStatuses.elementAt(pos).name
        val useDefaultName = sharedPrefs.getBoolean(context.getString(R.string.use_default_name_key), false)
        if (useDefaultName) {

            //Download the status...
            val response = if (downloadStatus(pos, name)) {
                ResponseStatus.SUCCESSFUL
            } else {
                ResponseStatus.FAILED
            }
            listener.onResponse(response)

        } else {
            val inputView = LayoutInflater.from(context).inflate(R.layout.layout_rename_dialog, null)
            val dialog = AlertDialog.Builder(context)
                    .setView(inputView)
                    .create()
            dialog.show()
            listener.onResponse(ResponseStatus.CANCLED)
            inputView.rename_dialog_input.setText(name)
            inputView.rename_dialog_cancel.setOnClickListener {
                dialog.dismiss()
            }
            inputView.rename_dialog_rename.setOnClickListener {
                val new_name = inputView.rename_dialog_input.text.toString()
                if (new_name.isNotEmpty()) {
                    name = new_name
                    dialog.dismiss()
                    val prefs = context.getSharedPreferences(context.getString(R.string.shared_prefs_name), Context.MODE_PRIVATE).edit()
                    prefs.putBoolean(context.getString(R.string.use_default_name_key), inputView.rename_dialog_checkbox.isChecked).apply()

                    Log.i(TAG, "File Renamed...")
                    //Download the status...
                    val response = if (downloadStatus(pos, name)) {
                        ResponseStatus.SUCCESSFUL
                    } else {
                        ResponseStatus.FAILED
                    }
                    listener.onResponse(response)
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun downloadStatus(pos: Int, fileName: String): Boolean {
        var isDownloaded = false

        if (allStatuses.isNotEmpty() && pos < allStatuses.size) {
            val pathToStoreStatus = if (fileName.contains(".jpg", true) || fileName.contains(".mp4", true)) {
                "${Constant.appFolder}/$fileName"
            } else {
                "${Constant.appFolder}/$fileName.${allStatuses.elementAt(pos).extension}"
            }

            try {
                isDownloaded = copyFile(allStatuses.elementAt(pos), File(pathToStoreStatus))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return isDownloaded
    }

    fun downloadAllStatus(listener: OnUserDialogResponse) {
        AlertDialog.Builder(context)
                .setTitle("Download All")
                .setMessage("Do you want to download all status?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    listener.onResponse()
                }.setPositiveButton("Yes") { _, _ ->
                    FileUtils.copyDirectory(File(Constant.whatsAppStatusDir), File(Constant.appFolder))
                    listener.onResponse()
                }.show()
    }

    fun getStatusTimeLeft(file: File): String {
        val creationTimeMilliSec: Long
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
            creationTimeMilliSec = fileAttributes.creationTime().to(TimeUnit.MILLISECONDS)
        } else {
            creationTimeMilliSec = file.lastModified()
        }
        val creationTime = Date().time - creationTimeMilliSec
        val seconds = creationTime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val hoursDiff = 24 - hours
        val minsDiff = 3600 - minutes
        val timeLeft = "$hoursDiff hours"
        return if (hoursDiff > 0) {
            timeLeft
        } else {
            "few minutes"
        }
    }

    @Throws(IOException::class)
    private fun copyFile(source: File, destination: File): Boolean {
        val isCopied: Boolean

//        //  Delete File if it already exist
//        for (file in savedStatuses!!) {
//            if (FileUtils.contentEquals(source, file)) {
//                deleteFile(file)
//            }
//        }

        if (!FileUtils.contentEquals(source, destination)) {
            FileUtils.copyFile(source, destination)
            isCopied = true
        } else {
            Log.i(TAG, "File already exits...")
            isCopied = false
        }
        return isCopied
    }

    fun deleteFile(file: File): Boolean {
        return file.delete()
    }

    fun deleteAllFiles(listener: AppUtil.OnUserDialogResponse) {
        AlertDialog.Builder(context)
                .setTitle("Delete All")
                .setMessage("Do you want to delete all downloaded status?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.setPositiveButton("Yes") { _, _ ->
                    FileUtils.cleanDirectory(File(Constant.appFolder + "/"))
                    listener.onResponse()
                }.show()
    }

    fun getApkFile(): File? {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        val apkFile = File(appInfo.publicSourceDir)
        val path = "${context.externalCacheDir}/ExtractedApk/WhatsAppStatusDownloader.apk"
        val newApkFile = File(path)
        if (copyFile(apkFile, newApkFile)) return newApkFile
        return null
    }

    fun getVideoDuration(videoFile: File): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.fromFile(videoFile))
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()

        return time.toLong()
    }

    fun getRvFirstItem(rv: RecyclerView): View {
        var view: View? = null
        val lm = rv.layoutManager
        if (lm is LinearLayoutManager) {
            val index = lm.findFirstCompletelyVisibleItemPosition()
            view = lm.findViewByPosition(index)!!
        }
        return view!!
    }

    fun showAllStatusTapTarget(activity: Activity) {
        val helpSequence = TapTargetSequence(activity)
        val itemView = getRvFirstItem(activity.status_rv)
        val toolbar = activity.findViewById<Toolbar>(R.id.main_toolbar)

        helpSequence.target(TapTarget.forView(itemView.status_download_btn, "Download Status", "Use this button to download a status")
                .tintTarget(true)
                .descriptionTextColorInt(Color.WHITE)
                .id(0))
                .target(TapTarget.forView(itemView.status_image, "Preview Status", "Click here to preview a status")
                        .descriptionTextColorInt(Color.WHITE)
                        .transparentTarget(true)
                        .id(1))
                .target(TapTarget.forView(activity.save_all, "Download All Status", "Use this button to download all status")
                        .transparentTarget(true)
                        .descriptionTextColorInt(Color.WHITE)
                        .id(2))
        try {
            helpSequence.target(TapTarget.forToolbarMenuItem(
                    toolbar, R.id.menu_share,
                    "Share",
                    "Click this icon to share the app with friends and family"
            )
                    .tintTarget(true)
                    .descriptionTextColorInt(Color.WHITE)
                    .id(3))
        } catch (e: Exception) {
            //Do nothing...
        }
        helpSequence.target(TapTarget.forToolbarOverflow(toolbar,
                "More Options and Customization",
                "Click this icon to see more options like; Settings,About and Help")
                .tintTarget(true)
                .descriptionTextColorInt(Color.WHITE)
                .id(4))
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    }

                    override fun onSequenceFinish() {
                        Toast.makeText(context, "I hope that was helpful :)", Toast.LENGTH_LONG).show()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                    }

                }).continueOnCancel(true)
                .start()
    }

    fun showSavedStatusTapTarget(activity: Activity) {
        val helpSequence = TapTargetSequence(activity)
        val itemView = getRvFirstItem(activity.downloaded_status_rv)
        helpSequence.target(TapTarget.forView(itemView.status_download_btn, "Delete Status", "Use this button to delete a status")
                .tintTarget(true)
                .descriptionTextColorInt(Color.WHITE)
                .id(0))
                .target(TapTarget.forView(activity.delete_all_downloads, "Delete All Status", "Use this button to delete all status")
                        .transparentTarget(true)
                        .descriptionTextColorInt(Color.WHITE)
                        .id(1))
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    }

                    override fun onSequenceFinish() {
                        Toast.makeText(context, "I hope that was helpful :)", Toast.LENGTH_LONG).show()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                    }

                })
                .start()
    }

    fun setBackgroundTint(v: View, color: Int) {
        val tint = ContextCompat.getColorStateList(context, color)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            v.backgroundTintList = tint
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            v.background.setColorFilter(context.resources.getColor(color), PorterDuff.Mode.MULTIPLY)
        } else {
            ViewCompat.setBackgroundTintList(v, tint)
        }
    }

    interface OnUserDialogResponse {
        fun onResponse(status: Int = ResponseStatus.SUCCESSFUL)
    }
}