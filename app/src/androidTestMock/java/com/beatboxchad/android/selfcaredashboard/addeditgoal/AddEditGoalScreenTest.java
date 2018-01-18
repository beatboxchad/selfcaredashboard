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

package com.beatboxchad.android.selfcaredashboard.addeditgoal;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.TestUtils;
import com.beatboxchad.android.selfcaredashboard.data.FakeGoalsRemoteDataSource;
import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;
import com.beatboxchad.android.selfcaredashboard.util.EspressoIdlingResource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.beatboxchad.android.selfcaredashboard.R.id.toolbar;

/**
 * Tests for the add goal screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEditGoalScreenTest {

    private static final String GOAL_ID = "1";

    /**
     * {@link IntentsTestRule} is an {@link ActivityTestRule} which inits and releases Espresso
     * Intents before and after each test run.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<AddEditGoalActivity> mActivityTestRule =
            new ActivityTestRule<>(AddEditGoalActivity.class, false, false);

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

    @Test
    public void emptyGoal_isNotSaved() {
        // Launch activity to add a new goal
        launchNewGoalActivity(null);

        // Add invalid title and description combination
        onView(withId(R.id.add_goal_title)).perform(clearText());
        onView(withId(R.id.add_goal_description)).perform(clearText());
        // Try to save the goal
        onView(withId(R.id.fab_edit_goal_done)).perform(click());

        // Verify that the activity is still displayed (a correct goal would close it).
        onView(withId(R.id.add_goal_title)).check(matches(isDisplayed()));
    }

    @Test
    public void toolbarTitle_newGoal_persistsRotation() {
        // Launch activity to add a new goal
        launchNewGoalActivity(null);

        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_goal)));

        // Rotate activity
        TestUtils.rotateOrientation(mActivityTestRule.getActivity());

        // Check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_goal)));
    }

    @Test
    public void toolbarTitle_editGoal_persistsRotation() {
        // Put a goal in the repository and start the activity to edit it
        GoalsRepository.destroyInstance();
        FakeGoalsRemoteDataSource.getInstance().addGoals(new Goal("Title1", "", GOAL_ID, false));
        launchNewGoalActivity(GOAL_ID);

        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_goal)));

        // Rotate activity
        TestUtils.rotateOrientation(mActivityTestRule.getActivity());

        // check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_goal)));
    }

    /**
     * @param goalId is null if used to add a new goal, otherwise it edits the goal.
     */
    private void launchNewGoalActivity(@Nullable String goalId) {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation()
                .getTargetContext(), AddEditGoalActivity.class);

        intent.putExtra(AddEditGoalFragment.ARGUMENT_EDIT_GOAL_ID, goalId);
        mActivityTestRule.launchActivity(intent);
    }

    /**
     * Matches the toolbar title with a specific string resource.
     *
     * @param resourceId the ID of the string resource to match
     */
    public static Matcher<View> withToolbarTitle(final int resourceId) {
        return new BoundedMatcher<View, Toolbar>(Toolbar.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("with toolbar title from resource id: ");
                description.appendValue(resourceId);
            }

            @Override
            protected boolean matchesSafely(Toolbar toolbar) {
                CharSequence expectedText = "";
                try {
                    expectedText = toolbar.getResources().getString(resourceId);
                } catch (Resources.NotFoundException ignored) {
                    /* view could be from a context unaware of the resource id. */
                }
                CharSequence actualText = toolbar.getTitle();
                return expectedText.equals(actualText);
            }
        };
    }
}
