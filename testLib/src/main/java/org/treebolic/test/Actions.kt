/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.test;

import android.view.MotionEvent;
import android.view.View;

import org.hamcrest.Matcher;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.MotionEvents;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

@SuppressWarnings("WeakerAccess")
public class Actions
{
	@NonNull
	static ViewAction onlyIf(@NonNull final ViewAction action, @NonNull final Matcher<View> constraints)
	{
		return new ViewAction()
		{
			@NonNull
			@Override
			public Matcher<View> getConstraints()
			{
				return constraints;
			}

			@Override
			public String getDescription()
			{
				return action.getDescription();
			}

			@Override
			public void perform(UiController uiController, View view)
			{
				action.perform(uiController, view);
			}
		};
	}

	@NonNull
	static ViewAction andThen(@NonNull final ViewAction action1, @NonNull final ViewAction action2)
	{
		return new ViewAction()
		{
			@NonNull
			@Override
			public Matcher<View> getConstraints()
			{
				return allOf(action1.getConstraints(), action2.getConstraints());
			}

			@NonNull
			@Override
			public String getDescription()
			{
				return action1.getDescription() + " then " + action2.getDescription();
			}

			@Override
			public void perform(UiController uiController, View view)
			{
				action1.perform(uiController, view);
				action2.perform(uiController, view);
			}
		};
	}

	@NonNull
	static ViewAction andThen(@NonNull final ViewAction action1, @NonNull final ViewAction action2, @SuppressWarnings("SameParameterValue") int lapse)
	{
		return new ViewAction()
		{
			@NonNull
			@Override
			public Matcher<View> getConstraints()
			{
				return allOf(action1.getConstraints(), action2.getConstraints());
			}

			@NonNull
			@Override
			public String getDescription()
			{
				return action1.getDescription() + " then " + action2.getDescription();
			}

			@Override
			public void perform(UiController uiController, View view)
			{
				action1.perform(uiController, view);
				Wait.pause(lapse);
				action2.perform(uiController, view);
			}
		};
	}

	@NonNull
	static ViewAction touchDownAndUp(final float x, final float y)
	{
		return new ViewAction()
		{
			@NonNull
			@Override
			public Matcher<View> getConstraints()
			{
				return isDisplayed();
			}

			@NonNull
			@Override
			public String getDescription()
			{
				return "Send touch events.";
			}

			@Override
			public void perform(@NonNull UiController uiController, @NonNull final View view)
			{
				// Get view absolute position
				int[] location = new int[2];
				view.getLocationOnScreen(location);

				// Offset coordinates by view position
				float[] coordinates = new float[]{x + location[0], y + location[1]};
				float[] precision = new float[]{1f, 1f};

				// Send down event, pause, and send up
				MotionEvent down = MotionEvents.sendDown(uiController, coordinates, precision).down;
				uiController.loopMainThreadForAtLeast(200);
				MotionEvents.sendUp(uiController, down, coordinates);
			}
		};
	}

	@NonNull
	public static ViewAction drag(final CoordinatesProvider from, final CoordinatesProvider to)
	{
		return new GeneralSwipeAction(Swipe.SLOW, from, to, Press.FINGER);
	}

	@NonNull
	public static ViewAction dragBack(final CoordinatesProvider from, final CoordinatesProvider to)
	{
		return andThen(new GeneralSwipeAction(Swipe.SLOW, from, to, Press.FINGER), new GeneralSwipeAction(Swipe.SLOW, to, from, Press.FINGER), 1);
	}

	@NonNull
	public static ViewAction dragBack(final CoordinatesProvider from, final CoordinatesProvider to, final CoordinatesProvider to2)
	{
		return andThen(new GeneralSwipeAction(Swipe.SLOW, from, to, Press.FINGER), new GeneralSwipeAction(Swipe.SLOW, from, to2, Press.FINGER), 1);
	}

	@NonNull
	public static ViewAction dragBack2(final CoordinatesProvider from, final CoordinatesProvider to, final CoordinatesProvider to2)
	{
		return andThen( //
				dragBack(from, to, to2), //
				dragBack(from, to2, to) //
		);
	}
}
