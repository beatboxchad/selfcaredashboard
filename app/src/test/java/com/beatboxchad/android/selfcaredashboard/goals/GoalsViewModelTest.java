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

import android.content.Context;
import android.content.res.Resources;

import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.addeditgoal.AddEditGoalActivity;
import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource.LoadGoalsCallback;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;
import com.beatboxchad.android.selfcaredashboard.goaldetail.GoalDetailActivity;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link GoalsViewModel}
 */
public class GoalsViewModelTest {

    private static List<Goal> GOALS;

    @Mock
    private GoalsRepository mGoalsRepository;

    @Mock
    private Context mContext;

    @Mock
    private GoalsActivity mGoalsNavigator;

    @Captor
    private ArgumentCaptor<LoadGoalsCallback> mLoadGoalsCallbackCaptor;

    private GoalsViewModel mGoalsViewModel;

    private static final String SNACKBAR_TEXT = "Snackbar text";

    @Before
    public void setupGoalsViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        // Get a reference to the class under test
        mGoalsViewModel = new GoalsViewModel(
                mGoalsRepository, mContext);
        mGoalsViewModel.setNavigator(mGoalsNavigator);

        // We initialise the goals to 3, with one active and two archived
        GOALS = Lists.newArrayList(new Goal("Title1", "Description1"),
                new Goal("Title2", "Description2", true), new Goal("Title3", "Description3", true));
    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.successfully_saved_goal_message))
                .thenReturn("EDIT_RESULT_OK");
        when(mContext.getString(R.string.successfully_added_goal_message))
                .thenReturn("ADD_EDIT_RESULT_OK");
        when(mContext.getString(R.string.successfully_deleted_goal_message))
                .thenReturn("DELETE_RESULT_OK");

        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void loadAllGoalsFromRepository_dataLoaded() {
        // Given an initialized GoalsViewModel with initialized goals
        // When loading of Goals is requested
        mGoalsViewModel.setFiltering(GoalsFilterType.ALL_GOALS);
        mGoalsViewModel.loadGoals(true);

        // Callback is captured and invoked with stubbed goals
        verify(mGoalsRepository).getGoals(mLoadGoalsCallbackCaptor.capture());


        // Then progress indicator is shown
        assertTrue(mGoalsViewModel.dataLoading.get());
        mLoadGoalsCallbackCaptor.getValue().onGoalsLoaded(GOALS);

        // Then progress indicator is hidden
        assertFalse(mGoalsViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mGoalsViewModel.items.isEmpty());
        assertTrue(mGoalsViewModel.items.size() == 3);
    }

    @Test
    public void loadActiveGoalsFromRepositoryAndLoadIntoView() {
        // Given an initialized GoalsViewModel with initialized goals
        // When loading of Goals is requested
        mGoalsViewModel.setFiltering(GoalsFilterType.ACTIVE_GOALS);
        mGoalsViewModel.loadGoals(true);

        // Callback is captured and invoked with stubbed goals
        verify(mGoalsRepository).getGoals(mLoadGoalsCallbackCaptor.capture());
        mLoadGoalsCallbackCaptor.getValue().onGoalsLoaded(GOALS);

        // Then progress indicator is hidden
        assertFalse(mGoalsViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mGoalsViewModel.items.isEmpty());
        assertTrue(mGoalsViewModel.items.size() == 1);
    }

    @Test
    public void loadArchivedGoalsFromRepositoryAndLoadIntoView() {
        // Given an initialized GoalsViewModel with initialized goals
        // When loading of Goals is requested
        mGoalsViewModel.setFiltering(GoalsFilterType.ARCHIVED_GOALS);
        mGoalsViewModel.loadGoals(true);

        // Callback is captured and invoked with stubbed goals
        verify(mGoalsRepository).getGoals(mLoadGoalsCallbackCaptor.capture());
        mLoadGoalsCallbackCaptor.getValue().onGoalsLoaded(GOALS);

        // Then progress indicator is hidden
        assertFalse(mGoalsViewModel.dataLoading.get());

        // And data loaded
        assertFalse(mGoalsViewModel.items.isEmpty());
        assertTrue(mGoalsViewModel.items.size() == 2);
    }

    @Test
    public void clickOnFab_ShowsAddGoalUi() {
        // When adding a new goal
        mGoalsViewModel.addNewGoal();

        // Then the navigator is called
        verify(mGoalsNavigator).addNewGoal();
    }

    @Test
    public void clearArchivedGoals_ClearsGoals() {
        // When archived goals are cleared
        mGoalsViewModel.clearArchivedGoals();

        // Then repository is called and the view is notified
        verify(mGoalsRepository).clearArchivedGoals();
        verify(mGoalsRepository).getGoals(any(LoadGoalsCallback.class));
    }

    @Test
    public void handleActivityResult_editOK() {
        // When GoalDetailActivity sends a EDIT_RESULT_OK
        mGoalsViewModel.handleActivityResult(
                AddEditGoalActivity.REQUEST_CODE, GoalDetailActivity.EDIT_RESULT_OK);

        // Then the snackbar shows the correct message
        assertThat(mGoalsViewModel.getSnackbarText(), is("EDIT_RESULT_OK"));
    }

    @Test
    public void handleActivityResult_addEditOK() {
        // When AddEditGoalActivity sends a ADD_EDIT_RESULT_OK
        mGoalsViewModel.handleActivityResult(
                AddEditGoalActivity.REQUEST_CODE, AddEditGoalActivity.ADD_EDIT_RESULT_OK);

        // Then the snackbar shows the correct message
        assertThat(mGoalsViewModel.getSnackbarText(), is("ADD_EDIT_RESULT_OK"));
    }

    @Test
    public void handleActivityResult_deleteOk() {
        // When GoalDetailActivity sends a DELETE_RESULT_OK
        mGoalsViewModel.handleActivityResult(
                AddEditGoalActivity.REQUEST_CODE, GoalDetailActivity.DELETE_RESULT_OK);

        // Then the snackbar shows the correct message
        assertThat(mGoalsViewModel.getSnackbarText(), is("DELETE_RESULT_OK"));
    }

    @Test
    public void getGoalsAddViewVisible() {
        // When the filter type is ALL_GOALS
        mGoalsViewModel.setFiltering(GoalsFilterType.ALL_GOALS);

        // Then the "Add goal" action is visible
        assertThat(mGoalsViewModel.goalsAddViewVisible.get(), is(true));
    }

    @Test
    public void updateSnackbar_nullValue() {
        // Before setting the Snackbar text, get its current value
        String snackbarText = mGoalsViewModel.getSnackbarText();

        // Check that the value is null
        assertThat("Snackbar text does not match", snackbarText, is(nullValue()));
    }

    @Test
    public void updateSnackbar_nonNullValue() {
        // Set a new value for the Snackbar text via the public Observable
        mGoalsViewModel.snackbarText.set(SNACKBAR_TEXT);

        // Get its current value with the Snackbar text getter
        String snackbarText = mGoalsViewModel.getSnackbarText();

        // Check that the value matches the observable's.
        assertThat("Snackbar text does not match", snackbarText, is(SNACKBAR_TEXT));
    }
}
