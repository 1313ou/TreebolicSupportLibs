package org.treebolic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AppCompatCommonUtils
{
	static public final String PREF_THEME = "pref_theme";

	/**
	 * Set theme preference
	 *
	 * @param context  context
	 * @param themeIdx theme idx
	 */
	@SuppressLint("ApplySharedPref")
	public static void setThemePref(final Context context, int themeIdx)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(PREF_THEME, Integer.toString(themeIdx));
		editor.commit();
	}

	/**
	 * Get theme preference
	 *
	 * @param context context
	 * @return value (null if node)
	 */
	@SuppressWarnings("boxing")
	static public Integer getThemePref(final Context context)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final String value = sharedPref.getString(PREF_THEME, null);
		return value == null ? null : Integer.parseInt(value);
	}
}
