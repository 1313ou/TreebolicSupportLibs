/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.wheel;

import android.view.View;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Recycle stored spinnerwheel items to reuse.
 */
@SuppressWarnings("WeakerAccess")
public class WheelRecycler
{
	// private static final String TAG = WheelRecycler.class.getName();

	// Cached items
	private List<View> items;

	// Cached empty items
	private List<View> emptyItems;

	// Wheel view
	private final AbstractWheel wheel;

	/**
	 * Constructor
	 *
	 * @param wheel0 the spinnerwheel view
	 */
	public WheelRecycler(AbstractWheel wheel0)
	{
		this.wheel = wheel0;
	}

	/**
	 * Recycles items from specified layout. There are saved only items not included to specified range. All the cached items are removed from original layout.
	 *
	 * @param layout     the layout containing items to be cached
	 * @param firstItem0 the number of first item in layout
	 * @param range      the range of current spinnerwheel items
	 * @return the new value of first item number
	 */
	public int recycleItems(@NonNull LinearLayout layout, int firstItem0, @NonNull ItemsRange range)
	{
		int firstItem = firstItem0;
		int index = firstItem;
		for (int i = 0; i < layout.getChildCount(); )
		{
			if (!range.contains(index))
			{
				recycleView(layout.getChildAt(i), index);
				layout.removeViewAt(i);
				if (i == 0)
				{ // first item
					firstItem++;
				}
			}
			else
			{
				i++; // go to next item
			}
			index++;
		}
		return firstItem;
	}

	/**
	 * Gets item view
	 *
	 * @return the cached view
	 */
	@Nullable
	public View getItem()
	{
		return getCachedView(this.items);
	}

	/**
	 * Gets empty item view
	 *
	 * @return the cached empty view
	 */
	@Nullable
	public View getEmptyItem()
	{
		return getCachedView(this.emptyItems);
	}

	/**
	 * Clears all views
	 */
	public void clearAll()
	{
		if (this.items != null)
		{
			this.items.clear();
		}
		if (this.emptyItems != null)
		{
			this.emptyItems.clear();
		}
	}

	/**
	 * Adds view to specified cache. Creates a cache list if it is null.
	 *
	 * @param view   the view to be cached
	 * @param cache0 the cache list
	 * @return the cache list
	 */
	private static List<View> addView(View view, List<View> cache0)
	{
		List<View> cache = cache0;
		if (cache == null)
		{
			cache = new LinkedList<>();
		}

		cache.add(view);
		return cache;
	}

	/**
	 * Adds view to cache. Determines view type (item view or empty one) by index.
	 *
	 * @param view   the view to be cached
	 * @param index0 the index of view
	 */
	private void recycleView(View view, int index0)
	{
		int count = this.wheel.getViewAdapter().getItemsCount();

		int index = index0;
		if ((index < 0 || index >= count) && !this.wheel.isCyclic())
		{
			// empty view
			this.emptyItems = addView(view, this.emptyItems);
		}
		else
		{
			while (index < 0)
			{
				index = count + index;
			}
			this.items = addView(view, this.items);
		}
	}

	/**
	 * Gets view from specified cache.
	 *
	 * @param cache the cache
	 * @return the first view from cache.
	 */
	@Nullable
	private static View getCachedView(@Nullable final List<View> cache)
	{
		if (cache != null && cache.size() > 0)
		{
			View view = cache.get(0);
			cache.remove(0);
			return view;
		}
		return null;
	}

}
