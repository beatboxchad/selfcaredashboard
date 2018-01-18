/*
 * Copyright 2017, Chad Cassady
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beatboxchad.android.selfcaredashboard.goals;

import android.support.test.espresso.NoActivityResumedException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;

import com.beatboxchad.android.selfcaredashboard.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.contrib.DrawerMatchers.isOpen;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.beatboxchad.android.selfcaredashboard.TestUtils.getToolbarNavigationContentDescription;
import static com.beatboxchad.android.selfcaredashboard.custom.action.NavigationViewActions.navigateTo;
import static junit.framework.Assert.fail;

/**
 * Tests for the {@link DrawerLayout} layout component in {@link GoalsActivity} which manages
 * navigation within the app.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppNavigationTest {

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<GoalsActivity> mActivityTestRule =
            new ActivityTestRule<>(GoalsActivity.class);

    @Test
    public void clickOnStatisticsNavigationItem_ShowsStatisticsScreen() {
        openStatisticsScreen();

        // Check that statistics Activity was opened.
        onView(withId(R.id.statistics)).check(matches(isDisplayed()));
    }

    @Test
    public void clickOnListNavigationItem_ShowsListScreen() {
        openStatisticsScreen();

        openGoalsScreen();

        // Check that Goals Activity was opened.
        onView(withId(R.id.goalsContainer)).check(matches(isDisplayed()));
    }

    @Test
    public void clickOnAndroidHomeIcon_OpensNavigation() {
        // Check that left drawer is closed at startup
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))); // Left Drawer should be closed.

        // Open Drawer
        onView(withContentDescription(getToolbarNavigationContentDescription(
                mActivityTestRule.getActivity(), R.id.toolbar))).perform(click());

        // Check if drawer is open
        onView(withId(R.id.drawer_layout))
                .check(matches(isOpen(Gravity.LEFT))); // Left drawer is open open.
    }

    @Test
    public void Statistics_backNavigatesToGoals() {
        openStatisticsScreen();

        // Press back to go back to the goals list
        pressBack();

        // Check that Goals Activity was restored.
        onView(withId(R.id.goalsContainer)).check(matches(isDisplayed()));
    }

    @Test
    public void backFromGoalsScreen_ExitsApp() {
        // From the goals screen, press back should exit the app.
        assertPressingBackExitsApp();
    }

    @Test
    public void backFromGoalsScreenAfterStats_ExitsApp() {
        // This test checks that GoalsActivity is a parent of StatisticsActivity

        // Open the stats screen
        openStatisticsScreen();

        // Open the goals screen to restore the goal
        openGoalsScreen();

        // Pressing back should exit app
        assertPressingBackExitsApp();
    }

    private void assertPressingBackExitsApp() {
        try {
            pressBack();
            fail("Should kill the app and throw an exception");
        } catch (NoActivityResumedException e) {
            // Test OK
        }
    }

    private void openGoalsScreen() {
        // Open Drawer to click on navigation item.
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Left Drawer should be closed.
                .perform(open()); // Open Drawer

        // Start goals list screen.
        onView(withId(R.id.nav_view))
                .perform(navigateTo(R.id.list_navigation_menu_item));
    }

    private void openStatisticsScreen() {
        // Open Drawer to click on navigation item.
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Left Drawer should be closed.
                .perform(open()); // Open Drawer

        // Start statistics screen.
        onView(withId(R.id.nav_view))
                .perform(navigateTo(R.id.statistics_navigation_menu_item));
    }
}
