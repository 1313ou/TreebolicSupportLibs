/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.test

import android.view.View
import android.widget.CheckBox
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

object Matchers {

    fun withMenuIdOrText(@IdRes id: Int, @StringRes menuText: Int): Matcher<View> {
        val matcher = ViewMatchers.withId(id)
        try {
            Espresso.onView(matcher).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            return matcher
        } catch (noMatchingViewException: Exception) {
            Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
            return ViewMatchers.withText(menuText)
        }
    }

    fun checkboxWithMenuItem(@StringRes titleId: Int): Matcher<View> {
        return CoreMatchers.allOf(CoreMatchers.instanceOf(CheckBox::class.java), ViewMatchers.hasSibling(ViewMatchers.withChild(ViewMatchers.withText(titleId))), ViewMatchers.isCompletelyDisplayed())
    }
}