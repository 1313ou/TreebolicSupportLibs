/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.test

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions
import org.hamcrest.Matcher

object ToBoolean {

    fun test(view: Matcher<View?>, state: Matcher<View?>): Boolean {
        try {
            Espresso.onView(view).check(ViewAssertions.matches(state))
            return true
        } catch (e: Throwable) {
            return false
        }
    }

    fun testAssertion(view: Matcher<View?>, assertion: ViewAssertion): Boolean {
        try {
            Espresso.onView(view).check(assertion)
            return true
        } catch (e: Throwable) {
            return false
        }
    }
}