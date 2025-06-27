/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.guide

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.InflateException
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import org.treebolic.AppCompatCommonActivity
import org.treebolic.guide.Tip.Companion.show

/**
 * Help activity
 *
 * @author Bernard Bou
 */
@SuppressLint("Registered")
open class HelpActivity : AppCompatCommonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        try {
            setContentView(R.layout.activity_help)
        } catch (e: InflateException) {
            Toast.makeText(this, "No WebView support", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }

        // web view
        val webView = findViewById<WebView>(R.id.webView)
        webView.clearCache(true)
        webView.clearHistory()
        // webView.settings.javaScriptEnabled = true
        // webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.webViewClient = object : WebViewClient(
        ) {
            @Deprecated("Deprecated in Java")
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                Log.e(TAG, "$failingUrl:$description,$errorCode")
            }

            @SuppressLint("ObsoleteSdkInt")
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                Log.e(TAG, error.description.toString() + ',' + error.errorCode)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return false
            }

            @SuppressLint("ObsoleteSdkInt")
            @RequiresApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                view.loadUrl(uri.toString())
                return false
            }
        }

        val lang = getString(R.string.lang_tag)
        var url = "file:///android_asset/help/"
        if (lang.isNotEmpty()) {
            url += "$lang-"
        }
        url += "index.html"
        webView.loadUrl(url)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.help, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_tips) {
            show(supportFragmentManager)
            return true
        } else if (itemId == R.id.action_about) {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {

        private const val TAG = "Help activity"
    }
}
