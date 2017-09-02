package org.treebolic;

import android.os.Bundle;
import android.support.v7app.contrib.AppCompatPreferenceActivity;


public class AppCompatCommonPreferenceActivity extends AppCompatPreferenceActivity
{
	static private final String PREF_THEME = "pref_theme";

	static private final int NO_THEME = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// theme
		final Integer themeId = AppCompatCommonUtils.getThemePref(this);
		if (themeId != null)
		{
			setTheme(themeId);
		}

		super.onCreate(savedInstanceState);
	}
}
