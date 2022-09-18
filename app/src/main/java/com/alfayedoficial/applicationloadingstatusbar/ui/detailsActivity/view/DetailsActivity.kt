package com.alfayedoficial.applicationloadingstatusbar.ui.detailsActivity.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.alfayedoficial.applicationloadingstatusbar.BuildConfig
import com.alfayedoficial.applicationloadingstatusbar.utilities.TemplateEnums


class DetailsActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_FILE_NAME = "${BuildConfig.APPLICATION_ID}.FILE_NAME"
        private const val EXTRA_DOWNLOAD_STATUS = "${BuildConfig.APPLICATION_ID}.DOWNLOAD_STATUS"

        /**
         * Creates a [Bundle] with given parameters and pass as data to [DetailsActivity].
         */
        fun bundleExtrasOf(
            fileName: String,
            downloadStatus: TemplateEnums.DownloadStatus
        ) = bundleOf(
            EXTRA_FILE_NAME to fileName,
            EXTRA_DOWNLOAD_STATUS to downloadStatus.type
        )
    }
}