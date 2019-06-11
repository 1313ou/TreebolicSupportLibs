package org.treebolic;

import android.os.Bundle;
import androidx.appcompat.app.contrib.AppCompatPreferenceActivity;

public abstract class AppCompatCommonPreferenceActivity extends AppCompatPreferenceActivity
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
