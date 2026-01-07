/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import org.treebolic.AppCompatCommonUtils.getThemePref
import org.treebolic.AppCompatCommonUtils.isCurrentThemeDark
import org.treebolic.AppCompatCommonUtils.isThemeDark
import org.treebolic.AppCompatCommonUtils.updateStatusBarForTheme

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

        // status bar
        val isDark = if (themeId != null) isThemeDark(this, themeId) else isCurrentThemeDark(this)
        updateStatusBarForTheme(this, isDark)
    }

    /**
     * Pre-theming hook: give app a chance to do something before theme is set by overriding this.
     */
    protected open fun preThemeHook() {
    }
}
