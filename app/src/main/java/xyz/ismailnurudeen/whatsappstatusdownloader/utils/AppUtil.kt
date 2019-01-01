package xyz.ismailnurudeen.whatsappstatusdownloader.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_rename_dialog.view.*
import org.apache.commons.io.FileUtils
import xyz.ismailnurudeen.whatsappstatusdownloader.Constant
import xyz.ismailnurudeen.whatsappstatusdownloader.R
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.concurrent.TimeUnit

class AppUtil(val context: Context) {
    val TAG = "whatsappstatusstealer"
    val allStatuses = FileUtils.listFiles(File(Constant.whatsAppStatusDir), arrayOf("jpg", "png", "jpeg", "mp4"), true)
    val savedStatuses = FileUtils.listFiles(File(Constant.appFolder), arrayOf("jpg", "png", "jpeg", "mp4"), true)
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    @SuppressLint("InflateParams")
    fun renameFileAndDownload(pos: Int): Boolean {
        var isSuccessful = false
        var name = allStatuses!!.elementAt(pos).name
        val useDefaultName = sharedPrefs.getBoolean(context.getString(R.string.use_default_name_key), false)
        if (useDefaultName) {

            //Download the status...
            isSuccessful = downloadStatus(pos, name)
        } else {
            val inputView = LayoutInflater.from(context).inflate(R.layout.layout_rename_dialog, null)
            val dialog = AlertDialog.Builder(context)
                    .setView(inputView)
                    .create()
            dialog.show()

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
                    isSuccessful = downloadStatus(pos, name)
                }
            }
        }
        return isSuccessful
    }

    @SuppressLint("SimpleDateFormat")
    fun downloadStatus(pos: Int, fileName: String): Boolean {
        var isDownloaded = false

        if (allStatuses!!.isNotEmpty() && pos < allStatuses.size) {
            val pathToStoreStatus = if (fileName.contains(".jpg", true) || fileName.contains(".mp4", true)) {
                "${Constant.appFolder}/$fileName"
            } else {
                "${Constant.appFolder}/$fileName.${allStatuses.elementAt(pos).extension}"
            }

            try {
                isDownloaded = copyFile(allStatuses.elementAt(pos), File(pathToStoreStatus))
                if (isDownloaded) Toast.makeText(context, "Status Downloaded Successfully...", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return isDownloaded
    }

    fun downloadAllStatus() {
        AlertDialog.Builder(context)
                .setTitle("Download All")
                .setMessage("Do you want to download all status?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.setPositiveButton("Yes") { _, _ ->
                    FileUtils.copyDirectory(File(Constant.whatsAppStatusDir), File(Constant.appFolder))
                    Toast.makeText(context, "Status Downloaded Succesfully...", Toast.LENGTH_SHORT).show()
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
        val timeLeft = "$hoursDiff hours Left"
        return if (hoursDiff > 0) {
            timeLeft
        } else {
            "few minutes Left"
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

    fun deleteAllFiles(allFiles: MutableCollection<File>) {

        AlertDialog.Builder(context)
                .setTitle("Delete All")
                .setMessage("Do you want to delete all downloaded status?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.setPositiveButton("Yes") { _, _ ->
                    for (file in allFiles) {
                        deleteFile(file)
                    }
                    Toast.makeText(context, "All Downloaded Status Deleted Succesfully...", Toast.LENGTH_SHORT).show()
                }.show()
    }

    fun getApkFile(): File? {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        val apkFile = File(appInfo.publicSourceDir)
        val path = "${context.externalCacheDir}/ExtractedApk/WhatsAppStatusDownloader.apk"
        //  val path = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/ExtractedApk/WhatsAppStatusDownloader.apk"
        val newApkFile = File(path)
        if (copyFile(apkFile, newApkFile)) return newApkFile
        return null
    }

    fun getVideoDuration(videoFile: File): Long {
        val retriver = MediaMetadataRetriever()
        retriver.setDataSource(context, Uri.fromFile(videoFile))
        val time = retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriver.release()

        return time.toLong()
    }
}