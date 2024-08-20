/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.animation.Interpolator
import android.widget.Scroller
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * Scroller class handles scrolling events and updates the spinnerwheel
 * @param context0  the current context
 * @param listener0 the scrolling listener
 */
abstract class WheelScroller(context0: Context?, listener0: ScrollingListener) {

    /**
     * Scrolling listener interface
     */
    interface ScrollingListener {

        /**
         * Scrolling callback called when scrolling is performed.
         *
         * @param distance the distance to scroll
         */
        fun onScroll(distance: Int)

        /**
         * This callback is invoked when scroller has been touched
         */
        fun onTouch()

        /**
         * This callback is invoked when touch is up
         */
        fun onTouchUp()

        /**
         * Starting callback called when scrolling is started
         */
        fun onStarted()

        /**
         * Finishing callback called after justifying
         */
        fun onFinished()

        /**
         * Justifying callback called to justify a view when scrolling is ended
         */
        fun onJustify()
    }

    private class AnimationHandler(scroller0: WheelScroller) : Handler(Looper.getMainLooper()) {

        private val wheelScrollerRef = WeakReference(scroller0)

        override fun handleMessage(msg: Message) {
            val wheelScroller = wheelScrollerRef.get()
            if (wheelScroller != null) {
                wheelScroller.scroller.computeScrollOffset()
                val currPosition = wheelScroller.currentScrollerPosition
                val delta = wheelScroller.lastScrollPosition - currPosition
                wheelScroller.lastScrollPosition = currPosition
                if (delta != 0) {
                    wheelScroller.listener.onScroll(delta)
                }

                // scrolling is not finished when it comes to final Y. So, finish it manually
                if (abs((currPosition - wheelScroller.finalScrollerPosition).toDouble()) < MIN_DELTA_FOR_SCROLLING) {
                    wheelScroller.scroller.forceFinished(true)
                }
                if (!wheelScroller.scroller.isFinished) {
                    wheelScroller.animationHandler.sendEmptyMessage(msg.what)
                } else if (msg.what == wheelScroller.messageScroll) {
                    wheelScroller.justify()
                } else {
                    wheelScroller.finishScrolling()
                }
            }
        }
    }

    /**
     * Animation handler
     */
    private val animationHandler: Handler = AnimationHandler(this)

    // Listener
    private val listener: ScrollingListener

    // Context
    private val context: Context?

    // Scrolling
    private val gestureDetector: GestureDetector

    @JvmField
    protected var scroller: Scroller

    private var lastScrollPosition = 0

    private var lastTouchedPosition = 0f

    private var isScrollingPerformed = false

    /**
     * Set the the specified scrolling interpolator
     *
     * @param interpolator the interpolator
     */
    fun setInterpolator(interpolator: Interpolator?) {
        scroller.forceFinished(true)
        scroller = Scroller(context, interpolator)
    }

    /**
     * Scroll the spinnerwheel
     *
     * @param distance the scrolling distance
     * @param time     the scrolling duration
     */
    fun scroll(distance: Int, time: Int) {
        scroller.forceFinished(true)
        lastScrollPosition = 0
        scrollerStartScroll(distance, if (time != 0) time else SCROLLING_DURATION)
        setNextMessage(messageScroll)
        startScrolling()
    }

    /**
     * Stops scrolling
     */
    fun stopScrolling() {
        scroller.forceFinished(true)
    }

    /**
     * Handles Touch event
     *
     * @param event the motion event
     * @return true if the event was handled, false otherwise.
     */
    @Suppress("SameReturnValue")
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchedPosition = getMotionEventPosition(event)
                scroller.forceFinished(true)
                clearMessages()
                listener.onTouch()
            }

            MotionEvent.ACTION_UP -> if (scroller.isFinished) {
                listener.onTouchUp()
            }

            MotionEvent.ACTION_MOVE -> {
                // perform scrolling
                val distance = (getMotionEventPosition(event) - lastTouchedPosition).toInt()
                if (distance != 0) {
                    startScrolling()
                    listener.onScroll(distance)
                    lastTouchedPosition = getMotionEventPosition(event)
                }
            }

            else -> {}
        }
        if (!gestureDetector.onTouchEvent(event) && event.action == MotionEvent.ACTION_UP) {
            justify()
        }

        return true
    }

    // Messages

    private val messageScroll = 0

    private val messageJustify = 1

    init {
        gestureDetector = GestureDetector(context0, object : SimpleOnGestureListener() {

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                // Do scrolling in onTouchEvent() since onScroll() are not call immediately when user touch and move the spinnerwheel
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                this@WheelScroller.lastScrollPosition = 0
                scrollerFling(this@WheelScroller.lastScrollPosition, velocityX.toInt(), velocityY.toInt())
                setNextMessage(this@WheelScroller.messageScroll)
                return true
            }
        })
        gestureDetector.setIsLongpressEnabled(false)

        scroller = Scroller(context0)

        listener = listener0
        context = context0
    }

    /**
     * Set next message to queue. Clears queue before.
     *
     * @param message the message to set
     */
    private fun setNextMessage(message: Int) {
        clearMessages()
        animationHandler.sendEmptyMessage(message)
    }

    /**
     * Clears messages from queue
     */
    private fun clearMessages() {
        animationHandler.removeMessages(messageScroll)
        animationHandler.removeMessages(messageJustify)
    }

    /**
     * Justifies spinnerwheel
     */
    fun justify() {
        listener.onJustify()
        setNextMessage(messageJustify)
    }

    /**
     * Starts scrolling
     */
    private fun startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true
            listener.onStarted()
        }
    }

    /**
     * Finishes scrolling
     */
    protected fun finishScrolling() {
        if (isScrollingPerformed) {
            listener.onFinished()
            isScrollingPerformed = false
        }
    }

    protected abstract val currentScrollerPosition: Int

    protected abstract val finalScrollerPosition: Int

    protected abstract fun getMotionEventPosition(event: MotionEvent): Float

    protected abstract fun scrollerStartScroll(distance: Int, time: Int)

    protected abstract fun scrollerFling(position: Int, velocityX: Int, velocityY: Int)

    companion object {

        /**
         * Scrolling duration
         */
        private const val SCROLLING_DURATION = 400

        /**
         * Minimum delta for scrolling
         */
        const val MIN_DELTA_FOR_SCROLLING: Int = 1
    }
}
