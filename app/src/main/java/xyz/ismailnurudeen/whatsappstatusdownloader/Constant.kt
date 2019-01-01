package xyz.ismailnurudeen.whatsappstatusdownloader

import android.os.Environment

object Constant {
    val whatsAppStatusDir = Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media/.Statuses"
    val appFolder = Environment.getExternalStorageDirectory().absolutePath + "/DownloadedWhatsAppStatuses"
    val TAG="whatsappstatusstealer"
}