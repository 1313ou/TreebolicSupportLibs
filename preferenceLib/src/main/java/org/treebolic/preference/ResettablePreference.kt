/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ResettablePreference extends Preference
{
	public ResettablePreference(@NonNull final Context context)
	{
		super(context);
	}

	public ResettablePreference(@NonNull final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public ResettablePreference(@NonNull final Context context, final AttributeSet attrs, final int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	public ResettablePreference(@NonNull final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}

	private View.OnClickListener listener;

	public void setClickListener(final View.OnClickListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void onBindViewHolder(@NonNull final PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);

		Button button = (Button) holder.findViewById(R.id.bn_model_dir_reset);
		button.setOnClickListener(this.listener);
	}
}
