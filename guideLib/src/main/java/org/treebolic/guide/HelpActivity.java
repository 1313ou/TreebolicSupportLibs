package org.treebolic.guide;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
	private static final String TAG = "Help activity";

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.activity_help);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}

		// web view
		final WebView webView = findViewById(R.id.webView);
		webView.clearCache(true);
		webView.clearHistory();
		//webView.getSettings().setJavaScriptEnabled(true);
		//webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl)
			{
				Log.e(HelpActivity.TAG, failingUrl + ':' + description + ',' + errorCode);
			}

			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public void onReceivedError(final WebView view, final WebResourceRequest request, @NonNull final WebResourceError error)
			{
				Log.e(HelpActivity.TAG, error.getDescription().toString() + ',' + error.getErrorCode());
			}

			@Override
			public boolean shouldOverrideUrlLoading(@NonNull final WebView view, final String url)
			{
				view.loadUrl(url);
				return false;
			}

			@TargetApi(Build.VERSION_CODES.N)
			public boolean shouldOverrideUrlLoading(@NonNull final WebView view, @NonNull final WebResourceRequest request)
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
		webView.loadUrl(url);
	}

	@SuppressWarnings("SameReturnValue")
	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item)
	{
		final int itemId = item.getItemId();
		if (itemId == R.id.action_tips)
		{
			Tip.show(getSupportFragmentManager());
			return true;
		}
		else if (itemId == R.id.action_about)
		{
			final Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
