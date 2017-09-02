package org.treebolic.download;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.treebolic.AppCompatCommonActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Download activity
 *
 * @author Bernard Bou
 */
abstract public class DownloadActivity extends AppCompatCommonActivity implements View.OnClickListener
{
	/**
	 * Log tag
	 */
	private static final String TAG = "Download";

	/**
	 * Allow expanding of archive key
	 */
	public static final String ARG_ALLOW_EXPAND_ARCHIVE = "download_allow_expand_archive";

	/**
	 * Result extra
	 */
	public static final String RESULT_DOWNLOAD_DATA_AVAILABLE = "download_data_available";

	/**
	 * Start download
	 */
	abstract protected void start();

	/**
	 * Whether to process
	 */
	abstract protected boolean doProcessing();

	/**
	 * Process obtained input stream: what to do once the file has been downloaded and opened as a stream
	 *
	 * @param inputStream obtained input stream
	 * @return true if file should be disposed of
	 */
	@SuppressWarnings({"static-method", "RedundantThrows"})
	protected boolean process(@SuppressWarnings("UnusedParameters") final InputStream inputStream) throws IOException
	{
		return false;
	}

	/**
	 * Download id
	 */
	protected long downloadId = -1;

	/**
	 * Download uri
	 */
	protected String downloadUrl;

	protected Uri downloadUri;

	/**
	 * Destination uri
	 */
	protected File destDir;

	protected Uri destUri;

	/**
	 * Download manager
	 */
	private DownloadManager downloadManager;

	/**
	 * Done receiver
	 */
	private BroadcastReceiver receiver;

	/**
	 * Download button
	 */
	private ImageButton downloadButton;

	/**
	 * Progress bar
	 */
	private ProgressBar progressBar;

	/**
	 * Progress status
	 */
	private TextView progressStatus;

	/**
	 * Source
	 */
	private TextView src;

	/**
	 * Target
	 */
	private TextView target;

	/**
	 * Expand archive checkbox
	 */
	protected CheckBox expandArchiveCheckbox;

