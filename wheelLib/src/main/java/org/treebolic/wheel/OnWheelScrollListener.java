/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wheel;

/**
 * Wheel scrolled listener interface.
 */
public interface OnWheelScrollListener
{
	/**
	 * Callback method to be invoked when scrolling started.
	 *
	 * @param wheel the spinnerwheel view whose state has changed.
	 */
	void onScrollingStarted(AbstractWheel wheel);

	/**
	 * Callback method to be invoked when scrolling ended.
	 *
	 * @param wheel the spinnerwheel view whose state has changed.
	 */
	void onScrollingFinished(AbstractWheel wheel);
}
