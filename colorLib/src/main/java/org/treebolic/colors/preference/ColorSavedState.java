/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.colors.preference;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

class ColorSavedState extends Preference.BaseSavedState
{
	/**
	 * The value
	 */
	boolean isNull;
	int value;

	/**
	 * Constructor from superstate
	 *
	 * @param superState superstate
	 */
	@SuppressWarnings("WeakerAccess")
	public ColorSavedState(final Parcelable superState)
	{
		super(superState);
	}

	/**
	 * Constructor from parcel
	 *
	 * @param parcel source parcel
	 */
	@SuppressWarnings("WeakerAccess")
	public ColorSavedState(@NonNull final Parcel parcel)
	{
		super(parcel);

		// get the preference's value
		this.isNull = parcel.readInt() != 0;
		this.value = parcel.readInt();
	}

	@Override
	public void writeToParcel(@NonNull final Parcel parcel, final int flags)
	{
		super.writeToParcel(parcel, flags);

		// write the preference's value
		parcel.writeInt(this.isNull ? 1 : 0);
		parcel.writeInt(this.value);
	}

	// Standard creator object using an instance of this class
	public static final Creator<ColorSavedState> CREATOR = new Creator<ColorSavedState>()
	{
		@NonNull
		@Override
		public ColorSavedState createFromParcel(@NonNull final Parcel in)
		{
			return new ColorSavedState(in);
		}

		@NonNull
		@Override
		public ColorSavedState[] newArray(final int size)
		{
			return new ColorSavedState[size];
		}
	};
}
