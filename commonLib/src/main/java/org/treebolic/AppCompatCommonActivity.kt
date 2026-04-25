/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic

import android.os.Bundle
import android.util.Log
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import org.treebolic.AppCompatCommonUtils.getThemePref

abstract class AppCompatCommonActivity : BaseActivity() {

    @StyleRes
    protected var themeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // give app a chance to do something before theme is set
        preThemeHook()

        // theme
        themeId = getThemePref(this)
        if (themeId != null) {
            setTheme(themeId!!)
        }

        // super
        super.onCreate(savedInstanceState)
    }

    /**
     * Pre-theming hook: give app a chance to do something before theme is set by overriding this.
     */
    protected open fun preThemeHook() {
    }
}
