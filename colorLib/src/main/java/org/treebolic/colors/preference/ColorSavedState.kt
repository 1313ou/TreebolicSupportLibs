/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.colors.preference

import android.os.Parcel
import android.os.Parcelable
import androidx.preference.Preference

internal class ColorSavedState : Preference.BaseSavedState {

    /**
     * The value
     */
    var isNull: Boolean = false
    var value: Int = 0

    /**
     * Constructor from superstate
     *
     * @param superState superstate
     */
    constructor(superState: Parcelable?) : super(superState)

    /**
     * Constructor/read from parcel
     *
     * @param parcel source parcel
     */
    constructor(parcel: Parcel) : super(parcel) {
        isNull = parcel.readInt() != 0
        value = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeInt(if (isNull) 1 else 0)
        parcel.writeInt(value)
    }

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<ColorSavedState> {
            override fun createFromParcel(parcel: Parcel): ColorSavedState {
                return ColorSavedState(parcel)
            }

            override fun newArray(size: Int): Array<ColorSavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
