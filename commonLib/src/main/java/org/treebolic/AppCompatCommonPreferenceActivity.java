package org.treebolic;

import android.os.Bundle;
import android.support.v7app.contrib.AppCompatPreferenceActivity;


public class AppCompatCommonPreferenceActivity extends AppCompatPreferenceActivity
{
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
