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
	// Member that holds the setting's value
	int value;

	@SuppressWarnings("WeakerAccess")
	public ColorSavedState(final Parcelable superState)
	{
		super(superState);
	}

	@SuppressWarnings("WeakerAccess")
	public ColorSavedState(@NonNull final Parcel source)
	{
		super(source);
		// Get the current preference's value
		this.value = source.readInt();
	}

	@Override
	public void writeToParcel(@NonNull final Parcel dest, final int flags)
	{
		super.writeToParcel(dest, flags);
		// Write the preference's value
		dest.writeInt(this.value);
	}

	// Standard creator object using an instance of this class
	public static final Creator<ColorSavedState> CREATOR = new Creator<ColorSavedState>()
	{
		@Override
		public ColorSavedState createFromParcel(@NonNull final Parcel in)
		{
			return new ColorSavedState(in);
		}

		@Override
		public ColorSavedState[] newArray(final int size)
		{
			return new ColorSavedState[size];
		}
	};
}
