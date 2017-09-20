package org.treebolic.guide;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.treebolic.AppCompatCommonActivity;

/**
 * Help activity
 *
 * @author Bernard Bou
 */
@SuppressLint("Registered")
public class HelpActivity extends AppCompatCommonActivity
{
	/**
	 * Log tag
	 */
	protected static final String TAG = "Help activity";

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.activity_help);

		// toolbar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}

		// web view
		final WebView webview = (WebView) findViewById(R.id.webView);
		webview.clearCache(true);
		webview.clearHistory();
		//webview.getSettings().setJavaScriptEnabled(true);
		//webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webview.setWebViewClient(new WebViewClient()
		{
			@SuppressWarnings("deprecation")
			@Override
			public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl)
			{
				Log.e(HelpActivity.TAG, failingUrl + ':' + description + ',' + errorCode);
			}

			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public void onReceivedError(final WebView view, final WebResourceRequest request, final WebResourceError error)
			{
				Log.e(HelpActivity.TAG, error.getDescription().toString() + ',' + error.getErrorCode());
			}

			@SuppressWarnings("deprecation")
			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url)
			{
				view.loadUrl(url);
				return false;
			}

			@TargetApi(Build.VERSION_CODES.N)
			public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request)
			{
				final Uri uri = request.getUrl();
				view.loadUrl(uri.toString());
				return false;
			}
		});

		String lang = getString(R.string.lang_tag);
		String url = "file:///android_asset/help/";
		if (!lang.isEmpty())
		{
			url += lang + '-';
		}
		url += "index.html";
		webview.loadUrl(url);
	}

	// /*
	// @Override
	// public boolean onCreateOptionsMenu(final Menu menu)
	// {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.help, menu);
	// return true;
	// }

	// @Override
	// public boolean onOptionsItemSelected(final MenuItem item)
	// {
	// if (item.getItemId() == R.id.action_tips)
	// {
	// Tip.showTips(getSupportFragmentManager());
	// return true;
	// }
	// else if (item.getItemId() == R.id.action_help)
	// {
	// HelpActivity.start(this);
	// return true;
	// }
	//
	// return super.onOptionsItemSelected(item);
	// }

	/**
	 * Show help
	 */
	static public void start(final Context context)
	{
		final Intent intent = new Intent(context, HelpActivity.class);
		context.startActivity(intent);
	}
}
