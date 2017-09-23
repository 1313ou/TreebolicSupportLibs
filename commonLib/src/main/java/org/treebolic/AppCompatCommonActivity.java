package org.treebolic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class AppCompatCommonActivity extends AppCompatActivity
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
