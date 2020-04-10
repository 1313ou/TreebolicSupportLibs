/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public abstract class AppCompatCommonActivity extends AppCompatActivity
{
	@SuppressWarnings("UnusedReturnValue")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// give app a chance to do something before theme is set
		preThemeHook();

		// theme
		final Integer themeId = AppCompatCommonUtils.getThemePref(this);
		if (themeId != null)
		{
			setTheme(themeId);
		}

		super.onCreate(savedInstanceState);
	}

	/**
	 * Pre-theming hook: give app a chance to do something before theme is set by overriding this.
	 */
	protected void preThemeHook()
	{
	}
}
