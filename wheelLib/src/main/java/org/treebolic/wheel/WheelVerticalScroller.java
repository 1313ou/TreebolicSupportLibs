/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wheel;

import android.content.Context;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * Scroller class handles scrolling events and updates the
 */
public class WheelVerticalScroller extends WheelScroller
{
	/**
	 * Constructor
	 *
	 * @param context  the current context
	 * @param listener the scrolling listener
	 */
	public WheelVerticalScroller(Context context, ScrollingListener listener)
	{
		super(context, listener);
	}

	@Override
	protected int getCurrentScrollerPosition()
	{
		return this.scroller.getCurrY();
	}

	@Override
	protected int getFinalScrollerPosition()
	{
		return this.scroller.getFinalY();
	}

	@Override
	protected float getMotionEventPosition(@NonNull MotionEvent event)
	{
		// should be overridden
		return event.getY();
	}

	@Override
	protected void scrollerStartScroll(int distance, int time)
	{
		this.scroller.startScroll(0, 0, 0, distance, time);
	}

	@Override
	protected void scrollerFling(int position, int velocityX, int velocityY)
	{
		final int maxPosition = 0x7FFFFFFF;
		final int minPosition = -maxPosition;
		this.scroller.fling(0, position, 0, -velocityY, 0, 0, minPosition, maxPosition);
	}
}
