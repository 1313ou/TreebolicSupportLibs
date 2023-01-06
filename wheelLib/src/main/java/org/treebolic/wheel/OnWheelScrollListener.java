/*
 * Copyright (c) 2019-2023. Bernard Bou
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
	@SuppressWarnings("EmptyMethod")
	void onScrollingStarted(AbstractWheel wheel);

	/**
	 * Callback method to be invoked when scrolling ended.
	 *
	 * @param wheel the spinnerwheel view whose state has changed.
	 */
	@SuppressWarnings("EmptyMethod")
	void onScrollingFinished(AbstractWheel wheel);
}
