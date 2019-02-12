package xyz.ismailnurudeen.whatsappstatusaver

import android.os.Environment

object Constant {
    val whatsAppStatusDir = Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media/.Statuses"
    val appFolder = Environment.getExternalStorageDirectory().absolutePath + "/DownloadedWhatsAppStatuses"
    val hiddenAppFolder = Environment.getExternalStorageDirectory().absolutePath + "/Android/data"
    val TAG = "whatsappstatusstealer"
}

object ResponseStatus {
    val CANCLED: Int = -1
    val FAILED: Int = 0
    val SUCCESSFUL: Int = 1
}