/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import org.treebolic.wheel.adapters.WheelViewAdapter;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Abstract spinner spinnerwheel view. This class should be subclassed.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
public abstract class AbstractWheel extends View
{
	// private final String TAG = AbstractWheel.class.getName() + " #" + (++itemID);
	// private static int itemID = -1;

	// ----------------------------------
	// Default properties values
	// ----------------------------------

	/**
	 * Default count of visible items
	 */
	private static final int DEF_VISIBLE_ITEMS = 4;
	private static final boolean DEF_IS_CYCLIC = false;

	// ----------------------------------
	// Class properties
	// ----------------------------------

	@SuppressWarnings("WeakerAccess")
	protected int mCurrentItemIdx = 0;

	// Count of visible items
	@SuppressWarnings("WeakerAccess")
	protected int mVisibleItems;

	// Should all items be visible
	@SuppressWarnings("WeakerAccess")
	protected boolean mIsAllVisible;

	@SuppressWarnings("WeakerAccess")
	protected boolean mIsCyclic;

	// Scrolling
	@SuppressWarnings("WeakerAccess")
	protected WheelScroller mScroller;
	@SuppressWarnings("WeakerAccess")
	protected boolean mIsScrollingPerformed;
	@SuppressWarnings("WeakerAccess")
	protected int mScrollingOffset;

	// Items layout
	@SuppressWarnings("WeakerAccess")
	protected LinearLayout mItemsLayout;

	// The number of first item in layout
	@SuppressWarnings("WeakerAccess")
	protected int mFirstItemIdx;

	// View adapter
	@SuppressWarnings("WeakerAccess")
	protected WheelViewAdapter mViewAdapter;

	@SuppressWarnings("WeakerAccess")
	protected int mLayoutHeight;
	@SuppressWarnings("WeakerAccess")
	protected int mLayoutWidth;

	// Recycle
	private final WheelRecycler mRecycler = new WheelRecycler(this);

	// Listeners
	private final List<OnWheelChangedListener> changingListeners = new LinkedList<>();
	private final List<OnWheelScrollListener> scrollingListeners = new LinkedList<>();
	private final List<OnWheelClickedListener> clickingListeners = new LinkedList<>();

	// XXX: I don't like listeners the way as they are now. -df

	// Adapter listener
	private DataSetObserver mDataObserver;

	// --------------------------------------------------------------------------
	//
	// Constructor
	//
	// --------------------------------------------------------------------------

	/**
	 * Create a new AbstractWheel instance
	 *
	 * @param context  the application environment.
	 * @param attrs    a collection of attributes.
	 * @param defStyle The default style to apply to this view.
	 */
	public AbstractWheel(@NonNull Context context, @Nullable final AttributeSet attrs, @AttrRes int defStyle)
	{
		super(context, attrs);
		initAttributes(context, attrs, defStyle);
		initData(context);
	}

	// --------------------------------------------------------------------------
	//
	// Initiating data and assets at start up
	//
	// --------------------------------------------------------------------------

