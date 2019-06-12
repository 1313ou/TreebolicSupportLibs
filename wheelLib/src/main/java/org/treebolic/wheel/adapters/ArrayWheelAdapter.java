/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.wheel.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The simple Array spinnerwheel adapter
 *
 * @param <T> the element type
 */
public class ArrayWheelAdapter<T> extends AbstractWheelTextAdapter
{
	// items
	private final T[] items;

	/**
	 * Constructor
	 *
	 * @param context0 the current context
	 * @param items0   the items
	 */
	public ArrayWheelAdapter(@NonNull Context context0, T[] items0)
	{
		super(context0);

		// setEmptyItemResource(TEXT_VIEW_ITEM_RESOURCE);
		this.items = items0;
	}

	@Nullable
	@Override
	public CharSequence getItemText(int index)
	{
		if (index >= 0 && index < this.items.length)
		{
			T item = this.items[index];
			if (item instanceof CharSequence)
			{
				return (CharSequence) item;
			}
			return item.toString();
		}
		return null;
	}

	@Override
	public int getItemsCount()
	{
		return this.items.length;
	}
}
