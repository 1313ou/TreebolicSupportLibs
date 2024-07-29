/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

/**
 * Wheel scrolled listener interface.
 */
interface OnWheelScrollListener {

    /**
     * Callback method to be invoked when scrolling started.
     *
     * @param wheel the spinnerwheel view whose state has changed.
     */
    fun onScrollingStarted(wheel: AbstractWheel)

    /**
     * Callback method to be invoked when scrolling ended.
     *
     * @param wheel the spinnerwheel view whose state has changed.
     */
    fun onScrollingFinished(wheel: AbstractWheel)
}
