/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

/**
 * Range for visible items.
 *
 * @property first the index of first item
 * @property count the count of items
 */
class ItemsRange

/**
 * Default constructor. Creates an empty range
 */
@JvmOverloads constructor(
    @JvmField val first: Int = 0,
    @JvmField val count: Int = 0
) {

    /**
     * Last item index
     */
    val last: Int
        get() = first + count - 1

    /**
     * Tests whether item is contained by range
     *
     * @param index the item number
     * @return true if item is contained
     */
    fun contains(index: Int): Boolean {
        return index in first..last
    }
}