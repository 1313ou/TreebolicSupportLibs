package org.treebolic

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.use

object ColorUtils {

    /**
     * Fetch colors resources
     *
     * @param context context
     * @param attr    attribute
     * @return color int
     */
    @JvmStatic
    @ColorInt
    fun fetchColor(context: Context, @AttrRes attr: Int): Int {
        return fetchColorNullable(context, attr) ?: Color.TRANSPARENT
    }

    /**
     * Fetch colors resources
     *
     * @param context context
     * @param attr    attribute
     * @return color int
     */
    @JvmStatic
    @ColorInt
    fun fetchColorNullable(context: Context, @AttrRes attr: Int): Int? {
        val typedValue = TypedValue()
        val wasResolved = context.theme.resolveAttribute(attr, typedValue, true)
        return if (wasResolved) {
            if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT
            ) {
                // It's a raw hex value like #FF0000 defined directly in the theme
                typedValue.data
            } else {
                // It's a reference to a color resource (@color/...)
                // Must use ContextCompat or Resources to get the actual hex
                getColor(context, typedValue.resourceId)
            }
        } else null
    }

    /**
     * Fetch colors resources
     *
     * @param context context
     * @param attrs   attributes
     * Expects an array of Attribute IDs (@AttrRes attrs)
     * Even though passing a dynamic array of @AttrRes (the IDs), the compiler treats that array as a "temporary styleable."
     * @return array of int resources, with 0 value if not found
     */
    @JvmStatic
    @ColorInt
    fun fetchColors(
        context: Context,
        vararg attrs: Int
    ): IntArray {
        context.obtainStyledAttributes(attrs).use { typedArray ->
            return IntArray(attrs.size) { i ->
                typedArray.getColor(i, 0)
            }
        }
    }

    /**
     * Fetch colors resources
     *
     * @param context context
     * @param attrs   attributes
     * Expects an array of Attribute IDs (@AttrRes attrs)
     * Even though passing a dynamic array of @AttrRes (the IDs), the compiler treats that array as a "temporary styleable."
     * @return array of Integer resources, with null value if not found
     */
    @JvmStatic
    @ColorInt
    fun fetchColorsNullable(
        context: Context,
        vararg attrs: Int
    ): Array<Int?> {
        val typedValue = TypedValue()
        context.obtainStyledAttributes(typedValue.data, attrs).use {
            return Array(attrs.size) { i ->
                if (it.hasValue(i)) it.getColor(i, 0) else null
            }
        }
    }

    /**
     * Get color
     *
     * @param context context
     * @param resId   resource id
     * @return color int
     */
    @JvmStatic
    @ColorInt
    fun getColor(context: Context, @ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }
}