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
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter extends AbstractWheelTextAdapter
{
	/** The default min value */
	public static final int DEFAULT_MAX_VALUE = 9;

	/** The default max value */
	private static final int DEFAULT_MIN_VALUE = 0;

	// Values
	private int minValue;
	private int maxValue;

	// format
	private String format;

	/**
	 * Constructor
	 * 
	 * @param context0
	 *            the current context
	 */
	public NumericWheelAdapter(Context context0)
	{
		this(context0, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	/**
	 * Constructor
	 * 
	 * @param context0
	 *            the current context
	 * @param minValue0
	 *            the spinnerwheel min value
	 * @param maxValue0
	 *            the spinnerwheel max value
	 */
	public NumericWheelAdapter(Context context0, int minValue0, int maxValue0)
	{
		this(context0, minValue0, maxValue0, null);
	}

	/**
	 * Constructor
	 * 
	 * @param context0
	 *            the current context
	 * @param minValue0
	 *            the spinnerwheel min value
	 * @param maxValue0
	 *            the spinnerwheel max value
	 * @param format0
	 *            the format string
	 */
	public NumericWheelAdapter(Context context0, int minValue0, int maxValue0, String format0)
	{
		super(context0);

		this.minValue = minValue0;
		this.maxValue = maxValue0;
		this.format = format0;
	}

	public void setMinValue(int minValue0)
	{
		this.minValue = minValue0;
		notifyDataInvalidatedEvent();
	}

	public void setMaxValue(int maxValue0)
	{
		this.maxValue = maxValue0;
		notifyDataInvalidatedEvent();
	}

	@SuppressWarnings("boxing")
	@Override
	public CharSequence getItemText(int index)
	{
		if (index >= 0 && index < getItemsCount())
		{
			int value = this.minValue + index;
			return this.format != null ? String.format(this.format, value) : Integer.toString(value);
		}
		return null;
	}

	@Override
	public int getItemsCount()
	{
		return this.maxValue - this.minValue + 1;
	}
}
