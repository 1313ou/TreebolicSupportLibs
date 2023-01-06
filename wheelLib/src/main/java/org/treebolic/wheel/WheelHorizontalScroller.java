/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.wheel;

import android.content.Context;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public class WheelHorizontalScroller extends WheelScroller
{

	/**
	 * Constructor
	 *
	 * @param context  the current context
	 * @param listener the scrolling listener
	 */
	public WheelHorizontalScroller(Context context, ScrollingListener listener)
	{
		super(context, listener);
	}

	@Override
	protected int getCurrentScrollerPosition()
	{
		return this.scroller.getCurrX();
	}

	@Override
	protected int getFinalScrollerPosition()
	{
		return this.scroller.getFinalX();
	}

	@Override
	protected float getMotionEventPosition(@NonNull MotionEvent event)
	{
		// should be overridden
		return event.getX();
	}

	@Override
	protected void scrollerStartScroll(int distance, int time)
	{
		this.scroller.startScroll(0, 0, distance, 0, time);
	}

	@Override
	protected void scrollerFling(int position, int velocityX, int velocityY)
	{
		final int maxPosition = 0x7FFFFFFF;
		final int minPosition = -maxPosition;
		this.scroller.fling(position, 0, -velocityX, 0, minPosition, maxPosition, 0, 0);
	}
}
