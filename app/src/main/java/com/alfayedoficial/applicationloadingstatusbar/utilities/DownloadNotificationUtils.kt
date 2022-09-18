package com.alfayedoficial.applicationloadingstatusbar.utilities

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.alfayedoficial.applicationloadingstatusbar.R
import com.alfayedoficial.kotlinutils.kuToast

class DownloadNotificationUtils(private val context: Context, private val lifecycle: Lifecycle) :
    LifecycleObserver {

    init {
        lifecycle.addObserver(this).also {
            Log.d("Tag_Download","Notificator added as a Lifecycle Observer")
        }
    }

    fun notify(fileName: String, downloadStatus: TemplateEnums.DownloadStatus, ) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Log.d("Tag_Download","Notifying with a Toast. Lifecycle is resumed")
            context.kuToast(context.getString(R.string.download_completed))
        }
        with(context.applicationContext) {
            getNotificationManager().run {
                createDownloadStatusChannel(applicationContext)
                sendDownloadCompletedNotification(
                    fileName,
                    downloadStatus,
                    applicationContext
                )
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unregisterObserver() = lifecycle.removeObserver(this)
        .also { Log.d("Tag_Download","Notificator removed from Lifecycle Observers") }
}