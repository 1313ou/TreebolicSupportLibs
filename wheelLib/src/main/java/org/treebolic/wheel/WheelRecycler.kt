/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.wheel

import android.view.View
import android.widget.LinearLayout

/**
 * Recycle stored spinnerwheel items to reuse.
 *
 * @param wheel the spinnerwheel view
 */
class WheelRecycler(
    /** Wheel view */
    private val wheel: AbstractWheel
) {

    /** Cached items */
    private var items: MutableList<View>? = null

    /** Cached empty items */
    private var emptyItems: MutableList<View>? = null

    /**
     * Recycles items from specified layout. There are saved only items not included to specified range. All the cached items are removed from original layout.
     *
     * @param layout     the layout containing items to be cached
     * @param firstItem0 the number of first item in layout
     * @param range      the range of current spinnerwheel items
     * @return the new value of first item number
     */
    fun recycleItems(layout: LinearLayout, firstItem0: Int, range: ItemsRange): Int {
        var firstItem = firstItem0
        var index = firstItem
        var i = 0
        while (i < layout.childCount) {
            if (!range.contains(index)) {
                recycleView(layout.getChildAt(i), index)
                layout.removeViewAt(i)
                if (i == 0) { // first item
                    firstItem++
                }
            } else {
                i++ // go to next item
            }
            index++
        }
        return firstItem
    }

    /**
     * Item view (the cached view)
     */
    val item: View?
        get() = getCachedView(items)

    /**
     * Empty item view (the cached empty view)
     */
    val emptyItem: View?
        get() = getCachedView(emptyItems)

    /**
     * Clears all views
     */
    fun clearAll() {
        if (items != null) {
            items!!.clear()
        }
        if (emptyItems != null) {
            emptyItems!!.clear()
        }
    }

    /**
     * Adds view to cache. Determines view type (item view or empty one) by index.
     *
     * @param view   the view to be cached
     * @param index0 the index of view
     */
    private fun recycleView(view: View, index0: Int) {
        val count = wheel.viewAdapter!!.itemsCount

        var index = index0
        if ((index < 0 || index >= count) && !wheel.isCyclic) {
            // empty view
            emptyItems = addView(view, emptyItems!!)
        } else {
            while (index < 0) {
                index += count
            }
            items = addView(view, items!!)
        }
    }

    companion object {

        /**
         * Adds view to specified cache. Creates a cache list if it is null.
         *
         * @param view   the view to be cached
         * @param cache the cache list
         * @return the cache list
         */
        private fun addView(view: View, cache: MutableList<View>): MutableList<View> {
            cache.add(view)
            return cache
        }

        /**
         * Gets view from specified cache.
         *
         * @param cache the cache
         * @return the first view from cache.
         */
        private fun getCachedView(cache: MutableList<View>?): View? {
            if (!cache.isNullOrEmpty()) {
                val view = cache[0]
                cache.removeAt(0)
                return view
            }
            return null
        }
    }
}
