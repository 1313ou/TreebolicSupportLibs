/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppCompatCommonUtils
{
	static private final String TAG = "AppCompatCommonUtils";

	@SuppressWarnings("WeakerAccess")
	static public final String PREF_THEME = "pref_theme";

	/**
	 * Set theme preference
	 *
	 * @param context  context
	 * @param themeIdx theme idx
	 */
	static public void setThemePref(final Context context, int themeIdx)
	{
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(PREF_THEME, Integer.toString(themeIdx));
		tryCommit(editor);
	}

	/**
	 * Get theme preference
	 *
	 * @param context context
	 * @return value (null if node)
	 */
	@Nullable
	@SuppressWarnings("boxing")
	static public Integer getThemePref(@NonNull final Context context)
	{
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		final String value = sharedPrefs.getString(PREF_THEME, null);
		if (value == null)
		{
			return null;
		}
		int intValue = Integer.parseInt(value);
		if (intValue == 0)
		{
			final SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.remove(PREF_THEME);
			editor.apply();
			return null;
		}

		final Resources resources = context.getResources();
		try
		{
			final String type = resources.getResourceTypeName(intValue);
			if ("style".equals(type))
			{
				Log.d(TAG, "Theme " + type + ' ' + resources.getResourceName(intValue) + ' ' + context.getResources().getResourceEntryName(intValue));
				return intValue;
			}
		}
		catch (Resources.NotFoundException ignored)
		{
			final SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.remove(PREF_THEME);
			editor.apply();
		}
		return null;
	}

	/**
	 * Try to commit
	 *
	 * @param editor editor editor
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static private void tryCommit(@NonNull final SharedPreferences.Editor editor)
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
}
