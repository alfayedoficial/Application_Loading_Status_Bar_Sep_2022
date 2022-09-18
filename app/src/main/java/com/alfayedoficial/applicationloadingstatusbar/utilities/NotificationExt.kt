package com.alfayedoficial.applicationloadingstatusbar.utilities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alfayedoficial.applicationloadingstatusbar.R
import com.alfayedoficial.applicationloadingstatusbar.ui.detailsActivity.view.DetailsActivity
import com.alfayedoficial.applicationloadingstatusbar.ui.detailsActivity.view.DetailsActivity.Companion.bundleExtrasOf
import com.alfayedoficial.applicationloadingstatusbar.utilities.Constants.DOWNLOAD_COMPLETED_ID
import com.alfayedoficial.applicationloadingstatusbar.utilities.Constants.NOTIFICATION_REQUEST_CODE
import com.alfayedoficial.applicationloadingstatusbar.utilities.Constants.notification_channel_description
import com.alfayedoficial.applicationloadingstatusbar.utilities.Constants.notification_channel_id
import com.alfayedoficial.applicationloadingstatusbar.utilities.Constants.notification_channel_name


/**
 * Builds and delivers the notification.
 *
 * @param context activity context.
 */
fun NotificationManager.sendDownloadCompletedNotification(
    fileName: String,
    downloadStatus: TemplateEnums.DownloadStatus,
    context: Context
) {
    // https://developer.android.com/training/notify-user/build-notification#click
    val contentIntent = Intent(context, DetailsActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtras(bundleExtrasOf(fileName, downloadStatus))
    }
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_REQUEST_CODE,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // https://developer.android.com/training/notify-user/build-notification#Actions
    val checkStatusAction = NotificationCompat.Action.Builder(
        null,
        context.getString(R.string.notification_action_check_status),
        contentPendingIntent
    ).build()

    // TODO: Further improvement: Add a progress bar to keep track of Download progress
    // https://developer.android.com/training/notify-user/build-notification#progressbar

    NotificationCompat.Builder(context, notification_channel_id) // Set the notification content
        // https://developer.android.com/training/notify-user/build-notification#builder
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(context.getString(R.string.notification_description))
        // priority determines how intrusive the notification should be on Android 7.1 and lower.
        // (For Android 8.0 and higher, you must instead set the channel importance—shown in the next section.)
            // High priority makes a sound and appears as a heads up notification
            // Default priority makes a sound
            // Low priority makes no sound
            // Min priority makes no sound and does not appear in the status bar
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        // Set the notification's tap action
        // https://developer.android.com/training/notify-user/build-notification#click
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        // Add action buttons
        // https://developer.android.com/training/notify-user/build-notification#Actions
        .addAction(checkStatusAction)
        // Set lock screen visibility
        // https://developer.android.com/training/notify-user/build-notification#lockscreenNotification
            // VISIBILITY_PUBLIC shows the notification's full content
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .apply {

        }.run {
            notify(DOWNLOAD_COMPLETED_ID, this.build())
        }
}

/**
 * Because you must create the notification channel before posting any notifications on
 * Android 8.0 and higher, you should execute this code as soon as your app starts.
 * It's safe to call this repeatedly because creating an existing notification channel
 * performs no operation.
 *
 * **See also:** [Create a channel and set the importance][https://developer.android.com/training/notify-user/build-notification#Priority]
 */
@SuppressLint("NewApi")
fun NotificationManager.createDownloadStatusChannel(context: Context) {
    Build.VERSION.SDK_INT.takeIf { it >= Build.VERSION_CODES.O }?.run {
        NotificationChannel(
            notification_channel_id,
            notification_channel_name,
            // This parameter determines how to interrupt the user for any notification that
            // belongs to this channel—though you must also set the priority with
            // NotificationCompat.Builder.setPriority()
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = notification_channel_description
            setShowBadge(true)
        }.run {
            createNotificationChannel(this)
        }
    }
}