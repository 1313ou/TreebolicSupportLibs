/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import org.treebolic.AppCompatCommonActivity
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Download activity
 *
 * @author Bernard Bou
 */
abstract class DownloadActivity : AppCompatCommonActivity(), View.OnClickListener {

    /**
     * Download id
     */
    protected var downloadId: Long = -1

    /**
     * Download uri
     */
    @JvmField
    protected var downloadUrl: String? = null

    /**
     * Download manager
     */
    private var downloadManager: DownloadManager? = null

    /**
     * Done receiver
     */
    private var receiver: BroadcastReceiver? = null

    /**
     * Download button
     */
    private var downloadButton: ImageButton? = null

    /**
     * Progress bar
     */
    private var progressBar: ProgressBar? = null

    /**
     * Progress status
     */
    private var progressStatus: TextView? = null

    /**
     * Source (file)
     */
    private var src: TextView? = null

    /**
     * Source 2 (server)
     */
    private var src2: TextView? = null

    /**
     * Target
     */
    private var target: TextView? = null

    /**
     * Expand archive checkbox
     */
    @JvmField
    protected var expandArchiveCheckbox: CheckBox? = null

    /**
     * Whether to expand archive
     */
    @JvmField
    protected var expandArchive: Boolean = false

    // A B S T R A C T

    /**
     * Start download
     */
    protected abstract fun start()

    /**
     * Whether to process
     */
    protected abstract fun doProcessing(): Boolean

    // P R O C E S S I N G

    /**
     * Process obtained input stream: what to do once the file has been downloaded and opened as a stream
     *
     * @param inputStream obtained input stream
     * @return true if file should be disposed of
     */
    @Throws(IOException::class)
    protected open fun process(inputStream: InputStream): Boolean {
        return false
    }

    // L I F E C Y C L E

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        setContentView(R.layout.activity_download)

