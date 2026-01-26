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
     * Fetch color from theme
     *
     * @param context context
     * @param attr    color attr
     * @return color
     */
    fun fetchColor(context: Context, attr: Int): Int {
        val theme = context.theme
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    /**
     * Get color from style
     *
     * @param context  context
     * @param styleRes style id (R.style.MyTheme)
     * @param attr     attr id (R.attr.editTextColor)
     * @return color
     */
    fun fetchColorFromStyle(context: Context, @StyleRes styleRes: Int, attr: Int): Int {
        context.theme.obtainStyledAttributes(styleRes, intArrayOf(attr)).use {
            val intColor = it.getColor(0,  /* index */0 /* defaultVal */)
            // Log.d(TAG, "style resId=${Integer.toHexString(styleRes)} color=${Integer.toHexString(intColor)}")
            return intColor
        }
    }

    /**
     * Get color from style in theme
     *
     * @param context     context
     * @param style       style id (ex: R.style.MyTheme)
     * @param colorAttrId attr id (ex: R.attr.editTextColor)
     * @return color
     */
    private fun getColorFromStyleInTheme(context: Context, @AttrRes style: Int, @Suppress("SameParameterValue") @AttrRes colorAttrId: Int): Int {
        val theme = context.theme

        // res id of style pointed to from actionBarStyle
        val typedValue = TypedValue()
        theme.resolveAttribute(style, typedValue, true)
        val resId = typedValue.resourceId

        // now get action bar style values
        val attrs: IntArray = intArrayOf(colorAttrId)

        // get color
        theme.obtainStyledAttributes(resId, attrs).use {
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
        val color = getColorFromStyleInTheme(context, AndroidR.attr.actionBarTheme, AndroidR.attr.textColorPrimary)
        // Log.d(TAG, "getActionBarForegroundColorFromTheme=0x${Integer.toHexString(color)}")
        return color
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

