/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

import android.content.Context
import android.view.MotionEvent

/**
 * Scroller class handles scrolling events and updates the
 *
 * @param context  the current context
 * @param listener the scrolling listener
 */
class WheelVerticalScroller(context: Context?, listener: ScrollingListener?) : WheelScroller(context, listener!!) {

    override val currentScrollerPosition: Int
        get() = scroller.currY

    override val finalScrollerPosition: Int
        get() = scroller.finalY

    override fun getMotionEventPosition(event: MotionEvent): Float {
        // should be overridden
        return event.y
    }

    override fun scrollerStartScroll(distance: Int, time: Int) {
        scroller.startScroll(0, 0, 0, distance, time)
    }

    override fun scrollerFling(position: Int, velocityX: Int, velocityY: Int) {
        val maxPosition = 0x7FFFFFFF
        val minPosition = -maxPosition
        scroller.fling(0, position, 0, -velocityY, 0, 0, minPosition, maxPosition)
    }
}