	/**
	 * Initiates data and parameters from styles
	 *
	 * @param context  the application environment.
	 * @param attrs    a collection of attributes.
	 * @param defStyle The default style to apply to this view.
	 */
	@SuppressWarnings("WeakerAccess")
	protected void initAttributes(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes int defStyle)
	{
		if (attrs != null)
		{
			final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AbstractWheel, defStyle, 0);
			this.mVisibleItems = array.getInt(R.styleable.AbstractWheel_visibleItems, DEF_VISIBLE_ITEMS);
			this.mIsAllVisible = array.getBoolean(R.styleable.AbstractWheel_isAllVisible, false);
			this.mIsCyclic = array.getBoolean(R.styleable.AbstractWheel_isCyclic, DEF_IS_CYCLIC);
			array.recycle();
		}
	}

	/**
	 * Initiates data
	 *
	 * @param context the context
	 */
	@SuppressWarnings("WeakerAccess")
	protected void initData(@NonNull final Context context)
	{
		this.mDataObserver = new DataSetObserver()
		{
			@Override
			public void onChanged()
			{
				invalidateItemsLayout(false);
			}

			@Override
			public void onInvalidated()
			{
				invalidateItemsLayout(true);
			}
		};

		// creating new scroller
		this.mScroller = createScroller(new WheelScroller.ScrollingListener()
		{

			@Override
			public void onStarted()
			{
				AbstractWheel.this.mIsScrollingPerformed = true;
				notifyScrollingListenersAboutStart();
				onScrollStarted();
			}

			@Override
			public void onTouch()
			{
				onScrollTouched();
			}

			@Override
			public void onTouchUp()
			{
				if (!AbstractWheel.this.mIsScrollingPerformed)
				{
					onScrollTouchedUp(); // if scrolling IS performed, whe should use onFinished instead
				}
			}

			@Override
			public void onScroll(int distance)
			{
				doScroll(distance);

				int dimension = getBaseDimension();
				if (AbstractWheel.this.mScrollingOffset > dimension)
				{
					AbstractWheel.this.mScrollingOffset = dimension;
					AbstractWheel.this.mScroller.stopScrolling();
				}
				else if (AbstractWheel.this.mScrollingOffset < -dimension)
				{
					AbstractWheel.this.mScrollingOffset = -dimension;
					AbstractWheel.this.mScroller.stopScrolling();
				}
			}

			@Override
			public void onFinished()
			{
				if (AbstractWheel.this.mIsScrollingPerformed)
				{
					notifyScrollingListenersAboutEnd();
					AbstractWheel.this.mIsScrollingPerformed = false;
					onScrollFinished();
				}

				AbstractWheel.this.mScrollingOffset = 0;
				invalidate();
			}

			@Override
			public void onJustify()
			{
				if (Math.abs(AbstractWheel.this.mScrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING)
				{
					AbstractWheel.this.mScroller.scroll(AbstractWheel.this.mScrollingOffset, 0);
				}
			}
		});
	}

	@Override
	public Parcelable onSaveInstanceState()
	{
		// begin boilerplate code that allows parent classes to save state
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		// end

		ss.currentItem = this.getCurrentItem();

		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state)
	{
		// begin boilerplate code so parent classes can restore state
		if (!(state instanceof SavedState))
		{
			super.onRestoreInstanceState(state);
			return;
		}

		final SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		// end

		this.mCurrentItemIdx = ss.currentItem;

		// dirty hack to re-draw child items correctly
		postDelayed(() -> invalidateItemsLayout(false), 100);
	}

	static class SavedState extends BaseSavedState
	{
		int currentItem;

		SavedState(Parcelable superState)
		{
			super(superState);
		}

		private SavedState(@NonNull Parcel in)
		{
			super(in);
			this.currentItem = in.readInt();
		}

		@Override
		public void writeToParcel(@NonNull Parcel out, int flags)
		{
			super.writeToParcel(out, flags);
			out.writeInt(this.currentItem);
		}

		// required field that makes Parcelables from a Parcel
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
		{
			@NonNull
			@Override
			public SavedState createFromParcel(@NonNull Parcel in)
			{
				return new SavedState(in);
			}

			@NonNull
			@Override
			public SavedState[] newArray(int size)
			{
				return new SavedState[size];
			}
		};
	}

	abstract protected void recreateAssets(int width, int height);

	// --------------------------------------------------------------------------
	//
	// Scroller operations
	//
	// --------------------------------------------------------------------------

	/**
	 * Creates scroller appropriate for specific wheel implementation.
	 *
	 * @param scrollingListener listener to be passed to the scroller
	 * @return Initialized scroller to be used
	 */
	@NonNull
	abstract protected WheelScroller createScroller(WheelScroller.ScrollingListener scrollingListener);

	/* These methods are not abstract, as we may want to override only some of them */
	@SuppressWarnings({"EmptyMethod", "WeakerAccess"})
	protected void onScrollStarted()
	{
		//
	}

	@SuppressWarnings({"WeakerAccess", "EmptyMethod"})
	protected void onScrollTouched()
	{
		//
	}

	@SuppressWarnings({"WeakerAccess", "EmptyMethod"})
	protected void onScrollTouchedUp()
	{
		//
	}

	@SuppressWarnings({"WeakerAccess", "EmptyMethod"})
	protected void onScrollFinished()
	{
		//
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling()
	{
		this.mScroller.stopScrolling();
	}

	/**
	 * Set the the specified scrolling interpolator
	 *
	 * @param interpolator the interpolator
	 */
	public void setInterpolator(Interpolator interpolator)
	{
		this.mScroller.setInterpolator(interpolator);
	}

	/**
	 * Scroll the spinnerwheel
	 *
	 * @param itemsToScroll items to scroll
	 * @param time          scrolling duration
	 */
	@SuppressWarnings("WeakerAccess")
	public void scroll(int itemsToScroll, @SuppressWarnings("SameParameterValue") int time)
	{
		int distance = itemsToScroll * getItemDimension() - this.mScrollingOffset;
		onScrollTouched(); // we have to emulate touch when scrolling spinnerwheel programmatically to light up stuff
		this.mScroller.scroll(distance, time);
	}

	/**
	 * Scrolls the spinnerwheel
	 *
	 * @param delta the scrolling value
	 */
	private void doScroll(int delta)
	{
		this.mScrollingOffset += delta;

		int itemDimension = getItemDimension();
		int count = this.mScrollingOffset / itemDimension;

		int pos = this.mCurrentItemIdx - count;
		int itemCount = this.mViewAdapter.getItemsCount();

		int fixPos = this.mScrollingOffset % itemDimension;
		if (Math.abs(fixPos) <= itemDimension / 2)
		{
			fixPos = 0;
		}
		if (this.mIsCyclic && itemCount > 0)
		{
			if (fixPos > 0)
			{
				pos--;
				count++;
			}
			else if (fixPos < 0)
			{
				pos++;
				count--;
			}
			// fix position by rotating
			while (pos < 0)
			{
				pos += itemCount;
			}
			pos %= itemCount;
		}
		else
		{
			if (pos < 0)
			{
				count = this.mCurrentItemIdx;
				pos = 0;
			}
			else if (pos >= itemCount)
			{
				count = this.mCurrentItemIdx - itemCount + 1;
				pos = itemCount - 1;
			}
			else if (pos > 0 && fixPos > 0)
			{
				pos--;
				count++;
			}
			else if (pos < itemCount - 1 && fixPos < 0)
			{
				pos++;
				count--;
			}
		}

		int offset = this.mScrollingOffset;
		if (pos != this.mCurrentItemIdx)
		{
			setCurrentItem(pos, false);
		}
		else
		{
			invalidate();
		}

		// update offset
		int baseDimension = getBaseDimension();
		this.mScrollingOffset = offset - count * itemDimension;
		if (this.mScrollingOffset > baseDimension)
		{
			this.mScrollingOffset = this.mScrollingOffset % baseDimension + baseDimension;
		}
	}

	// --------------------------------------------------------------------------
	//
	// Base measurements
	//
	// --------------------------------------------------------------------------

	/**
	 * Returns base dimension of the spinnerwheel — width for horizontal spinnerwheel, height for vertical
	 *
	 * @return width or height of the spinnerwheel
	 */
	abstract protected int getBaseDimension();

	/**
	 * Returns base dimension of base item — width for horizontal spinnerwheel, height for vertical
	 *
	 * @return width or height of base item
	 */
	abstract protected int getItemDimension();

	/**
	 * Processes MotionEvent and returns relevant position — x for horizontal spinnerwheel, y for vertical
	 *
	 * @param event MotionEvent to be processed
	 * @return relevant position of the MotionEvent
	 */
	abstract protected float getMotionEventPosition(MotionEvent event);

	// --------------------------------------------------------------------------
	//
	// Layout creation and measurement operations
	//
	// --------------------------------------------------------------------------

	/**
	 * Creates item layouts if necessary
	 */
	abstract protected void createItemsLayout();

	/**
	 * Sets layout width and height
	 */
	abstract protected void doItemsLayout();

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		if (changed)
		{
			int w = r - l;
			int h = b - t;
			doItemsLayout();
			if (this.mLayoutWidth != w || this.mLayoutHeight != h)
			{
				recreateAssets(getMeasuredWidth(), getMeasuredHeight());
			}
			this.mLayoutWidth = w;
			this.mLayoutHeight = h;
		}
	}

	/**
	 * Invalidates items layout
	 *
	 * @param clearCaches if true then cached views will be cleared
	 */
	@SuppressWarnings("WeakerAccess")
	public void invalidateItemsLayout(boolean clearCaches)
	{
		if (clearCaches)
		{
			this.mRecycler.clearAll();
			if (this.mItemsLayout != null)
			{
				this.mItemsLayout.removeAllViews();
			}
			this.mScrollingOffset = 0;
		}
		else if (this.mItemsLayout != null)
		{
			// cache all items
			this.mRecycler.recycleItems(this.mItemsLayout, this.mFirstItemIdx, new ItemsRange());
		}
		invalidate();
	}

	// --------------------------------------------------------------------------
	//
	// Getters and setters
	//
	// --------------------------------------------------------------------------

	/**
	 * Gets count of visible items
	 *
	 * @return the count of visible items
	 */
	public int getVisibleItems()
	{
		return this.mVisibleItems;
	}

	/**
	 * Sets the desired count of visible items. Actual amount of visible items depends on spinnerwheel layout parameters. To apply changes and rebuild view call
	 * measure().
	 *
	 * @param count the desired count for visible items
	 */
	public void setVisibleItems(@SuppressWarnings("SameParameterValue") int count)
	{
		this.mVisibleItems = count;
	}

	/**
	 * Sets all items to have no dim and makes them visible
	 *
	 * @param isAllVisible true if all items are to be visible
	 */
	public void setAllItemsVisible(boolean isAllVisible)
	{
		this.mIsAllVisible = isAllVisible;
		invalidateItemsLayout(false);
	}

	/**
	 * Gets view adapter
	 *
	 * @return the view adapter
	 */
	public WheelViewAdapter getViewAdapter()
	{
		return this.mViewAdapter;
	}

	/**
	 * Sets view adapter. Usually new adapters contain different views, so it needs to rebuild view by calling measure().
	 *
	 * @param viewAdapter the view adapter
	 */
	public void setViewAdapter(WheelViewAdapter viewAdapter)
	{
		if (this.mViewAdapter != null)
		{
			this.mViewAdapter.unregisterDataSetObserver(this.mDataObserver);
		}
		this.mViewAdapter = viewAdapter;
		if (this.mViewAdapter != null)
		{
			this.mViewAdapter.registerDataSetObserver(this.mDataObserver);
		}
		invalidateItemsLayout(true);
	}

	/**
	 * Gets current value
	 *
	 * @return the current value
	 */
	public int getCurrentItem()
	{
		return this.mCurrentItemIdx;
	}

	/**
	 * Sets the current item. Does nothing when index is wrong.
	 *
	 * @param index0   the item index
	 * @param animated the animation flag
	 */
	@SuppressWarnings("WeakerAccess")
	public void setCurrentItem(int index0, @SuppressWarnings("SameParameterValue") boolean animated)
	{
		int index = index0;
		if (this.mViewAdapter == null || this.mViewAdapter.getItemsCount() == 0)
		{
			return; // throw?
		}

		int itemCount = this.mViewAdapter.getItemsCount();
		if (index < 0 || index >= itemCount)
		{
			if (this.mIsCyclic)
			{
				while (index < 0)
				{
					index += itemCount;
				}
				index %= itemCount;
			}
			else
			{
				return; // throw?
			}
		}
		if (index != this.mCurrentItemIdx)
		{
			if (animated)
			{
				int itemsToScroll = index - this.mCurrentItemIdx;
				if (this.mIsCyclic)
				{
					int scroll = itemCount + Math.min(index, this.mCurrentItemIdx) - Math.max(index, this.mCurrentItemIdx);
					if (scroll < Math.abs(itemsToScroll))
					{
						itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
					}
				}
				scroll(itemsToScroll, 0);
			}
			else
			{
				this.mScrollingOffset = 0;
				final int old = this.mCurrentItemIdx;
				this.mCurrentItemIdx = index;
				notifyChangingListeners(old, this.mCurrentItemIdx);
				invalidate();
			}
		}
	}

	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 *
	 * @param index the item index
	 */
	public void setCurrentItem(int index)
	{
		setCurrentItem(index, false);
	}

	/**
	 * Tests if spinnerwheel is cyclic. That means before the 1st item there is shown the last one
	 *
	 * @return true if spinnerwheel is cyclic
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isCyclic()
	{
		return this.mIsCyclic;
	}

	/**
	 * Set spinnerwheel cyclic flag
	 *
	 * @param isCyclic the flag to set
	 */
	public void setCyclic(boolean isCyclic)
	{
		this.mIsCyclic = isCyclic;
		invalidateItemsLayout(false);
	}

	// --------------------------------------------------------------------------
	//
	// Listener operations
	//
	// --------------------------------------------------------------------------

	/**
	 * Adds spinnerwheel changing listener
	 *
	 * @param listener the listener
	 */
	public void addChangingListener(OnWheelChangedListener listener)
	{
		this.changingListeners.add(listener);
	}

	/**
	 * Removes spinnerwheel changing listener
	 *
	 * @param listener the listener
	 */
	public void removeChangingListener(OnWheelChangedListener listener)
	{
		this.changingListeners.remove(listener);
	}

	/**
	 * Notifies changing listeners
	 *
	 * @param oldValue the old spinnerwheel value
	 * @param newValue the new spinnerwheel value
	 */
	@SuppressWarnings("WeakerAccess")
	protected void notifyChangingListeners(int oldValue, int newValue)
	{
		for (OnWheelChangedListener listener : this.changingListeners)
		{
			listener.onChanged(this, oldValue, newValue);
		}
	}

	/**
	 * Adds spinnerwheel scrolling listener
	 *
	 * @param listener the listener
	 */
	public void addScrollingListener(OnWheelScrollListener listener)
	{
		this.scrollingListeners.add(listener);
	}

	/**
	 * Removes spinnerwheel scrolling listener
	 *
	 * @param listener the listener
	 */
	public void removeScrollingListener(OnWheelScrollListener listener)
	{
		this.scrollingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about starting scrolling
	 */
	@SuppressWarnings("WeakerAccess")
	protected void notifyScrollingListenersAboutStart()
	{
		for (OnWheelScrollListener listener : this.scrollingListeners)
		{
			listener.onScrollingStarted(this);
		}
	}

	/**
	 * Notifies listeners about ending scrolling
	 */
	@SuppressWarnings("WeakerAccess")
	protected void notifyScrollingListenersAboutEnd()
	{
		for (OnWheelScrollListener listener : this.scrollingListeners)
		{
			listener.onScrollingFinished(this);
		}
	}

	/**
	 * Adds spinnerwheel clicking listener
	 *
	 * @param listener the listener
	 */
	public void addClickingListener(OnWheelClickedListener listener)
	{
		this.clickingListeners.add(listener);
	}

	/**
	 * Removes spinnerwheel clicking listener
	 *
	 * @param listener the listener
	 */
	public void removeClickingListener(OnWheelClickedListener listener)
	{
		this.clickingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about clicking
	 *
	 * @param item clicked item
	 */
	@SuppressWarnings("WeakerAccess")
	protected void notifyClickListenersAboutClick(int item)
	{
		for (OnWheelClickedListener listener : this.clickingListeners)
		{
			listener.onItemClicked(this, item);
		}
	}

	// --------------------------------------------------------------------------
	//
	// Rebuilding items
	//
	// --------------------------------------------------------------------------

	/**
	 * Rebuilds spinnerwheel items if necessary. Caches all unused items.
	 *
	 * @return true if items are rebuilt
	 */
	@SuppressWarnings("WeakerAccess")
	protected boolean rebuildItems()
	{
		boolean updated;
		ItemsRange range = getItemsRange();

		if (this.mItemsLayout != null)
		{
			int first = this.mRecycler.recycleItems(this.mItemsLayout, this.mFirstItemIdx, range);
			updated = this.mFirstItemIdx != first;
			this.mFirstItemIdx = first;
		}
		else
		{
			createItemsLayout();
			updated = true;
		}

		if (!updated)
		{
			updated = this.mFirstItemIdx != range.getFirst() || this.mItemsLayout.getChildCount() != range.getCount();
		}

		if (this.mFirstItemIdx > range.getFirst() && this.mFirstItemIdx <= range.getLast())
		{
			for (int i = this.mFirstItemIdx - 1; i >= range.getFirst(); i--)
			{
				if (!addItemView(i, true))
				{
					break;
				}
				this.mFirstItemIdx = i;
			}
		}
		else
		{
			this.mFirstItemIdx = range.getFirst();
		}

		int first = this.mFirstItemIdx;
		for (int i = this.mItemsLayout.getChildCount(); i < range.getCount(); i++)
		{
			if (!addItemView(this.mFirstItemIdx + i, false) && this.mItemsLayout.getChildCount() == 0)
			{
				first++;
			}
		}
		this.mFirstItemIdx = first;

		return updated;
	}

	// ----------------------------------
	// ItemsRange operations
	// ----------------------------------

	/**
	 * Calculates range for spinnerwheel items
	 *
	 * @return the items range
	 */
	@NonNull
	private ItemsRange getItemsRange()
	{
		if (this.mIsAllVisible)
		{
			int baseDimension = getBaseDimension();
			int itemDimension = getItemDimension();
			if (itemDimension != 0)
			{
				this.mVisibleItems = baseDimension / itemDimension + 1;
			}
		}

		int start = this.mCurrentItemIdx - this.mVisibleItems / 2;
		int end = start + this.mVisibleItems - (this.mVisibleItems % 2 == 0 ? 0 : 1);
		if (this.mScrollingOffset != 0)
		{
			if (this.mScrollingOffset > 0)
			{
				start--;
			}
			else
			{
				end++;
			}
		}
		if (!isCyclic())
		{
			if (start < 0)
			{
				start = 0;
			}
			if (this.mViewAdapter == null)
			{
				end = 0;
			}
			else if (end > this.mViewAdapter.getItemsCount())
			{
				end = this.mViewAdapter.getItemsCount();
			}
		}
		return new ItemsRange(start, end - start + 1);
	}

	/**
	 * Checks whether item index is valid
	 *
	 * @param index the item index
	 * @return true if item index is not out of bounds or the spinnerwheel is cyclic
	 */
	@SuppressWarnings("WeakerAccess")
	protected boolean isValidItemIndex(int index)
	{
		return (this.mViewAdapter != null) && (this.mViewAdapter.getItemsCount() > 0) && (this.mIsCyclic || (index >= 0 && index < this.mViewAdapter.getItemsCount()));
	}

	// ----------------------------------
	// Operations with item view
	// ----------------------------------

	/**
	 * Adds view for item to items layout
	 *
	 * @param index the item index
	 * @param first the flag indicates if view should be first
	 * @return true if corresponding item exists and is added
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean addItemView(int index, boolean first)
	{
		View view = getItemView(index);
		if (view != null)
		{
			if (first)
			{
				this.mItemsLayout.addView(view, 0);
			}
			else
			{
				this.mItemsLayout.addView(view);
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns view for specified item
	 *
	 * @param index0 the item index
	 * @return item view or empty view if index is out of bounds
	 */
	@Nullable
	private View getItemView(int index0)
	{
		int index = index0;
		if (this.mViewAdapter == null || this.mViewAdapter.getItemsCount() == 0)
		{
			return null;
		}
		int count = this.mViewAdapter.getItemsCount();
		if (!isValidItemIndex(index))
		{
			return this.mViewAdapter.getEmptyItem(this.mRecycler.getEmptyItem(), this.mItemsLayout);
		}
		while (index < 0)
		{
			index = count + index;
		}
		index %= count;
		return this.mViewAdapter.getItem(index, this.mRecycler.getItem(), this.mItemsLayout);
	}

	// --------------------------------------------------------------------------
	//
	// Intercepting and processing touch event
	//
	// --------------------------------------------------------------------------

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event)
	{
		if (!isEnabled() || getViewAdapter() == null)
		{
			return true;
		}

		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				if (getParent() != null)
				{
					getParent().requestDisallowInterceptTouchEvent(true);
				}
				break;

			case MotionEvent.ACTION_UP:
				if (!this.mIsScrollingPerformed)
				{
					int distance = (int) getMotionEventPosition(event) - getBaseDimension() / 2;
					if (distance > 0)
					{
						distance += getItemDimension() / 2;
					}
					else
					{
						distance -= getItemDimension() / 2;
					}
					int items = distance / getItemDimension();
					if (items != 0 && isValidItemIndex(this.mCurrentItemIdx + items))
					{
						notifyClickListenersAboutClick(this.mCurrentItemIdx + items);
					}
				}
				break;
			default:
				break;
		}
		return this.mScroller.onTouchEvent(event);
	}

}
