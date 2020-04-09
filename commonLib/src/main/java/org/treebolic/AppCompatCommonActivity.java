/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

public abstract class AppCompatCommonActivity extends AppCompatActivity
{
	/**
	 * Version preference name
	 */
	static final String PREF_VERSION = "org.treebolic.wordnet.browser.version";

	@SuppressWarnings("UnusedReturnValue")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// first run of this version
		clearSettingsOnUpgrade(AppCompatCommonActivity.PREF_VERSION);

		// theme
		final Integer themeId = AppCompatCommonUtils.getThemePref(this);
		if (themeId != null)
		{
			setTheme(themeId);
		}

		super.onCreate(savedInstanceState);
	}

	/**
	 * Clear settings on upgrade
	 *
	 * @param key key holding last version
	 * @return build version
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	@SuppressWarnings({"UnusedReturnValue"})
	private long clearSettingsOnUpgrade(@SuppressWarnings("SameParameterValue") final String key)
	{
		// first run of this version
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		long version;
		try
		{
			version = prefs.getLong(key, -1);
		}
		catch (ClassCastException e)
		{
			version = prefs.getInt(key, -1);
		}
		long build = 0; //BuildConfig.VERSION_CODE;
		try
		{
			final PackageInfo packageInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
			{
				build = packageInfo.getLongVersionCode();
			}
			else
			{
				build = packageInfo.versionCode;
			}
		}
		catch (PackageManager.NameNotFoundException ignored)
		{
			//
		}
		if (version < build)
		{
			final SharedPreferences.Editor edit = prefs.edit();

			// clear settings
			edit.clear();

			// flag as 'has run'
			edit.putLong(key, build).apply();
		}
		return build;
	}
}