	/**
	 * Whether to expand archive
	 */
	protected boolean expandArchive = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.activity_download);

		// toolbar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}

		// download manager
		this.downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

		// components
		this.downloadButton = (ImageButton) findViewById(R.id.downloadButton);
		this.downloadButton.setOnClickListener(this);

		final Button showDownloadButton = (Button) findViewById(R.id.showButton);
		showDownloadButton.setOnClickListener(this);
		this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
		this.progressStatus = (TextView) findViewById(R.id.progressStatus);
		this.src = (TextView) findViewById(R.id.src);
		this.target = (TextView) findViewById(R.id.target);
		this.expandArchiveCheckbox = (CheckBox) findViewById(R.id.expandArchive);
		this.expandArchiveCheckbox.setOnClickListener(new OnClickListener()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void onClick(final View v)
			{
				DownloadActivity.this.expandArchive = DownloadActivity.this.expandArchiveCheckbox.isChecked();
			}
		});

		// retrieve arguments
		final boolean allowKeepArchive = getIntent().getBooleanExtra(ARG_ALLOW_EXPAND_ARCHIVE, false);
		if (allowKeepArchive)
		{
			this.expandArchiveCheckbox.setVisibility(View.VISIBLE);
		}

		// receiver
		this.receiver = new BroadcastReceiver()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void onReceive(final Context context, final Intent intent)
			{
				final String action = intent.getAction();
				if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
				{
					final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
					if (id == DownloadActivity.this.downloadId)
					{
						final boolean success = retrieve();

						// progress
						DownloadActivity.this.progressBar.setProgress(success ? 100 : 0);
						DownloadActivity.this.progressStatus.setText(success ? R.string.status_download_successful : R.string.status_download_fail);

						// toast
						Toast.makeText(DownloadActivity.this, success ? R.string.ok_data : R.string.fail_data, Toast.LENGTH_SHORT).show();

						// return result
						final Intent resultIntent = new Intent();
						resultIntent.putExtra(DownloadActivity.RESULT_DOWNLOAD_DATA_AVAILABLE, true);
						DownloadActivity.this.setResult(AppCompatActivity.RESULT_OK, resultIntent);

						finish();
					}
				}
			}
		};
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		this.src.setText(this.downloadUrl);
		this.target.setText(this.destDir != null ? this.destDir.getAbsolutePath() : getString(R.string.internal));
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		// register receiver
		registerReceiver(this.receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

		// finish
		if (finished())
		{
			Toast.makeText(DownloadActivity.this, R.string.ok_data, Toast.LENGTH_SHORT).show();

			// return result
			final Intent resultIntent = new Intent();
			resultIntent.putExtra(DownloadActivity.RESULT_DOWNLOAD_DATA_AVAILABLE, true);
			setResult(AppCompatActivity.RESULT_OK, resultIntent);

			finish();
		}
	}

	@Override
	protected void onStop()
	{
		// register receiver
		unregisterReceiver(DownloadActivity.this.receiver);
		super.onStop();
	}

	@Override
	public void onClick(final View view)
	{
		final int id = view.getId();
		if (id == R.id.downloadButton)
		{
			this.downloadButton.setVisibility(View.INVISIBLE);
			this.progressBar.setVisibility(View.VISIBLE);
			this.progressStatus.setVisibility(View.VISIBLE);

			// start download
			start();
		}
		else if (id == R.id.showButton)
		{
			showDownload();
		}
	}

	/**
	 * Start download. Assume download url has been set by derived class
	 */
	protected void start(final int titleRes)
	{
		this.downloadUri = Uri.parse(this.downloadUrl);
		try
		{
			final Request request = new Request(this.downloadUri);
			request.setTitle(getResources().getText(titleRes));
			request.setDescription(this.downloadUri.getLastPathSegment());
			if (this.destDir != null)
			{
				final File destFile = new File(this.destDir, this.downloadUri.getLastPathSegment());
				this.destUri = Uri.fromFile(destFile);
				request.setDestinationUri(this.destUri);
			}

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

			request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
			this.downloadId = this.downloadManager.enqueue(request);

			// start progress
			startProgress();
		}
		catch (Exception e)
		{
			Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Whether download has finished
	 *
	 * @return true if download has finished
	 */
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
	private boolean finished()
	{
		// query
		final Query query = new Query();
		query.setFilterById(this.downloadId);

		// cursor
		final Cursor cursor = DownloadActivity.this.downloadManager.query(query);
		try
		{
			if (cursor.moveToFirst())
			{
				final int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
				final int status = cursor.getInt(columnIndex);
				switch (status)
				{
					case DownloadManager.STATUS_SUCCESSFUL:
					case DownloadManager.STATUS_FAILED:
						return true;
					default:
						break;
				}
			}
			return false;
		}
		finally
		{
			cursor.close();
		}
	}

	/**
	 * Retrieve data
	 *
	 * @return status
	 */
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
	private boolean retrieve()
	{
		// query
		final Query query = new Query();
		query.setFilterById(this.downloadId);

		// cursor
		final Cursor cursor = DownloadActivity.this.downloadManager.query(query);
		try
		{
			if (cursor.moveToFirst())
			{
				final int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
				if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex))
				{
					// local uri
					final String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
					final Uri uri = Uri.parse(uriString);

					// as is
					if (!doProcessing())
					{
						return true;
					}

					// process
					InputStream inputStream = null;
					//noinspection TryWithIdenticalCatches
					try
					{
						inputStream = DownloadActivity.this.getContentResolver().openInputStream(uri);

						// handle
						if (process(inputStream))
						{
							// dispose
							final File file = new File(this.destDir, uri.getLastPathSegment());
							//noinspection ResultOfMethodCallIgnored
							file.delete();
						}

						return true;
					}
					catch (final FileNotFoundException e)
					{
						Log.e(DownloadActivity.TAG, "Downloading " + uriString, e);
					}
					catch (final IOException e)
					{
						Log.e(DownloadActivity.TAG, "Downloading " + uriString, e);
					}
					finally
					{
						if (inputStream != null)
						{
							try
							{
								inputStream.close();
							}
							catch (final IOException e)
							{
								//
							}
						}
					}
				}
			}
			return false;
		}
		finally
		{
			cursor.close();
		}
	}

	/**
	 * Start progress update thread
	 */
	private void startProgress()
	{
		new Thread(new Runnable()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void run()
			{
				boolean downloading = true;
				while (downloading)
				{
					// query
					final DownloadManager.Query query = new Query();
					query.setFilterById(DownloadActivity.this.downloadId);

					// cursor
					final Cursor cursor = DownloadActivity.this.downloadManager.query(query);
					if (cursor.moveToFirst())
					{
						// size info
						final int downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
						final int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
						final int progress = (int) (downloaded * 100L / total);

						// exit loop condition
						final int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
						switch (status)
						{
							case DownloadManager.STATUS_FAILED:
							case DownloadManager.STATUS_SUCCESSFUL:
								downloading = false;
								break;
							default:
								break;
						}

						// update UI
						final int resStatus = DownloadActivity.status2ResourceId(status);
						Log.d(DownloadActivity.TAG, getResources().getString(resStatus) + " at " + progress);
						runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								DownloadActivity.this.progressBar.setProgress(progress);
								DownloadActivity.this.progressStatus.setText(resStatus);
							}
						});
					}
					cursor.close();

					// sleep
					try
					{
						Thread.sleep(2000);
					}
					catch (final InterruptedException e)
					{
						//
					}
				}
			}
		}).start();
	}

	/**
	 * Get status message as per status returned by cursor
	 *
	 * @param status status
	 * @return string resource id
	 */
	private static int status2ResourceId(final int status)
	{
		switch (status)
		{
			case DownloadManager.STATUS_FAILED:
				return R.string.status_download_fail;
			case DownloadManager.STATUS_PAUSED:
				return R.string.status_download_paused;
			case DownloadManager.STATUS_PENDING:
				return R.string.status_download_pending;
			case DownloadManager.STATUS_RUNNING:
				return R.string.status_download_running;
			case DownloadManager.STATUS_SUCCESSFUL:
				return R.string.status_download_successful;
			default:
				return -1;
		}
	}

	/**
	 * Show downloads
	 */
	public void showDownload()
	{
		final Intent intent = new Intent();
		intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
		startActivity(intent);
	}
}
