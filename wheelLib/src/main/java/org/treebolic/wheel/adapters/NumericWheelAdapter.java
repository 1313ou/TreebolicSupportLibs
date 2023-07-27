/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.wheel.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Numeric Wheel adapter.
 * @noinspection WeakerAccess
 */
public class NumericWheelAdapter extends AbstractWheelTextAdapter
{
	/**
	 * The default min value
	 */
	@SuppressWarnings("WeakerAccess")
	public static final int DEFAULT_MAX_VALUE = 9;

	/**
	 * The default max value
	 */
	private static final int DEFAULT_MIN_VALUE = 0;

	// Values
	private int minValue;
	private int maxValue;

	// format
	private final String format;

	/**
	 * Constructor
	 *
	 * @param context0 the current context
	 */
	public NumericWheelAdapter(@NonNull Context context0)
	{
		this(context0, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	/**
	 * Constructor
	 *
	 * @param context0  the current context
	 * @param minValue0 the spinnerwheel min value
	 * @param maxValue0 the spinnerwheel max value
	 */
	@SuppressWarnings("WeakerAccess")
	public NumericWheelAdapter(@NonNull Context context0, @SuppressWarnings("SameParameterValue") int minValue0, @SuppressWarnings("SameParameterValue") int maxValue0)
	{
		this(context0, minValue0, maxValue0, null);
	}

	/**
	 * Constructor
	 *
	 * @param context0  the current context
	 * @param minValue0 the spinnerwheel min value
	 * @param maxValue0 the spinnerwheel max value
	 * @param format0   the format string
	 */
	@SuppressWarnings("WeakerAccess")
	public NumericWheelAdapter(@NonNull Context context0, int minValue0, int maxValue0, @SuppressWarnings("SameParameterValue") String format0)
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

	@Nullable
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

	@SuppressWarnings("WeakerAccess")
	@Override
	public int getItemsCount()
	{
		return this.maxValue - this.minValue + 1;
	}
}
