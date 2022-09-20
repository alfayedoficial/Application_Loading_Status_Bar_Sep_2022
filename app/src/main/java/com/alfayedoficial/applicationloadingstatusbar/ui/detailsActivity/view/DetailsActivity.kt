package com.alfayedoficial.applicationloadingstatusbar.ui.detailsActivity.view

import android.app.DownloadManager
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.alfayedoficial.applicationloadingstatusbar.BuildConfig
import com.alfayedoficial.applicationloadingstatusbar.R
import com.alfayedoficial.applicationloadingstatusbar.databinding.ActivityDetailsBinding
import com.alfayedoficial.applicationloadingstatusbar.databinding.ActivityMainBinding
import com.alfayedoficial.applicationloadingstatusbar.utilities.TemplateEnums
import com.alfayedoficial.applicationloadingstatusbar.utilities.changeDownloadStatusColorTo
import com.alfayedoficial.applicationloadingstatusbar.utilities.changeDownloadStatusImageTo
import com.alfayedoficial.applicationloadingstatusbar.utilities.setActivityToolbar
import com.alfayedoficial.kotlinutils.kuRes


class DetailsActivity : AppCompatActivity() {

    private var _dataBinder: ActivityDetailsBinding? = null
    private val dataBinder: ActivityDetailsBinding
        get() = _dataBinder!!

    private var fileName = ""
    private var downloadStatus = ""

    companion object {
        private const val EXTRA_FILE_NAME = "${BuildConfig.APPLICATION_ID}.FILE_NAME"
        private const val EXTRA_DOWNLOAD_STATUS = "${BuildConfig.APPLICATION_ID}.DOWNLOAD_STATUS"


        fun bundleExtrasOf(fileName: String, downloadStatus: TemplateEnums.DownloadStatus) = bundleOf(
            EXTRA_FILE_NAME to fileName,
            EXTRA_DOWNLOAD_STATUS to downloadStatus.type
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _dataBinder = DataBindingUtil.setContentView(this@DetailsActivity , R.layout.activity_details)

        fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: ""
        downloadStatus = intent.getStringExtra(EXTRA_DOWNLOAD_STATUS) ?: ""

        onActivityCreated()
    }

    private fun onActivityCreated() {
        dataBinder.apply {
            lifecycleOwner = this@DetailsActivity
            activity = this@DetailsActivity
            detailsToolbar.apply { setActivityToolbar(kuRes.getString(R.string.app_name), toolbar, tvNameToolbar) }
            detailContent.fileNameText.text = fileName
            detailContent.downloadStatusText.text = downloadStatus

            onFinishClick()
            onChangeViewForDownloadStatus()
        }
    }

    private fun onChangeViewForDownloadStatus() = dataBinder.detailContent.apply {
        when (downloadStatus) {
            TemplateEnums.DownloadStatus.SUCCESSFUL.type -> {
                downloadStatusImage.changeDownloadStatusImageTo(R.drawable.ic_check_circle_outline_24)
                downloadStatusImage.changeDownloadStatusColorTo(R.color.TemplateGreen)
                downloadStatusText.changeDownloadStatusColorTo(R.color.TemplateGreen)
            }
            TemplateEnums.DownloadStatus.FAILED.type -> {
                downloadStatusImage.changeDownloadStatusImageTo(R.drawable.ic_error_24)
                downloadStatusImage.changeDownloadStatusColorTo(R.color.TemplateRed)
                downloadStatusText.changeDownloadStatusColorTo(R.color.TemplateRed)
            }
        }
    }

    private fun onFinishClick() = dataBinder.detailContent.okButton.setOnClickListener { finishAffinity() }
}