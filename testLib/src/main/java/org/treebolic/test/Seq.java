/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */

package org.treebolic.test;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import org.hamcrest.Matcher;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.anything;

public class Seq
{
	static public String getResourceString(@StringRes int id)
	{
		final Context targetContext = ApplicationProvider.getApplicationContext();
		return targetContext.getResources().getString(id);
	}

	/**
	 * Press back control
	 */
	static public void do_pressBack()
	{
		onView(isRoot()).perform(pressBack());
	}

	/**
	 * Type in EditTextView
	 *
	 * @param editTextViewId EditTextView id
	 * @param text           text
	 */
	static public void do_type(@SuppressWarnings("SameParameterValue") @IdRes final int editTextViewId, @NonNull final String text)
	{
		onView(withId(editTextViewId)) //
				.check(matches(isDisplayed())) //
				.perform(typeText(text) //
				);
	}

	/**
	 * Type in SearchView
	 *
	 * @param searchViewId SearchView id
	 * @param text         text
	 */
	static public void do_typeSearch(@IdRes final int searchViewId, @NonNull final String text)
	{
		final Matcher<View> searchView = withId(searchViewId);

		// open search view
		onView(searchView) //
				.check(matches(isDisplayed())) //
				.perform(click() //
				);

		// type search
		onView(allOf(isDescendantOfA(searchView), isAssignableFrom(EditText.class))) //
				.check(matches(isDisplayed())) //
				.perform( //
						typeText(text),  //
						pressImeActionButton() //
				);
	}

	/**
	 * Click button
	 *
	 * @param buttonId Button id
	 */
	static public void do_click(@IdRes final int buttonId)
	{
		onView(withId(buttonId)) //
				.check(matches(isDisplayed())) //
				.perform(click())  //
		;
	}

	/**
	 * Click menu item (by text)
	 *
	 * @param menuId   Menu id
	 * @param menuText Text in menu item to click
	 */
	static public void do_menu(final @IdRes int menuId, @StringRes int menuText)
	{
		onView(Matchers.withMenuIdOrText(menuId, menuText)).perform(click());
	}

	/**
	 * Click overflow menu item (by text)
	 *
	 * @param menuText Text in menu item to click
	 */
	static public void do_options_menu(final @StringRes int menuText)
	{
		openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
		onView(withText(menuText)).perform(click());
	}

	/**
	 * Click spinner (by text)
	 *
	 * @param spinnerId  Spinner id
	 * @param targetText Text in spinner item to click
	 */
	static public void do_choose(@IdRes int spinnerId, final String targetText)
	{
		// expand spinner
		onView(allOf(withId(spinnerId), instanceOf(Spinner.class))) //
				.perform(click());

		// do_click view matching text
		onData(allOf(is(instanceOf(String.class)), is(targetText))) //
				.perform(click());

		// check
		onView(withId(spinnerId)) //
				.check(matches(withSpinnerText(containsString(targetText))));
	}

	/**
	 * Click spinner (by text)
	 *
	 * @param spinnerId Spinner id
	 * @param position  Text in spinner item to click
	 */
	@SuppressWarnings("Unused")
	static public void do_choose(@IdRes int spinnerId, final int position)
	{
		// expand spinner
		onView(allOf(withId(spinnerId), instanceOf(Spinner.class))) //
				.perform(click());

		// do_click view matching position
		onData(anything()).atPosition(position) //
				.perform(click());
	}

	/**
	 * Swipe view
	 *
	 * @param viewId View id
	 */
	static public void do_swipeUp(@IdRes final int viewId)
	{
		onView(withId(viewId)) //
				.check(matches(isDisplayed())) //
				.perform( //
						Actions.onlyIf(swipeUp(), isDisplayingAtLeast(1)) //
						//, swipeUp() //
				);
	}
}