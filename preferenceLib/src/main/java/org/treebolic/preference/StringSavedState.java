/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.preference;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

/**
 * Saved state
 */
class StringSavedState extends Preference.BaseSavedState
{
	// member that holds the setting's value
	@Nullable
	String value;

	/**
	 * Constructor from superstate
	 *
	 * @param superState superstate
	 */
	@SuppressWarnings("WeakerAccess")
	public StringSavedState(final Parcelable superState)
	{
		super(superState);
	}

	/**
	 * Constructor from parcel
	 *
	 * @param source source parcel
	 */
	@SuppressWarnings("WeakerAccess")
	public StringSavedState(@NonNull final Parcel source)
	{
		super(source);

		// get the current preference's value
		this.value = source.readString();
	}

	@Override
	public void writeToParcel(@NonNull final Parcel dest, final int flags)
	{
		super.writeToParcel(dest, flags);

		// write the preference's value
		dest.writeString(this.value);
	}

	/**
	 * Standard creator object using an instance of this class
	 */
	public static final Creator<StringSavedState> CREATOR = new Creator<StringSavedState>()
	{
		@Override
		public StringSavedState createFromParcel(@NonNull final Parcel in)
		{
			return new StringSavedState(in);
		}

		@Override
		public StringSavedState[] newArray(final int size)
		{
			return new StringSavedState[size];
		}
	};
}
