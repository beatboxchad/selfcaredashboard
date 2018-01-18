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


import android.content.Context;

import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link AddEditGoalViewModel}.
 */
public class AddEditGoalViewModelTest {

    public static final String SNACKBAR_TEXT = "Snackbar text";
    @Mock
    private GoalsRepository mGoalsRepository;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<GoalsDataSource.GetGoalCallback> mGetGoalCallbackCaptor;

    private AddEditGoalViewModel mAddEditGoalViewModel;

    @Before
    public void setupAddEditGoalViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mAddEditGoalViewModel = new AddEditGoalViewModel(
                mock(Context.class), mGoalsRepository);
        mAddEditGoalViewModel.onActivityCreated(mock(AddEditGoalActivity.class));
    }

    @Test
    public void saveNewGoalToRepository_showsSuccessMessageUi() {
        // When the ViewModel is asked to save a goal
        mAddEditGoalViewModel.description.set("Some Goal Description");
        mAddEditGoalViewModel.title.set("New Goal Title");
        mAddEditGoalViewModel.saveGoal();

        // Then a goal is saved in the repository and the view updated
        verify(mGoalsRepository).saveGoal(any(Goal.class)); // saved to the model
    }

    @Test
    public void populateGoal_callsRepoAndUpdatesView() {
        Goal testGoal = new Goal("TITLE", "DESCRIPTION", "1");

        // Get a reference to the class under test
        mAddEditGoalViewModel = new AddEditGoalViewModel(
                mock(Context.class), mGoalsRepository);
        mAddEditGoalViewModel.onActivityCreated(mock(AddEditGoalActivity.class));


        // When the ViewModel is asked to populate an existing goal
        mAddEditGoalViewModel.start(testGoal.getId());

        // Then the goal repository is queried and the view updated
        verify(mGoalsRepository).getGoal(eq(testGoal.getId()), mGetGoalCallbackCaptor.capture());

        // Simulate callback
        mGetGoalCallbackCaptor.getValue().onGoalLoaded(testGoal);

        // Verify the fields were updated
        assertThat(mAddEditGoalViewModel.title.get(), is(testGoal.getTitle()));
        assertThat(mAddEditGoalViewModel.description.get(), is(testGoal.getDescription()));
    }

    @Test
    public void updateSnackbar_nullValue() {
        // Get a reference to the class under test
        mAddEditGoalViewModel = new AddEditGoalViewModel(
                mock(Context.class), mGoalsRepository);
        mAddEditGoalViewModel.onActivityCreated(mock(AddEditGoalActivity.class));

        // Before setting the Snackbar text, get its current value
        String snackbarText = mAddEditGoalViewModel.getSnackbarText();

        // Check that the value is null
        assertThat("Snackbar text does not match", snackbarText, is(nullValue()));
    }

    @Test
    public void updateSnackbar_nonNullValue() {
        // Get a reference to the class under test
        mAddEditGoalViewModel = new AddEditGoalViewModel(
                mock(Context.class), mGoalsRepository);
        mAddEditGoalViewModel.onActivityCreated(mock(AddEditGoalActivity.class));

        // Set a new value for the Snackbar text via the public Observable
        mAddEditGoalViewModel.snackbarText.set(SNACKBAR_TEXT);

        // Get its current value with the Snackbar text getter
        String snackbarText = mAddEditGoalViewModel.getSnackbarText();

        // Check that the value matches the observable's.
        assertThat("Snackbar text does not match", snackbarText, is(SNACKBAR_TEXT));
    }
}
