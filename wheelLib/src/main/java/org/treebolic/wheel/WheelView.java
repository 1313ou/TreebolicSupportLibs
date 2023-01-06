/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.wheel;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;

/**
 *
 */
public class WheelView extends WheelVerticalView
{
	public WheelView(@NonNull Context context, AttributeSet attrs, @AttrRes int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public WheelView(@NonNull Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public WheelView(@NonNull Context context)
	{
		super(context);
	}
}