        // toolbar
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }

        // download manager
        this.downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        // components
        this.downloadButton = findViewById<View>(R.id.downloadButton) as ImageButton
        downloadButton!!.setOnClickListener(this)

        val showDownloadButton = findViewById<View>(R.id.showButton) as Button
        showDownloadButton.setOnClickListener(this)
        this.progressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        this.progressStatus = findViewById<View>(R.id.progressStatus) as TextView
        this.src = findViewById(R.id.src)
        this.src2 = findViewById(R.id.src2)
        this.target = findViewById(R.id.target)
        this.expandArchiveCheckbox = findViewById<View>(R.id.expandArchive) as CheckBox
        expandArchiveCheckbox!!.setOnClickListener { this@DownloadActivity.expandArchive = expandArchiveCheckbox!!.isChecked }

        // retrieve arguments
        val allowKeepArchive = intent.getBooleanExtra(ARG_ALLOW_EXPAND_ARCHIVE, false)
        if (allowKeepArchive) {
            expandArchiveCheckbox!!.visibility = View.VISIBLE
        }

        // receiver
        this.receiver = object : BroadcastReceiver(
        ) {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
                    if (id == this@DownloadActivity.downloadId) {
                        val success = retrieve()

                        // progress
                        progressBar!!.progress = if (success) 100 else 0
                        progressStatus!!.setText(if (success) R.string.status_download_successful else R.string.status_download_fail)

                        // toast
                        Toast.makeText(this@DownloadActivity, if (success) R.string.ok_data else R.string.fail_data, Toast.LENGTH_SHORT).show()

                        // return result
                        val resultIntent = Intent()
                        resultIntent.putExtra(RESULT_DOWNLOAD_DATA_AVAILABLE, true)
                        this@DownloadActivity.setResult(RESULT_OK, resultIntent)

                        finish()
                    }
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val downloadUri = Uri.parse(this.downloadUrl)
        val downloadUriStr = downloadUri.toString()
        val file = downloadUri.lastPathSegment
        val where = downloadUriStr.substring(0, downloadUriStr.length - file!!.length)
        src!!.text = file
        src2!!.text = where
        target!!.text = getString(R.string.internal)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()

        // register receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(this.receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(this.receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        // finish
        if (finished()) {
            Toast.makeText(this@DownloadActivity, R.string.ok_data, Toast.LENGTH_SHORT).show()

            // return result
            val resultIntent = Intent()
            resultIntent.putExtra(RESULT_DOWNLOAD_DATA_AVAILABLE, true)
            setResult(RESULT_OK, resultIntent)

            finish()
        }
    }

    override fun onStop() {
        // register receiver
        unregisterReceiver(this@DownloadActivity.receiver)
        super.onStop()
    }

    // C L I C K

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.downloadButton) {
            downloadButton!!.visibility = View.INVISIBLE
            progressBar!!.visibility = View.VISIBLE
            progressStatus!!.visibility = View.VISIBLE

            // start download
            start()
        } else if (id == R.id.showButton) {
            showDownload()
        }
    }

    /**
     * Start download. Assume download url has been set by derived class
     */
    protected fun start(@StringRes titleRes: Int) {
        val downloadUri = Uri.parse(this.downloadUrl)
        try {
            val request = DownloadManager.Request(downloadUri)
            Log.d(TAG, "Source $downloadUri")
            request.setTitle(resources.getText(titleRes))
            request.setDescription(downloadUri.lastPathSegment)

            // @formatter: off
            //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            //  {
            // 	    request.setAllowedOverMetered(false);
            //	}
            //	else
            //	{
            //		request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
            //	}
            //	request.setAllowedOverRoaming(false);
            // @formatter: on
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            this.downloadId = downloadManager!!.enqueue(request)

            // start progress
            startProgress()
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
            Log.e(TAG, "Failed ", e)
        }
    }

    /**
     * Whether download has finished
     *
     * @return true if download has finished
     */
    private fun finished(): Boolean {
        // query
        val query = DownloadManager.Query()
        query.setFilterById(this.downloadId)

        // cursor
        val cursor = downloadManager!!.query(query)
        cursor.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = it.getInt(columnIndex)
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL, DownloadManager.STATUS_FAILED -> return true
                    else -> {}
                }
            }
            return false
        }
    }

    /**
     * Retrieve data
     *
     * @return status
     */
    private fun retrieve(): Boolean {
        // query
        val query = DownloadManager.Query()
        query.setFilterById(this.downloadId)

        // cursor
        val cursor = downloadManager!!.query(query)
        cursor.use {
            if (cursor.moveToFirst()) {
                val columnIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (DownloadManager.STATUS_SUCCESSFUL == it.getInt(columnIndex)) {
                    // local uri
                    val uriIndex = it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    val uriString = it.getString(uriIndex)
                    val uri = Uri.parse(uriString)

                    // as is
                    if (!doProcessing()) {
                        return true
                    }

                    // process
                    var dispose = false
                    try {
                        this@DownloadActivity.contentResolver.openInputStream(uri).use { inputStream ->
                            // handle
                            dispose = process(inputStream!!)
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Processing $uriString", e)
                    }

                    // dispose
                    if (dispose) {
                        // dispose
                        val file = File(uri.path!!)
                        file.delete()
                    }
                    return true
                }

                val uriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_URI)
                val uri = cursor.getString(uriColumn)
                val localUriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val localUri = cursor.getString(localUriColumn)
                val reasonColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                val reason = cursor.getInt(reasonColumnIndex)
                Log.e(TAG, "Downloading $uri to $localUri failed with reason code $reason")
            }
            return false
        }
    }

    /**
     * Start progress update thread
     */
    private fun startProgress() {
        Thread {
            var downloading = true
            while (downloading) {
                // query
                val query = DownloadManager.Query()
                query.setFilterById(this@DownloadActivity.downloadId)

                // cursor
                val cursor = downloadManager!!.query(query)
                if (cursor.moveToFirst()) {
                    // size info
                    val downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val downloaded = cursor.getInt(downloadedIndex)
                    val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val total = cursor.getInt(totalIndex)
                    val progress = (downloaded * 100L / total).toInt()

                    // exit loop condition
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusIndex)
                    when (status) {
                        DownloadManager.STATUS_FAILED, DownloadManager.STATUS_SUCCESSFUL -> downloading = false
                        else -> {}
                    }
                    // update UI
                    val resStatus = status2ResourceId(status)
                    Log.d(TAG, resources.getString(resStatus) + " at " + progress)
                    runOnUiThread {
                        progressBar!!.progress = progress
                        progressStatus!!.setText(resStatus)
                    }
                }
                cursor.close()

                // sleep
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    //
                }
            }
        }.start()
    }

    /**
     * Show downloads
     */
    private fun showDownload() {
        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        startActivity(intent)
    }

    companion object {

        private const val TAG = "Download"

        /**
         * Allow expanding of archive key
         */
        const val ARG_ALLOW_EXPAND_ARCHIVE: String = "download_allow_expand_archive"

        /**
         * Result extra
         */
        const val RESULT_DOWNLOAD_DATA_AVAILABLE: String = "download_data_available"

        /**
         * Get status message as per status returned by cursor
         *
         * @param status status
         * @return string resource id
         */
        @StringRes
        private fun status2ResourceId(status: Int): Int {
            return when (status) {
                DownloadManager.STATUS_FAILED -> R.string.status_download_fail
                DownloadManager.STATUS_PAUSED -> R.string.status_download_paused
                DownloadManager.STATUS_PENDING -> R.string.status_download_pending
                DownloadManager.STATUS_RUNNING -> R.string.status_download_running
                DownloadManager.STATUS_SUCCESSFUL -> R.string.status_download_successful
                else -> -1
            }
        }
    }
}
