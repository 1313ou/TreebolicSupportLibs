/*
 * android-spinnerwheel
 * https://github.com/ai212983/android-spinnerwheel
 *
 * based on
 *
 * Android Wheel Control.
 * https://code.google.com/p/android-wheel/
 *
 * Copyright 2011 Yuri Kanivets
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.treebolic.wheel;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Scroller class handles scrolling events and updates the spinnerwheel
 */
public abstract class WheelScroller
{
	/**
	 * Scrolling listener interface
	 */
	public interface ScrollingListener
	{
		/**
		 * Scrolling callback called when scrolling is performed.
		 * 
		 * @param distance
		 *            the distance to scroll
		 */
		void onScroll(int distance);

		/**
		 * This callback is invoked when scroller has been touched
		 */
		void onTouch();

		/**
		 * This callback is invoked when touch is up
		 */
		void onTouchUp();

		/**
		 * Starting callback called when scrolling is started
		 */
		void onStarted();

		/**
		 * Finishing callback called after justifying
		 */
		void onFinished();

		/**
		 * Justifying callback called to justify a view when scrolling is ended
		 */
		void onJustify();
	}

	private static class AnimationHandler extends Handler
	{
		private final WeakReference<WheelScroller> wheelScrollerRef;

		public AnimationHandler(WheelScroller scroller0)
		{
			this.wheelScrollerRef = new WeakReference<>(scroller0);
		}

		@Override
		public void handleMessage(Message msg)
		{
			final WheelScroller wheelScroller = this.wheelScrollerRef.get();
			if (wheelScroller != null)
			{
				wheelScroller.scroller.computeScrollOffset();
				int currPosition = wheelScroller.getCurrentScrollerPosition();
				int delta = wheelScroller.lastScrollPosition - currPosition;
				wheelScroller.lastScrollPosition = currPosition;
				if (delta != 0)
				{
					wheelScroller.listener.onScroll(delta);
				}

				// scrolling is not finished when it comes to final Y
				// so, finish it manually
				if (Math.abs(currPosition - wheelScroller.getFinalScrollerPosition()) < MIN_DELTA_FOR_SCROLLING)
				{
					// currPosition = getFinalScrollerPosition();
					wheelScroller.scroller.forceFinished(true);
				}
				if (!wheelScroller.scroller.isFinished())
				{
					wheelScroller.animationHandler.sendEmptyMessage(msg.what);
				}
				else if (msg.what == wheelScroller.MESSAGE_SCROLL)
				{
					wheelScroller.justify();
				}
				else
				{
					wheelScroller.finishScrolling();
				}
			}
		}
	}

	/** Animation handler */
	private final Handler animationHandler = new AnimationHandler(this);

	/** Scrolling duration */
	private static final int SCROLLING_DURATION = 400;

	/** Minimum delta for scrolling */
	public static final int MIN_DELTA_FOR_SCROLLING = 1;

	// Listener
	private final ScrollingListener listener;

	// Context
	private final Context context;

	// Scrolling
	private final GestureDetector gestureDetector;
	@SuppressWarnings("WeakerAccess")
	protected Scroller scroller;
	private int lastScrollPosition;
	private float lastTouchedPosition;
	private boolean isScrollingPerformed;

	/**
	 * Constructor
	 * 
	 * @param context0
	 *            the current context
	 * @param listener0
	 *            the scrolling listener
	 */
	@SuppressWarnings("WeakerAccess")
	public WheelScroller(Context context0, ScrollingListener listener0)
	{
		this.gestureDetector = new GestureDetector(context0, new SimpleOnGestureListener()
		{
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
			{
				// Do scrolling in onTouchEvent() since onScroll() are not call immediately
				// when user touch and move the spinnerwheel
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
			{
				WheelScroller.this.lastScrollPosition = 0;
				scrollerFling(WheelScroller.this.lastScrollPosition, (int) velocityX, (int) velocityY);
				setNextMessage(WheelScroller.this.MESSAGE_SCROLL);
				return true;
			}

			// public boolean onDown(MotionEvent motionEvent);

		});
		this.gestureDetector.setIsLongpressEnabled(false);

		this.scroller = new Scroller(context0);

		this.listener = listener0;
		this.context = context0;
	}

	/**
	 * Set the the specified scrolling interpolator
	 * 
	 * @param interpolator
	 *            the interpolator
	 */
	public void setInterpolator(Interpolator interpolator)
	{
		this.scroller.forceFinished(true);
		this.scroller = new Scroller(this.context, interpolator);
	}

	/**
	 * Scroll the spinnerwheel
	 * 
	 * @param distance
	 *            the scrolling distance
	 * @param time
	 *            the scrolling duration
	 */
	public void scroll(int distance, int time)
	{
		this.scroller.forceFinished(true);
		this.lastScrollPosition = 0;
		scrollerStartScroll(distance, time != 0 ? time : SCROLLING_DURATION);
		setNextMessage(this.MESSAGE_SCROLL);
		startScrolling();
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling()
	{
		this.scroller.forceFinished(true);
	}

	/**
	 * Handles Touch event
	 * 
	 * @param event
	 *            the motion event
	 * @return true if the event was handled, false otherwise.
	 */
	@SuppressWarnings("SameReturnValue")
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			this.lastTouchedPosition = getMotionEventPosition(event);
			this.scroller.forceFinished(true);
			clearMessages();
			this.listener.onTouch();
			break;

		case MotionEvent.ACTION_UP:
			if (this.scroller.isFinished())
				this.listener.onTouchUp();
			break;

		case MotionEvent.ACTION_MOVE:
			// perform scrolling
			int distance = (int) (getMotionEventPosition(event) - this.lastTouchedPosition);
			if (distance != 0)
			{
				startScrolling();
				this.listener.onScroll(distance);
				this.lastTouchedPosition = getMotionEventPosition(event);
			}
			break;

		default:
			break;
		}

		if (!this.gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP)
		{
			justify();
		}

		return true;
	}

	// Messages
	private final int MESSAGE_SCROLL = 0;
	private final int MESSAGE_JUSTIFY = 1;

	/**
	 * Set next message to queue. Clears queue before.
	 * 
	 * @param message
	 *            the message to set
	 */
	private void setNextMessage(int message)
	{
		clearMessages();
		this.animationHandler.sendEmptyMessage(message);
	}

	/**
	 * Clears messages from queue
	 */
	private void clearMessages()
	{
		this.animationHandler.removeMessages(this.MESSAGE_SCROLL);
		this.animationHandler.removeMessages(this.MESSAGE_JUSTIFY);
	}

	/**
	 * Justifies spinnerwheel
	 */
	@SuppressWarnings("WeakerAccess")
	public void justify()
	{
		this.listener.onJustify();
		setNextMessage(this.MESSAGE_JUSTIFY);
	}

	/**
	 * Starts scrolling
	 */
	private void startScrolling()
	{
		if (!this.isScrollingPerformed)
		{
			this.isScrollingPerformed = true;
			this.listener.onStarted();
		}
	}

	/**
	 * Finishes scrolling
	 */
	@SuppressWarnings("WeakerAccess")
	protected void finishScrolling()
	{
		if (this.isScrollingPerformed)
		{
			this.listener.onFinished();
			this.isScrollingPerformed = false;
		}
	}

	protected abstract int getCurrentScrollerPosition();

	protected abstract int getFinalScrollerPosition();

	protected abstract float getMotionEventPosition(MotionEvent event);

	protected abstract void scrollerStartScroll(int distance, int time);

	protected abstract void scrollerFling(int position, int velocityX, int velocityY);
}
