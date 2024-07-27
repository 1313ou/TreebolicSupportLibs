/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.preference

import android.os.Parcel
import android.os.Parcelable
import androidx.preference.Preference

/**
 * Saved state
 */
internal class StringSavedState : Preference.BaseSavedState {

    /**
     * The value
     */
    @JvmField
    var value: String? = null

    /**
     * Constructor from superstate
     *
     * @param superState superstate
     */
    constructor(superState: Parcelable?) : super(superState)

    /**
     * Constructor from parcel
     *
     * @param parcel source parcel
     */
    constructor(parcel: Parcel) : super(parcel) {
        this.value = parcel.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(this.value)
    }

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<StringSavedState> {
            override fun createFromParcel(parcel: Parcel): StringSavedState {
                return StringSavedState(parcel)
            }

            override fun newArray(size: Int): Array<StringSavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
