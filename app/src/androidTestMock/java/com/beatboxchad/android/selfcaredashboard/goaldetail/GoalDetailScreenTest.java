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

package com.beatboxchad.android.selfcaredashboard.goaldetail;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.IsNot.not;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.TestUtils;
import com.beatboxchad.android.selfcaredashboard.data.FakeGoalsRemoteDataSource;
import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;
import com.beatboxchad.android.selfcaredashboard.util.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the goals screen, the main screen which contains a list of all goals.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class GoalDetailScreenTest {

    private static String GOAL_TITLE = "ATSL";

    private static String GOAL_DESCRIPTION = "Rocks";

    /**
     * {@link Goal} stub that is added to the fake service API layer.
     */
    private static Goal ACTIVE_GOAL = new Goal(GOAL_TITLE, GOAL_DESCRIPTION, false);

    /**
     * {@link Goal} stub that is added to the fake service API layer.
     */
    private static Goal COMPLETED_GOAL = new Goal(GOAL_TITLE, GOAL_DESCRIPTION, true);

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     *
     * <p>
     * Sometimes an {@link Activity} requires a custom start {@link Intent} to receive data
     * from the source Activity. ActivityTestRule has a feature which let's you lazily start the
     * Activity under test, so you can control the Intent that is used to start the target
     * Activity.
     */
    @Rule
    public ActivityTestRule<GoalDetailActivity> mGoalDetailActivityTestRule =
            new ActivityTestRule<>(GoalDetailActivity.class, true /* Initial touch mode  */,
                    false /* Lazily launch activity */);


    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void registerIdlingResource() {
        Espresso.registerIdlingResources(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(EspressoIdlingResource.getIdlingResource());
    }

    private void loadActiveGoal() {
        startActivityWithWithStubbedGoal(ACTIVE_GOAL);
    }

    private void loadCompletedGoal() {
        startActivityWithWithStubbedGoal(COMPLETED_GOAL);
    }

    /**
     * Setup your test fixture with a fake goal id. The {@link GoalDetailActivity} is started with
     * a particular goal id, which is then loaded from the service API.
     *
     * <p>
     * Note that this test runs hermetically and is fully isolated using a fake implementation of
     * the service API. This is a great way to make your tests more reliable and faster at the same
     * time, since they are isolated from any outside dependencies.
     */
    private void startActivityWithWithStubbedGoal(Goal goal) {
        // Add a goal stub to the fake service api layer.
        GoalsRepository.destroyInstance();
        FakeGoalsRemoteDataSource.getInstance().addGoals(goal);

        // Lazily start the Activity from the ActivityTestRule this time to inject the start Intent
        Intent startIntent = new Intent();
        startIntent.putExtra(GoalDetailActivity.EXTRA_GOAL_ID, goal.getId());
        mGoalDetailActivityTestRule.launchActivity(startIntent);
    }

    @Test
    public void activeGoalDetails_DisplayedInUi() throws Exception {
        loadActiveGoal();

        // Check that the goal title and description are displayed
        onView(withId(R.id.goal_detail_title)).check(matches(withText(GOAL_TITLE)));
        onView(withId(R.id.goal_detail_description)).check(matches(withText(GOAL_DESCRIPTION)));
        onView(withId(R.id.goal_detail_complete)).check(matches(not(isChecked())));
    }

    @Test
    public void completedGoalDetails_DisplayedInUi() throws Exception {
        loadCompletedGoal();

        // Check that the goal title and description are displayed
        onView(withId(R.id.goal_detail_title)).check(matches(withText(GOAL_TITLE)));
        onView(withId(R.id.goal_detail_description)).check(matches(withText(GOAL_DESCRIPTION)));
        onView(withId(R.id.goal_detail_complete)).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_menuAndGoalPersist() {
        loadActiveGoal();

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));

        TestUtils.rotateOrientation(mGoalDetailActivityTestRule.getActivity());

        // Check that the goal is shown
        onView(withId(R.id.goal_detail_title)).check(matches(withText(GOAL_TITLE)));
        onView(withId(R.id.goal_detail_description)).check(matches(withText(GOAL_DESCRIPTION)));

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));
    }

}
