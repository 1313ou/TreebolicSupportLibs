/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.treebolic.common.R;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.legacy.contrib.PreferenceActivityCompat;

public abstract class AppCompatCommonPreferenceActivity extends PreferenceActivityCompat
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

	// A C T I O N   B A R

	protected void setupToolbar(@LayoutRes int toolbarLayoutRes, @IdRes int toolbarId)
	{
		// TODO hacked dependency on R.id.action_bar_root
		final ViewGroup rootView = findViewById(R.id.action_bar_root); //id from appcompat
		if (rootView != null)
		{
			final View view = getLayoutInflater().inflate(toolbarLayoutRes, rootView, false);
			rootView.addView(view, 0);

			final Toolbar toolbar = findViewById(toolbarId);
			setSupportActionBar(toolbar);
		}
	}

	@SuppressWarnings("WeakerAccess")
	@Nullable
	public ActionBar getSupportActionBar()
	{
		return getDelegate().getSupportActionBar();
	}

	@SuppressWarnings("WeakerAccess")
	public void setSupportActionBar(@Nullable Toolbar toolbar)
	{
		getDelegate().setSupportActionBar(toolbar);
	}
}
