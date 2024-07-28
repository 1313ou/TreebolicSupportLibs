/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.search

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Menu
import androidx.annotation.AttrRes
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

    /**
     * Get color from theme
     *
     * @param context     context
     * @param style       style id (ex: R.style.MyTheme)
     * @param colorAttrId attr id (ex: R.attr.editTextColor)
     * @return color
     */
    private fun getColorFromTheme(context: Context, @AttrRes style: Int, @Suppress("SameParameterValue") @AttrRes colorAttrId: Int): Int {
        val theme = context.theme

        // res id of style pointed to from actionBarStyle
        val typedValue = TypedValue()
        theme.resolveAttribute(style, typedValue, true)
        val resId = typedValue.resourceId

        // Log.d(TAG, "actionBarStyle=${Integer.toHexString(resId)}")

        // now get action bar style values
        val attrs = intArrayOf(colorAttrId)

        // get color
        theme.obtainStyledAttributes(resId, attrs)
            .use {
                val color = it.getColor(0,  /* index */-0x33333334 /* defaultVal */)
                // Log.d(TAG, "$theme attr=${Integer.toHexString(attrs[0])} value=${Integer.toHexString(color)}")
                return color
        }
    }

    /**
     * Get actionbar fore color from theme
     *
     * @param context context
     * @return color
     */
    @JvmStatic
    fun getActionBarForegroundColorFromTheme(context: Context): Int {
        val color = getColorFromTheme(context, R.attr.actionBarTheme, android.R.attr.textColorPrimary)
        // Log.d(TAG, "getActionBarForegroundColorFromTheme=0x${Integer.toHexString(color)}")
        return color
    }
}

