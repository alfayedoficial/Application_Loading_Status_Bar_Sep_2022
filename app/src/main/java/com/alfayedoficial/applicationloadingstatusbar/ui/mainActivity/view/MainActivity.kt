package com.alfayedoficial.applicationloadingstatusbar.ui.mainActivity.view

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.DownloadManager.*
import android.app.DownloadManager.COLUMN_STATUS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
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
import com.alfayedoficial.applicationloadingstatusbar.utilities.ButtonState
import com.alfayedoficial.applicationloadingstatusbar.utilities.setActivityToolbar
import com.alfayedoficial.kotlinutils.kuRes

class MainActivity : AppCompatActivity() {

    private var _dataBinder: ActivityMainBinding? = null
    private val dataBinder: ActivityMainBinding
        get() = _dataBinder!!

    private var downloadFileName = ""
    private var downloadID: Long = NO_DOWNLOAD
    private var downloadContentObserver: ContentObserver? = null
    private var downloadNotificator: DownloadNotificationUtils? = null


    companion object {
        var URL = ""
        private const val NO_DOWNLOAD = 0L
        private const val CHANNEL_ID = "channelId"
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
            onLoadingClick()
            registerReceiver(onDownloadCompletedReceiver, IntentFilter(ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun onLoadingClick(){
        dataBinder.mainContent.apply {
            loadingButton.setOnClickListener {
                when (downloadOptionRadioGroup.checkedRadioButtonId) {
                    View.NO_ID -> Toast.makeText(this@MainActivity, "Please select an option to download", Toast.LENGTH_SHORT).show()
                    else ->{
                        downloadFileName = findViewById<RadioButton>(downloadOptionRadioGroup.checkedRadioButtonId).text.toString()
                        selectURL(downloadFileName)
                        onRequestDownloadFile()
                    }
                }
            }
        }
    }

    private fun selectURL(downloadFileName : String)  = when (downloadFileName) {
        getString(R.string.download_using_glide)-> {
            URL = "https://github.com/bumptech/glide/archive/master.zip"
        }
        getString(R.string.download_using_loadapp)-> {
            URL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        }
        else -> {
            URL = "https://github.com/square/retrofit/archive/master.zip"
        }

    }


    private fun onRequestDownloadFile() {
        with(getDownloadManager()){

            val request = Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .also {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        it.setRequiresCharging(false)
                    }
                }

            downloadID = enqueue(request)
            downloadContentObserver()
        }
    }

    private fun DownloadManager.downloadContentObserver() {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadContentObserver?.run {  queryProgress() }
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

    @SuppressLint("Range")
    private fun DownloadManager.queryProgress() {
        query(Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    // check download status
                    val id = getInt(getColumnIndex(COLUMN_ID))
                    when (getInt(getColumnIndex(COLUMN_STATUS))) {
                        STATUS_FAILED -> {
                            Log.i("Test_Download","Download $id: failed")
                            dataBinder.mainContent.loadingButton.changeButtonState(ButtonState.Completed)
                        }
                        STATUS_PAUSED -> {
                            Log.i("Test_Download","Download $id: paused")
                        }
                        STATUS_PENDING -> {
                            Log.i("Test_Download","Download $id: pending")
                        }
                        STATUS_RUNNING -> {
                            Log.i("Test_Download","Download $id: running")
                            dataBinder.mainContent.loadingButton.changeButtonState(ButtonState.Loading)
                        }
                        STATUS_SUCCESSFUL -> {
                            Log.i("Test_Download","Download $id: successful")
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
    private val onDownloadCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                val downloadStatus = getDownloadManager().queryStatus(it)
                Log.i("Test_Download","Download $it completed with status: ${downloadStatus.type}")
                unregisterDownloadContentObserver()
                downloadStatus.takeIf { status -> status != TemplateEnums.DownloadStatus.UNKNOWN }?.run {
                    getDownloadNotificator().notify(downloadFileName, downloadStatus)
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun DownloadManager.queryStatus(id: Long): TemplateEnums.DownloadStatus {
        query(Query().setFilterById(id)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    return when (getInt(getColumnIndex(COLUMN_STATUS))) {
                        STATUS_SUCCESSFUL -> TemplateEnums.DownloadStatus.SUCCESSFUL
                        STATUS_FAILED -> TemplateEnums.DownloadStatus.FAILED
                        else -> TemplateEnums.DownloadStatus.UNKNOWN
                    }
                }
                return TemplateEnums.DownloadStatus.UNKNOWN
            }
        }
    }

    private fun getDownloadNotificator(): DownloadNotificationUtils = when (downloadNotificator) {
        null -> DownloadNotificationUtils(this, lifecycle).also { downloadNotificator = it }
        else -> downloadNotificator!!
    }


}