/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.search

import android.graphics.drawable.Drawable
import android.view.Menu
import androidx.annotation.ColorInt

/**
 * Color utilities
 *
 * @author Bernard Bou
 */
object ColorUtils {

    /**
     * Tint menu items
     *
     * @param iconTint    tint
     * @param menu        menu
     * @param menuItemIds menu item ids
     */
    @JvmStatic
    fun tint(@ColorInt iconTint: Int, menu: Menu, vararg menuItemIds: Int) {
        for (menuItemId in menuItemIds) {
            val menuItem = menu.findItem(menuItemId)
            val drawable = menuItem.icon
            if (drawable != null) {
                tint(iconTint, drawable)
            }
        }
    }

    /**
     * Tint drawable
     *
     * @param iconTint tint
     * @param drawable drawable
     */
    fun tint(@ColorInt iconTint: Int, drawable: Drawable) {
        // DrawableCompat.setTint(DrawableCompat.wrap(drawable), iconTint)
        drawable.setTint(iconTint)
    }
}

