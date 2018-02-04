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

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.filters.LargeTest;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.beatboxchad.android.selfcaredashboard.Injection;
import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.TestUtils;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.beatboxchad.android.selfcaredashboard.TestUtils.getCurrentActivity;
import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the goals screen, the main screen which contains a list of all goals.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class GoalsScreenTest {

    private final static String TITLE1 = "TITLE1";

    private final static String DESCRIPTION = "DESCR";

    private final static String TITLE2 = "TITLE2";

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<GoalsActivity> mGoalsActivityTestRule =
            new ActivityTestRule<GoalsActivity>(GoalsActivity.class) {

                /**
                 * To avoid a long list of goals and the need to scroll through the list to find a
                 * goal, we call {@link GoalsDataSource#deleteAllGoals()} before each test.
                 */
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                    // Doing this in @Before generates a race condition.
                    Injection.provideGoalsRepository(InstrumentationRegistry.getTargetContext())
                        .deleteAllGoals();
                }
            };

    /**
     * A custom {@link Matcher} which matches an item in a {@link ListView} by its text.
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link ListView}
     * <ul>
     *
     * @param itemText the text to match
     * @return Matcher that matches text in the given view
     */
    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(
                        isDescendantOfA(isAssignableFrom(ListView.class)),
                        withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA LV with text " + itemText);
            }
        };
    }

    @Test
    public void clickAddGoalButton_opensAddGoalUi() {
        // Click on the add goal button
        onView(withId(R.id.fab_add_goal)).perform(click());

        // Check if the add goal screen is displayed
        onView(withId(R.id.add_goal_title)).check(matches(isDisplayed()));
    }

    @Test
    public void editGoal() throws Exception {
        // First add a goal
        createGoal(TITLE1, DESCRIPTION);

        // Click on the goal on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the edit goal button
        onView(withId(R.id.fab_edit_goal)).perform(click());

        String editGoalTitle = TITLE2;
        String editGoalDescription = "New Description";

        // Edit goal mTitle and description
        onView(withId(R.id.add_goal_title))
                .perform(replaceText(editGoalTitle), closeSoftKeyboard()); // Type new goal mTitle
        // Save the goal
        onView(withId(R.id.fab_edit_goal_done)).perform(click());

        // Verify goal is displayed on screen in the goal list.
        onView(withItemText(editGoalTitle)).check(matches(isDisplayed()));

        // Verify previous goal is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void addGoalToGoalsList() throws Exception {
        createGoal(TITLE1, DESCRIPTION);

        // Verify goal is displayed on screen
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
    }

    @Test
    public void markGoalAsArchive() {
        viewAllGoals();

        // Add active goal
        createGoal(TITLE1, DESCRIPTION);

        // Mark the goal as archive
        clickCheckBoxForGoal(TITLE1);

        // Verify goal is shown as archive
        viewAllGoals();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewActiveGoals();
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
        viewArchivedGoals();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
    }

    @Test
    public void markGoalAsActive() {
        viewAllGoals();

        // Add archived goal
        createGoal(TITLE1, DESCRIPTION);
        clickCheckBoxForGoal(TITLE1);

        // Mark the goal as active
        clickCheckBoxForGoal(TITLE1);

        // Verify goal is shown as active
        viewAllGoals();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewActiveGoals();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        viewArchivedGoals();
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showAllGoals() {
        // Add 2 active goals
        createGoal(TITLE1, DESCRIPTION);
        createGoal(TITLE2, DESCRIPTION);

        //Verify that all our goals are shown
        viewAllGoals();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void showActiveGoals() {
        // Add 2 active goals
        createGoal(TITLE1, DESCRIPTION);
        createGoal(TITLE2, DESCRIPTION);

        //Verify that all our goals are shown
        viewActiveGoals();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void showArchivedGoals() {
        // Add 2 archived goals
        createGoal(TITLE1, DESCRIPTION);
        clickCheckBoxForGoal(TITLE1);
        createGoal(TITLE2, DESCRIPTION);
        clickCheckBoxForGoal(TITLE2);

        // Verify that all our goals are shown
        viewArchivedGoals();
        onView(withItemText(TITLE1)).check(matches(isDisplayed()));
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));
    }

    @Test
    public void clearArchivedGoals() {
        viewAllGoals();

        // Add 2 archive goals
        createGoal(TITLE1, DESCRIPTION);
        clickCheckBoxForGoal(TITLE1);
        createGoal(TITLE2, DESCRIPTION);
        clickCheckBoxForGoal(TITLE2);

        // Click clear archived in menu
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.menu_clear)).perform(click());

        //Verify that archived goals are not shown
        onView(withItemText(TITLE1)).check(matches(not(isDisplayed())));
        onView(withItemText(TITLE2)).check(matches(not(isDisplayed())));
    }

    @Test
    public void createOneGoal_deleteGoal() {
        viewAllGoals();

        // Add active goal
        createGoal(TITLE1, DESCRIPTION);

        // Open it in details view
        onView(withText(TITLE1)).perform(click());

        // Click delete goal in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify it was deleted
        viewAllGoals();
        onView(withText(TITLE1)).check(matches(not(isDisplayed())));
    }

    @Test
    public void createTwoGoals_deleteOneGoal() {
        // Add 2 active goals
        createGoal(TITLE1, DESCRIPTION);
        createGoal(TITLE2, DESCRIPTION);

        // Open the second goal in details view
        onView(withText(TITLE2)).perform(click());

        // Click delete goal in menu
        onView(withId(R.id.menu_delete)).perform(click());

        // Verify only one goal was deleted
        viewAllGoals();
        onView(withText(TITLE1)).check(matches(isDisplayed()));
        onView(withText(TITLE2)).check(doesNotExist());
    }

    @Test
    public void markGoalAsArchiveOnDetailScreen_goalIsArchiveInList() {
        viewAllGoals();

        // Add 1 active goal
        createGoal(TITLE1, DESCRIPTION);

        // Click on the goal on the list
        onView(withText(TITLE1)).perform(click());

        onView(withId(R.id.fab_add_goal)).perform(click());

        // Click on the checkbox in goal details screen
        onView(withId(R.id.goal_detail_archived)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the goal is marked as archived
        onView(allOf(withId(R.id.archived),
                hasSibling(withText(TITLE1)))).check(matches(isChecked()));
    }

    @Test
    public void markGoalAsActiveOnDetailScreen_goalIsActiveInList() {
        viewAllGoals();

        // Add 1 archived goal
        createGoal(TITLE1, DESCRIPTION);
        clickCheckBoxForGoal(TITLE1);

        // Click on the goal on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in goal details screen
        onView(withId(R.id.goal_detail_archived)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the goal is marked as active
        onView(allOf(withId(R.id.archived),
                hasSibling(withText(TITLE1)))).check(matches(not(isChecked())));
    }

    @Test
    public void markGoalAsAarchiveAndActiveOnDetailScreen_goalIsActiveInList() {
        viewAllGoals();

        // Add 1 active goal
        createGoal(TITLE1, DESCRIPTION);

        // Click on the goal on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in goal details screen
        onView(withId(R.id.goal_detail_archived)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.goal_detail_archived)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the goal is marked as active
        onView(allOf(withId(R.id.archived),
                hasSibling(withText(TITLE1)))).check(matches(not(isChecked())));
    }

    @Test
    public void markGoalAsActiveAndArchiveOnDetailScreen_goalIsArchiveInList() {
        viewAllGoals();

        // Add 1 archived goal
        createGoal(TITLE1, DESCRIPTION);
        clickCheckBoxForGoal(TITLE1);

        // Click on the goal on the list
        onView(withText(TITLE1)).perform(click());

        // Click on the checkbox in goal details screen
        onView(withId(R.id.goal_detail_archived)).perform(click());

        // Click again to restore it to original state
        onView(withId(R.id.goal_detail_archived)).perform(click());

        // Click on the navigation up button to go back to the list
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());

        // Check that the goal is marked as active
        onView(allOf(withId(R.id.archived),
                hasSibling(withText(TITLE1)))).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_FilterActivePersists() {

        // Add a archived goal
        createGoal(TITLE1, DESCRIPTION);
        clickCheckBoxForGoal(TITLE1);

        // when switching to active goals
        viewActiveGoals();

        // then no goals should appear
        onView(withText(TITLE1)).check(matches(not(isDisplayed())));

        // when rotating the screen
        TestUtils.rotateOrientation(mGoalsActivityTestRule.getActivity());

        // then nothing changes
        onView(withText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void orientationChange_FilterArchivedPersists() {

        // Add a archived goal
        createGoal(TITLE1, DESCRIPTION);
        clickCheckBoxForGoal(TITLE1);

        // when switching to archived goals
        viewArchivedGoals();

        // the archived goal should be displayed
        onView(withText(TITLE1)).check(matches(isDisplayed()));

        // when rotating the screen
        TestUtils.rotateOrientation(mGoalsActivityTestRule.getActivity());

        // then nothing changes
        onView(withText(TITLE1)).check(matches(isDisplayed()));
        onView(withText(R.string.label_archived)).check(matches(isDisplayed()));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_ChangePersists() throws Throwable {
        // Add a archived goal
        createGoal(TITLE1, DESCRIPTION);

        // Open the goal in details view
        onView(withText(TITLE1)).perform(click());

        // Click on the edit goal button
        onView(withId(R.id.fab_edit_goal)).perform(click());

        // Change goal mTitle (but don't save)
        onView(withId(R.id.add_goal_title))
                .perform(replaceText(TITLE2), closeSoftKeyboard()); // Type new goal mTitle

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Verify goal mTitle is restored
        onView(withId(R.id.add_goal_title)).check(matches(withText(TITLE2)));
    }

    @Test
    @SdkSuppress(minSdkVersion = 21) // Blinking cursor after rotation breaks this in API 19
    public void orientationChange_DuringEdit_NoDuplicate() throws IllegalStateException {
        // Add a archived goal
        createGoal(TITLE1, DESCRIPTION);

        // Open the goal in details view
        onView(withText(TITLE1)).perform(click());

        // Click on the edit goal button
        onView(withId(R.id.fab_edit_goal)).perform(click());

        // Rotate the screen
        TestUtils.rotateOrientation(getCurrentActivity());

        // Edit goal mTitle and description
        onView(withId(R.id.add_goal_title))
                .perform(replaceText(TITLE2), closeSoftKeyboard()); // Type new goal mTitle

        // Save the goal
        onView(withId(R.id.fab_edit_goal_done)).perform(click());

        // Verify goal is displayed on screen in the goal list.
        onView(withItemText(TITLE2)).check(matches(isDisplayed()));

        // Verify previous goal is not displayed
        onView(withItemText(TITLE1)).check(doesNotExist());
    }

    @Test
    public void noGoals_AllGoalsFilter_AddGoalViewVisible() {
        // Given an empty list of goals, make sure "All goals" filter is on
        viewAllGoals();

        // Add goal View should be displayed
        onView(withId(R.id.noGoalsAdd)).check(matches(isDisplayed()));
    }

    @Test
    public void noGoals_ArchivedGoalsFilter_AddGoalViewNotVisible() {
        // Given an empty list of goals, make sure "All goals" filter is on
        viewArchivedGoals();

        // Add goal View should be displayed
        onView(withId(R.id.noGoalsAdd)).check(matches(not(isDisplayed())));
    }

    @Test
    public void noGoals_ActiveGoalsFilter_AddGoalViewNotVisible() {
        // Given an empty list of goals, make sure "All goals" filter is on
        viewActiveGoals();

        // Add goal View should be displayed
        onView(withId(R.id.noGoalsAdd)).check(matches(not(isDisplayed())));
    }

    private void viewAllGoals() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_all)).perform(click());
    }

    private void viewActiveGoals() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_active)).perform(click());
    }

    private void viewArchivedGoals() {
        onView(withId(R.id.menu_filter)).perform(click());
        onView(withText(R.string.nav_archived)).perform(click());
    }

    private void createGoal(String title, String description) {
        // Click on the add goal button
        onView(withId(R.id.fab_add_goal)).perform(click());

        // Add goal mTitle and description
        onView(withId(R.id.add_goal_title)).perform(typeText(title),
                closeSoftKeyboard()); // Type new goal mTitle

        // Save the goal
        onView(withId(R.id.fab_edit_goal_done)).perform(click());
    }

    private void clickCheckBoxForGoal(String title) {
        onView(allOf(withId(R.id.archived), hasSibling(withText(title)))).perform(click());
    }

    private String getText(int stringId) {
        return mGoalsActivityTestRule.getActivity().getResources().getString(stringId);
    }

    private String getToolbarNavigationContentDescription() {
        return TestUtils.getToolbarNavigationContentDescription(
                mGoalsActivityTestRule.getActivity(), R.id.toolbar);
    }
}
