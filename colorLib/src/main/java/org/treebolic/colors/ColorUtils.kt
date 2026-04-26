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
     * Get colors from theme
     *
     * @param context     context
     * @param colorAttrIds attr ids
     * @return colors
     */
    fun getColorsFromTheme(context: Context, vararg colorAttrIds: Int): IntArray {
        val theme = context.theme
        val typedArray: TypedArray = theme.obtainStyledAttributes(colorAttrIds)
        val colors = IntArray(typedArray.length())
        try {
            for (i in 0 until typedArray.length()) {
                colors[i] = typedArray.getColor(i, 0)
            }
        } finally {
            typedArray.recycle()
        }
        return colors
    }

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

     /**
     * Get actionbar fore color from theme
     *
     * @param context context
     * @return color
     */
    @JvmStatic
    fun getActionBarForegroundColorFromTheme(context: Context): Int {
        val color = getColorFromStyleInTheme(context, AndroidR.attr.actionBarTheme, AndroidR.attr.textColorPrimary)
        // Log.d(TAG, "getActionBarForegroundColorFromTheme=0x${Integer.toHexString(color)}")
        return color ?: 0x333333
    }

    /**
     * Add transparency
     * @receiver color int
     * @param alpha 0.0 to 1.0
     */
    fun Int.withAlpha(alpha: Float): Int {
        val alphaInt = (alpha.coerceIn(0f, 1f) * 255).toInt()
        return (this and 0x00FFFFFF) or (alphaInt shl 24)
    }

    private const val TAG = "ColorUtils"

    /**
     * Dump background and fore colors from theme
     *
     * @param context context
     */
    @Suppress("unused")
    private fun dumpActionBarColor(context: Context) {
        val theme = context.theme
        theme.dump(Log.DEBUG, TAG, "theme")

        // res id of style pointed to from actionBarStyle
        val typedValue = TypedValue()
        theme.resolveAttribute(AndroidR.attr.actionBarStyle, typedValue, true)
        val resId = typedValue.resourceId
        Log.d(TAG, "actionBarStyle=${Integer.toHexString(resId)}")

        // now get action bar style values
        val attrs = intArrayOf(AndroidR.attr.background, AndroidR.attr.colorForeground)
        theme.obtainStyledAttributes(resId, attrs).use {
            val drawable = it.getDrawable(0)
            Log.d(TAG, "attr=${Integer.toHexString(attrs[0])} value=$drawable")
            for (i in 1 until attrs.size) {
                val intColor = it.getColor(i, -0x33333334) // index, defaultVal
                Log.d(TAG, "$theme  attr=${Integer.toHexString(attrs[i])} value=${Integer.toHexString(intColor)}")
            }
        }
    }
}

