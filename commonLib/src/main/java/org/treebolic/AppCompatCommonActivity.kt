/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import org.treebolic.AppCompatCommonUtils.getThemePref
import org.treebolic.AppCompatCommonUtils.isCurrentThemeDark
import org.treebolic.AppCompatCommonUtils.isThemeDark
import org.treebolic.AppCompatCommonUtils.updateBarsForTheme

abstract class AppCompatCommonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // give app a chance to do something before theme is set
        preThemeHook()

        // theme
        val themeId = getThemePref(this)
        if (themeId != null) {
            setTheme(themeId)
        }

        // super
        super.onCreate(savedInstanceState)

        // edge to edge
        enableEdgeToEdge()

        // day/night mode
        val isDark = if (themeId != null) isThemeDark(this, themeId) else isCurrentThemeDark(this)
        switchToMode(if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        // status bar
        updateBarsForTheme(this, isDark)

        switchToMode(if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    /**
     * Switch to day/night mode
     * @param mode mode
     */
    private fun switchToMode(mode: Int) {
        Log.d("Switch mode", "set $mode mode for $componentName")
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            window.decorView.post {
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }

    /**
     * Pre-theming hook: give app a chance to do something before theme is set by overriding this.
     */
    protected open fun preThemeHook() {
    }
}
