/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel.adapters

import android.content.Context

/**
 * Numeric Wheel adapter.
 *
 * @param context the current context
 * @param minValue the spinnerwheel min value
 * @param maxValue the spinnerwheel max value
 * @param format   the format string
 */
class NumericWheelAdapter @JvmOverloads constructor(
    context: Context,
    private var minValue: Int = DEFAULT_MIN_VALUE,
    private var maxValue: Int = DEFAULT_MAX_VALUE,
    private val format: String? = null
) : AbstractWheelTextAdapter(context) {

    fun setMinValue(minValue0: Int) {
        minValue = minValue0
        notifyDataInvalidatedEvent()
    }

    fun setMaxValue(maxValue0: Int) {
        maxValue = maxValue0
        notifyDataInvalidatedEvent()
    }

    public override fun getItemText(index: Int): CharSequence? {
        if (index in 0 until itemsCount) {
            val value = minValue + index
            return if (format != null) String.format(format, value) else value.toString()
        }
        return null
    }

    override val itemsCount: Int
        get() {
            return maxValue - minValue + 1
        }

    companion object {

        /**
         * The default min value
         */
        const val DEFAULT_MAX_VALUE: Int = 9

        /**
         * The default max value
         */
        const val DEFAULT_MIN_VALUE: Int = 0
    }
}
