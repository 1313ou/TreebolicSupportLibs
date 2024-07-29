/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel.adapters

import android.content.Context

/**
 * The simple Array spinnerwheel adapter
 *
 * @param context the current context
 * @property items the items
 * @param T the element type
 */
class ArrayWheelAdapter<T>(
    context: Context,
    private val items: Array<T>
) : AbstractWheelTextAdapter(context) {

    public override fun getItemText(index: Int): CharSequence? {
        if (index >= 0 && index < items.size) {
            val item = items[index]
            if (item is CharSequence) {
                return item
            }
            return item.toString()
        }
        return null
    }

    override val itemsCount: Int
        get() {
            return items.size
        }
}
