package com.alfayedoficial.applicationloadingstatusbar.ui.mainActivity.view

import android.app.DownloadManager
import android.app.DownloadManager.COLUMN_STATUS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.alfayedoficial.applicationloadingstatusbar.R
import com.alfayedoficial.applicationloadingstatusbar.databinding.ActivityMainBinding
import com.alfayedoficial.applicationloadingstatusbar.utilities.DownloadNotificationUtils
import com.alfayedoficial.applicationloadingstatusbar.utilities.TemplateEnums
import com.alfayedoficial.applicationloadingstatusbar.utilities.getDownloadManager
import com.alfayedoficial.applicationloadingstatusbar.utilities.loading.ButtonState
import com.alfayedoficial.applicationloadingstatusbar.utilities.setActivityToolbar
import com.alfayedoficial.kotlinutils.kuRes

class MainActivity : AppCompatActivity() {

    private var _dataBinder: ActivityMainBinding? = null
    private val dataBinder: ActivityMainBinding
        get() = _dataBinder!!

    private var downloadFileName = ""
    private var downloadContentObserver: ContentObserver? = null
    private var downloadNotificator: DownloadNotificationUtils? = null
    private var downloadID: Long = NO_DOWNLOAD


    companion object {
        private const val URL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val NO_DOWNLOAD = 0L
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _dataBinder = DataBindingUtil.setContentView(this@MainActivity , R.layout.activity_main)
        onActivityCreated()
    }

    private fun onActivityCreated(){
        dataBinder.apply {
            lifecycleOwner = this@MainActivity
            activity = this@MainActivity
            mainToolbar.apply { setActivityToolbar(kuRes.getString(R.string.app_name), toolbar, tvNameToolbar) }
            onLoadingButtonClicked()
        }
    }

    private fun ActivityMainBinding.onLoadingButtonClicked() {
        with(mainContent) {
            loadingButton.setOnClickListener {
                when (downloadOptionRadioGroup.checkedRadioButtonId) {
                    View.NO_ID ->
                        Toast.makeText(this@MainActivity, "Please select the file to download", Toast.LENGTH_SHORT).show()
                    else -> {
                        downloadFileName = findViewById<RadioButton>(downloadOptionRadioGroup.checkedRadioButtonId).text.toString()
                        requestDownload()
                    }
                }
            }
        }
    }

    private val onDownloadCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)?.apply {
                val downloadStatus = getDownloadManager().queryStatus(this)
                unregisterDownloadContentObserver()
                downloadStatus.takeIf { status -> status != TemplateEnums.DownloadStatus.UNKNOWN }?.run {
                    getDownloadNotificator().notify(downloadFileName, downloadStatus)
                }
            }
        }
    }

    private fun getDownloadNotificator(): DownloadNotificationUtils = when (downloadNotificator) {
        null -> DownloadNotificationUtils(this, lifecycle).also { downloadNotificator = it }
        else -> downloadNotificator!!
    }

    private fun DownloadManager.queryStatus(id: Long): TemplateEnums.DownloadStatus {
        query(DownloadManager.Query().setFilterById(id)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    return when (getColumnIndex(COLUMN_STATUS)) {
                        DownloadManager.STATUS_SUCCESSFUL -> TemplateEnums.DownloadStatus.SUCCESSFUL
                        DownloadManager.STATUS_FAILED -> TemplateEnums.DownloadStatus.FAILED
                        else -> TemplateEnums.DownloadStatus.UNKNOWN
                    }
                }
                return TemplateEnums.DownloadStatus.UNKNOWN
            }
        }
    }

    private fun requestDownload() {
        with(getDownloadManager()) {
            downloadID.takeIf { it != NO_DOWNLOAD }?.run {
                val downloadsCancelled = remove(downloadID)
                unregisterDownloadContentObserver()
                downloadID = NO_DOWNLOAD
            }

            val request = DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .also {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        it.setRequiresCharging(false)
                    }
                }
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            downloadID = enqueue(request)

            createAndRegisterDownloadContentObserver()
        }
    }

    private fun DownloadManager.createAndRegisterDownloadContentObserver() {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadContentObserver?.run { queryProgress() }
            }
        }.also {
            downloadContentObserver = it
            contentResolver.registerContentObserver(
                "content://downloads/my_downloads".toUri(),
                true,
                downloadContentObserver!!
            )
        }
    }

    private fun DownloadManager.queryProgress() {
        query(DownloadManager.Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    val id = getColumnIndex(DownloadManager.COLUMN_ID)
                    when (getColumnIndex(COLUMN_STATUS)) {
                        DownloadManager.STATUS_FAILED -> {
                            Log.d("TAGG","Download $id: failed")
                            dataBinder.mainContent.loadingButton.changeButtonState(ButtonState.Completed)
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            Log.d("TAGG","Download $id: paused")
                        }
                        DownloadManager.STATUS_PENDING -> {
                            Log.d("TAGG","Download $id: pending")
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            Log.d("TAGG","Download $id: running")
                            dataBinder.mainContent.loadingButton.changeButtonState(ButtonState.Loading)
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            Log.d("TAGG","Download $id: successful")
                            dataBinder.mainContent.loadingButton.changeButtonState(ButtonState.Completed)
                        }
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadCompletedReceiver)
        unregisterDownloadContentObserver()
        downloadNotificator = null
    }

    private fun unregisterDownloadContentObserver() {
        downloadContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            downloadContentObserver = null
        }
    }


}