/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources.NotFoundException
import android.util.Log
import android.util.TypedValue
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import org.treebolic.common.R

object AppCompatCommonUtils {

    private const val TAG = "AppCompatCommonUtils"

    const val PREF_THEME: String = "pref_theme"

    /**
     * Set theme preference
     *
     * @param context  context
     * @param themeIdx theme idx
     */
    fun setThemePref(context: Context, themeIdx: Int) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.putInt(PREF_THEME, themeIdx)
        tryCommit(editor)
    }

    /**
     * Get theme preference
     *
     * @param context context
     * @return value (null if node)
     */
    @JvmStatic
    fun getThemePref(context: Context): Int? {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!sharedPrefs.contains(PREF_THEME)) {
            return null
        }
        try {
            val strValue = sharedPrefs.getString(PREF_THEME, null)
            if (strValue != null) {
                val intValue = strValue.toInt()
                if (intValue != 0) {
                    val resources = context.resources
                    val type = resources.getResourceTypeName(intValue)
                    if ("style" == type) {
                        Log.d(TAG, "Theme " + type + ' ' + resources.getResourceName(intValue))
                        return intValue
                    }
                }
            }
        } catch (_: NumberFormatException) {
        } catch (_: NotFoundException) {
        } catch (_: ClassCastException) {
        }
        sharedPrefs.edit {
            remove(PREF_THEME)
        }
        return null
    }

    /**
     * Try to commit
     *
     * @param editor editor editor
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    private fun tryCommit(editor: SharedPreferences.Editor) {
        try {
            editor.apply()
        } catch (_: AbstractMethodError) {
            editor.commit()
        }
    }

    fun isNightMode(context: Context) = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    fun isCurrentThemeDark(context: Context): Boolean {
        val outValue = TypedValue()
        context.theme.resolveAttribute(R.attr.isDark, outValue, true)
        return outValue.data != 0
    }

    fun isThemeDark(context: Context, themeId: Int): Boolean {
        val attrs = intArrayOf(R.attr.isDark)
        return context.obtainStyledAttributes(themeId, attrs).use { typedArray ->
            typedArray.getBoolean(0, true)
        }
    }

    fun updateBarsForTheme(activity: Activity, isDarkTheme: Boolean) {
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = !isDarkTheme
        controller.isAppearanceLightNavigationBars = !isDarkTheme
    }
}
