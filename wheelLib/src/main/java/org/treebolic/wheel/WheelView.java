/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wheel;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;

/**
 *
 */
public class WheelView extends WheelVerticalView
{
	public WheelView(Context context, AttributeSet attrs, @AttrRes int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public WheelView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public WheelView(Context context)
	{
		super(context);
	}
}
