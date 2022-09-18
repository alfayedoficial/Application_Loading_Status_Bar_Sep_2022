package com.alfayedoficial.applicationloadingstatusbar.utilities

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

fun AppCompatActivity.setActivityToolbar(title: String, toolbar: Toolbar, textView: TextView) {
    this.apply {
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        textView.text = title
    }
}

fun Context.getDownloadManager(): DownloadManager = ContextCompat.getSystemService(this, DownloadManager::class.java) as DownloadManager
fun Context.getNotificationManager(): NotificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
