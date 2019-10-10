/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */

package org.treebolic.test;

import android.view.View;

import org.hamcrest.Matcher;

import androidx.annotation.NonNull;
import androidx.test.espresso.ViewAssertion;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

@SuppressWarnings("WeakerAccess")
public class ToBoolean
{
	static public boolean test(@NonNull final Matcher<View> view, @NonNull final Matcher<View> state)
	{
		try
		{
			onView(view).check(matches(state));
			return true;
		}
		catch (Throwable e)
		{
			return false;
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	static public boolean testAssertion(@NonNull final Matcher<View> view, @NonNull final ViewAssertion assertion)
	{
		try
		{
			onView(view).check(assertion);
			return true;
		}
		catch (Throwable e)
		{
			return false;
		}
	}
}