/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.test

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.MotionEvents
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers

object Actions {

    fun onlyIf(action: ViewAction, constraints: Matcher<View>): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return constraints
            }

            override fun getDescription(): String {
                return action.description
            }

            override fun perform(uiController: UiController, view: View) {
                action.perform(uiController, view)
            }
        }
    }

    fun andThen(action1: ViewAction, action2: ViewAction): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return Matchers.allOf(action1.constraints, action2.constraints)
            }

            override fun getDescription(): String {
                return action1.description + " then " + action2.description
            }

            override fun perform(uiController: UiController, view: View) {
                action1.perform(uiController, view)
                action2.perform(uiController, view)
            }
        }
    }

    fun andThen(action1: ViewAction, action2: ViewAction, lapse: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return Matchers.allOf(action1.constraints, action2.constraints)
            }

            override fun getDescription(): String {
                return action1.description + " then " + action2.description
            }

            override fun perform(uiController: UiController, view: View) {
                action1.perform(uiController, view)
                Wait.pause(lapse)
                action2.perform(uiController, view)
            }
        }
    }

    fun touchDownAndUp(x: Float, y: Float): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isDisplayed()
            }

            override fun getDescription(): String {
                return "Send touch events."
            }

            override fun perform(uiController: UiController, view: View) {
                // Get view absolute position
                val location = IntArray(2)
                view.getLocationOnScreen(location)

                // Offset coordinates by view position
                val coordinates = floatArrayOf(x + location[0], y + location[1])
                val precision = floatArrayOf(1f, 1f)

                // Send down event, pause, and send up
                val down = MotionEvents.sendDown(uiController, coordinates, precision).down
                uiController.loopMainThreadForAtLeast(200)
                MotionEvents.sendUp(uiController, down, coordinates)
            }
        }
    }

    fun drag(from: CoordinatesProvider?, to: CoordinatesProvider?): ViewAction {
        return GeneralSwipeAction(Swipe.SLOW, from, to, Press.FINGER)
    }

    fun dragBack(from: CoordinatesProvider?, to: CoordinatesProvider?): ViewAction {
        return andThen(GeneralSwipeAction(Swipe.SLOW, from, to, Press.FINGER), GeneralSwipeAction(Swipe.SLOW, to, from, Press.FINGER), 1)
    }

    fun dragBack(from: CoordinatesProvider?, to: CoordinatesProvider?, to2: CoordinatesProvider?): ViewAction {
        return andThen(GeneralSwipeAction(Swipe.SLOW, from, to, Press.FINGER), GeneralSwipeAction(Swipe.SLOW, from, to2, Press.FINGER), 1)
    }

    fun dragBack2(from: CoordinatesProvider?, to: CoordinatesProvider?, to2: CoordinatesProvider?): ViewAction {
        return andThen( //
            dragBack(from, to, to2),  //
            dragBack(from, to2, to) //
        )
    }
}
