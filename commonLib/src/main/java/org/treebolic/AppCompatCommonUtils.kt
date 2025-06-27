/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources.NotFoundException
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.core.content.edit

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
}
