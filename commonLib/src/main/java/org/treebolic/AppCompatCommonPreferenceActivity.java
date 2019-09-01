/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import org.treebolic.common.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public abstract class AppCompatCommonPreferenceActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
{
	private static final String TITLE_TAG = "settingsActivityTitle";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// theme
		final Integer themeId = AppCompatCommonUtils.getThemePref(this);
		if (themeId != null)
		{
			setTheme(themeId);
		}

		// super
		super.onCreate(savedInstanceState);

		// content view
		setContentView(R.layout.activity_settings);

		// fragment
		if (savedInstanceState == null)
		{
			getSupportFragmentManager().beginTransaction().replace(R.id.settings, new HeaderFragment()).commit();
		}
		else
		{
			setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
		}
		getSupportFragmentManager().addOnBackStackChangedListener(() -> {

			if (getSupportFragmentManager().getBackStackEntryCount() == 0)
			{
				setTitle(R.string.title_settings);
			}
		});

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);
		// Save current activity title so we can set it again after a configuration change
		outState.putCharSequence(TITLE_TAG, getTitle());
	}

	// M E N U

	@SuppressWarnings("SameReturnValue")
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.preferences, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item)
	{
		final int itemId = item.getItemId();
		if (itemId == R.id.action_reset_settings)
		{
			resetSettings();
			restart();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// U P

	@Override
	public boolean onSupportNavigateUp()
	{
		if (getSupportFragmentManager().popBackStackImmediate())
		{
			return true;
		}
		return super.onSupportNavigateUp();
	}

	// U T I L S

	@Override
	public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref)
	{
		// Instantiate the new Fragment
		final Bundle args = pref.getExtras();
		final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), pref.getFragment());
		fragment.setArguments(args);
		fragment.setTargetFragment(caller, 0);

		// Replace the existing Fragment with the new Fragment
		getSupportFragmentManager().beginTransaction().replace(R.id.settings, fragment).addToBackStack(null).commit();
		setTitle(pref.getTitle());
		return true;
	}

	// H E A D E R   F R A G M E N T

	@SuppressWarnings("WeakerAccess")
	public static class HeaderFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
		{
			setPreferencesFromResource(R.xml.pref_headers, rootKey);
		}
	}

	// U T I L S

	/**
	 * Reset settings
	 */
	private void resetSettings()
	{
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.clear();
		tryCommit(editor);
	}

	/**
	 * Try to commit
	 *
	 * @param editor editor editor
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	private void tryCommit(@NonNull final SharedPreferences.Editor editor)
	{
		try
		{
			editor.apply();
		}
		catch (@NonNull final AbstractMethodError ignored)
		{
			// The app injected its own pre-Gingerbread SharedPreferences.Editor implementation without an apply method.
			editor.commit();
		}
	}

	/**
	 * Restart app
	 */
	private void restart()
	{
		final Intent restartIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		assert restartIntent != null;
		restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(restartIntent);
	}
}
