package org.treebolic

import android.content.Context
import org.treebolic.ColorUtils.getColorFromStyleInTheme
import android.R as AndroidR

object ActionBarColorUtils {

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

}