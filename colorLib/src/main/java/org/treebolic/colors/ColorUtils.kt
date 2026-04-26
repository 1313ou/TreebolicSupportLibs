/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.colors

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import org.treebolic.ColorUtils.fetchColors
import org.treebolic.ColorUtils.fetchColorsFromStyle
import org.treebolic.ColorUtils.fetchColorsNullableFromStyle
import org.treebolic.ColorUtils.getColorFromStyleInTheme
import org.treebolic.ColorUtils.getStyleFromTheme
import android.R as AndroidR

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
    @JvmStatic
    fun tint(@ColorInt iconTint: Int, drawable: Drawable) {
        // DrawableCompat.setTint(DrawableCompat.wrap(drawable), iconTint)
        drawable.setTint(iconTint)
    }

    /**
     * Get drawable
     *
     * @param context     context
     * @param drawableRes drawable id
     * @return drawable
     */
    private fun getDrawable(context: Context, @DrawableRes drawableRes: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, drawableRes, context.theme)
    }

    /**
     * Get tinted drawable
     *
     * @param context     context
     * @param drawableRes drawable id
     * @param iconTint    tint
     * @return tinted drawable
     */
    fun getTintedDrawable(context: Context, @DrawableRes drawableRes: Int, @ColorInt iconTint: Int): Drawable {
        val drawable = getDrawable(context, drawableRes)!!
        tint(iconTint, drawable)
        return drawable
    }
}

