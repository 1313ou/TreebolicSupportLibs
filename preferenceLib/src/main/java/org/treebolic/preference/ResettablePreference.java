/*
 * Copyright (c) 2021. Bernard Bou <1313ou@gmail.com>.
 */

package org.treebolic.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ResettablePreference extends Preference
{
	public ResettablePreference(final Context context)
	{
		super(context);
	}

	public ResettablePreference(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public ResettablePreference(final Context context, final AttributeSet attrs, final int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	public ResettablePreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}

	private View.OnClickListener listener;

	public void setClickListener(final View.OnClickListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void onBindViewHolder(final PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);

		Button button = (Button) holder.findViewById(R.id.bn_model_dir_reset);
		button.setOnClickListener(this.listener);
	}
}
