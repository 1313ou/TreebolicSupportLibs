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
package org.treebolic.wheel.adapters;

import android.content.Context;

/**
 * The simple Array spinnerwheel adapter
 * 
 * @param <T>
 *            the element type
 */
public class ArrayWheelAdapter<T> extends AbstractWheelTextAdapter
{
	// items
	private final T[] items;

	/**
	 * Constructor
	 * 
	 * @param context0
	 *            the current context
	 * @param items0
	 *            the items
	 */
	public ArrayWheelAdapter(Context context0, T items0[])
	{
		super(context0);

		// setEmptyItemResource(TEXT_VIEW_ITEM_RESOURCE);
		this.items = items0;
	}

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
