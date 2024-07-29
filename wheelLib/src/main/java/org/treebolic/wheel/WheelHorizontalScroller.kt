/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

import android.content.Context
import android.view.MotionEvent

/**
 * Constructor
 *
 * @param context  the current context
 * @param listener the scrolling listener
 */
class WheelHorizontalScroller(context: Context?, listener: ScrollingListener?) : WheelScroller(context, listener!!) {

    override val currentScrollerPosition: Int
        get() = scroller.currX

    override val finalScrollerPosition: Int
        get() = scroller.finalX

    override fun getMotionEventPosition(event: MotionEvent): Float {
        return event.x
    }

    override fun scrollerStartScroll(distance: Int, time: Int) {
        scroller.startScroll(0, 0, distance, 0, time)
    }

    override fun scrollerFling(position: Int, velocityX: Int, velocityY: Int) {
        val maxPosition = 0x7FFFFFFF
        val minPosition = -maxPosition
        scroller.fling(position, 0, -velocityX, 0, minPosition, maxPosition, 0, 0)
    }
}
