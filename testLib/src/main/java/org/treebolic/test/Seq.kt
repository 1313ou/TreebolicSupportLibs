/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.test

import android.content.Context
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers

object Seq {

    fun getResourceString(@StringRes id: Int): String {
        val targetContext = ApplicationProvider.getApplicationContext<Context>()
        return targetContext.resources.getString(id)
    }

    /**
     * Press back control
     */
    fun doPressBack() {
        Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
    }

    /**
     * Type in EditTextView
     *
     * @param editTextViewId EditTextView id
     * @param text           text
     */
    fun doType(@IdRes editTextViewId: Int, text: String) {
        Espresso.onView(ViewMatchers.withId(editTextViewId)) //
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())) //
            .perform(
                ViewActions.typeText(text) //
            )
    }

    /**
     * Type in SearchView
     *
     * @param searchViewId SearchView id
     * @param text         text
     */
    fun doTypeSearch(@IdRes searchViewId: Int, text: String) {
        val searchView = ViewMatchers.withId(searchViewId)

        // open search view
        Espresso.onView(searchView) //
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())) //
            .perform(
                ViewActions.click() //
            )

        // type search
        Espresso.onView(CoreMatchers.allOf(ViewMatchers.isDescendantOfA(searchView), ViewMatchers.isAssignableFrom(EditText::class.java))) //
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())) //
            .perform( //
                ViewActions.typeText(text),  //
                ViewActions.pressImeActionButton() //
            )
    }

    /**
     * Click button
     *
     * @param buttonId Button id
     */
    fun doClick(@IdRes buttonId: Int) {
        Espresso.onView(ViewMatchers.withId(buttonId)) //
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())) //
            .perform(ViewActions.click())
        //
    }

    /**
     * Click menu item (by text)
     *
     * @param menuId   Menu id
     * @param menuText Text in menu item to click
     */
    fun doMenu(@IdRes menuId: Int, @StringRes menuText: Int) {
        Espresso.onView(Matchers.withMenuIdOrText(menuId, menuText)).perform(ViewActions.click())
    }

    /**
     * Click overflow menu item (by text)
     *
     * @param menuText Text in menu item to click
     */
   fun doOptionsMenu(@StringRes menuText: Int) {
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        Espresso.onView(ViewMatchers.withText(menuText)).perform(ViewActions.click())
    }

    /**
     * Click spinner (by text)
     *
     * @param spinnerId  Spinner id
     * @param targetText Text in spinner item to click
     */
    fun doChoose(@IdRes spinnerId: Int, targetText: String) {
        // expand spinner
        Espresso.onView(CoreMatchers.allOf(ViewMatchers.withId(spinnerId), CoreMatchers.instanceOf(Spinner::class.java))) //
            .perform(ViewActions.click())

        // do_click view matching text
        Espresso.onData(CoreMatchers.allOf(CoreMatchers.`is`(CoreMatchers.instanceOf<Any>(String::class.java)), CoreMatchers.`is`(targetText))) //
            .perform(ViewActions.click())

        // check
        Espresso.onView(ViewMatchers.withId(spinnerId)) //
            .check(ViewAssertions.matches(ViewMatchers.withSpinnerText(CoreMatchers.containsString(targetText))))
    }

    /**
     * Click spinner (by text)
     *
     * @param spinnerId Spinner id
     * @param position  Text in spinner item to click
     */
    fun doChoose(@IdRes spinnerId: Int, position: Int) {
        // expand spinner
        Espresso.onView(CoreMatchers.allOf(ViewMatchers.withId(spinnerId), CoreMatchers.instanceOf(Spinner::class.java))) //
            .perform(ViewActions.click())

        // do_click view matching position
        Espresso.onData(org.hamcrest.Matchers.anything()).atPosition(position) //
            .perform(ViewActions.click())
    }

    /**
     * Swipe view
     *
     * @param viewId View id
     */
    fun doSwipeUp(@IdRes viewId: Int) {
        Espresso.onView(ViewMatchers.withId(viewId)) //
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed())) //
            .perform( //
                Actions.onlyIf(ViewActions.swipeUp(), ViewMatchers.isDisplayingAtLeast(1)) //
                //, swipeUp() //
            )
    }
}