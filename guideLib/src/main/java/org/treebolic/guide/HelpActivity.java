package org.treebolic.guide;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Help activity
 *
 * @author Bernard Bou
 */
@SuppressLint("Registered")
public class HelpActivity extends AppCompatActivity
{
	/**
	 * Log tag
	 */
	protected static final String TAG = "Help activity"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		// show the Up button in the action bar.
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// web view
		final WebView webview = (WebView) findViewById(R.id.webView);
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

		String lang = getString(R.string.lang_tag); //$NON-NLS-1$
		String url = "file:///android_asset/help/"; //$NON-NLS-1$
		if (!lang.isEmpty())
		{
			url += lang + '-';
		}
		url += "index.html"; //$NON-NLS-1$
		webview.loadUrl(url);
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	// */
	// @Override
	// public boolean onCreateOptionsMenu(final Menu menu)
	// {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.help, menu);
	// return true;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	// @Override
	// public boolean onOptionsItemSelected(final MenuItem item)
	// {
	// if (item.getItemId() == R.id.action_tips)
	// {
	// Tip.showTips(getFragmentManager());
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
