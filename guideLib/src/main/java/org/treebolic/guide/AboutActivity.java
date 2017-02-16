package org.treebolic.guide;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * About activity
 *
 * @author Bernard Bou
 */
@SuppressLint("Registered")
public class AboutActivity extends Activity
{
	// protected static final String TAG = "About activity"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		// show the Up button in the action bar.
		final ActionBar actionBar = getActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == R.id.action_help)
		{
			HelpActivity.start(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Show about
	 */
	static public void start(final Context context)
	{
		final Intent intent = new Intent(context, AboutActivity.class);
		context.startActivity(intent);
	}
}
