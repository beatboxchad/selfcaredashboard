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


import android.content.Context;
import android.content.res.Resources;

import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link GoalDetailViewModel}
 */
public class GoalDetailViewModelTest {

    private static final String TITLE_TEST = "mTitle";

    private static final String DESCRIPTION_TEST = "description";

    private static final String NO_DATA_STRING = "NO_DATA_STRING";

    private static final String NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING";

    public static final String SNACKBAR_TEXT = "Snackbar text";

    @Mock
    private GoalsRepository mGoalsRepository;

    @Mock
    private Context mContext;

    @Mock
    private GoalsDataSource.GetGoalCallback mRepositoryCallback;

    @Mock
    private GoalsDataSource.GetGoalCallback mViewModelCallback;

    @Captor
    private ArgumentCaptor<GoalsDataSource.GetGoalCallback> mGetGoalCallbackCaptor;

    private GoalDetailViewModel mGoalDetailViewModel;

    private Goal mGoal;

    @Before
    public void setupGoalsViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        mGoal = new Goal(TITLE_TEST, DESCRIPTION_TEST);

        // Get a reference to the class under test
        mGoalDetailViewModel = new GoalDetailViewModel(
                mContext, mGoalsRepository);
        mGoalDetailViewModel.setNavigator(mock(GoalDetailActivity.class));
    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.no_data)).thenReturn(NO_DATA_STRING);
        when(mContext.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING);
        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void getActiveGoalFromRepositoryAndLoadIntoView() {
        setupViewModelRepositoryCallback();

        // Then verify that the view was notified
        assertEquals(mGoalDetailViewModel.mTitle.get(), mGoal.getTitle());
        assertEquals(mGoalDetailViewModel.description.get(), mGoal.getDescription());
    }

    @Test
    public void deleteGoal() {
        setupViewModelRepositoryCallback();

        // When the deletion of a goal is requested
        mGoalDetailViewModel.deleteGoal();

        // Then the repository is notified
        verify(mGoalsRepository).deleteGoal(mGoal.getId());
    }

    @Test
    public void completeGoal() {
        setupViewModelRepositoryCallback();

        // When the ViewModel is asked to complete the goal
        mGoalDetailViewModel.setCompleted(true);

        // Then a request is sent to the goal repository and the UI is updated
        verify(mGoalsRepository).completeGoal(mGoal);
    }

    @Test
    public void activateGoal() {
        setupViewModelRepositoryCallback();

        // When the ViewModel is asked to complete the goal
        mGoalDetailViewModel.setCompleted(false);

        // Then a request is sent to the goal repository and the UI is updated
        verify(mGoalsRepository).activateGoal(mGoal);
    }

    @Test
    public void GoalDetailViewModel_repositoryError() {
        // Given an initialized ViewModel with an active goal
        mViewModelCallback = mock(GoalsDataSource.GetGoalCallback.class);

        mGoalDetailViewModel.start(mGoal.getId());

        // Use a captor to get a reference for the callback.
        verify(mGoalsRepository).getGoal(eq(mGoal.getId()), mGetGoalCallbackCaptor.capture());

        // When the repository returns an error
        mGetGoalCallbackCaptor.getValue().onDataNotAvailable(); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mGoalDetailViewModel.isDataAvailable());
    }

    @Test
    public void GoalDetailViewModel_repositoryNull() {
        setupViewModelRepositoryCallback();

        // When the repository returns a null goal
        mGetGoalCallbackCaptor.getValue().onGoalLoaded(null); // Trigger callback error

        // Then verify that data is not available
        assertFalse(mGoalDetailViewModel.isDataAvailable());

        // Then goal detail UI is shown
        assertEquals(mGoalDetailViewModel.mTitle.get(), NO_DATA_STRING);
        assertEquals(mGoalDetailViewModel.description.get(), NO_DATA_DESC_STRING);
    }

    private void setupViewModelRepositoryCallback() {
        // Given an initialized ViewModel with an active goal
        mViewModelCallback = mock(GoalsDataSource.GetGoalCallback.class);

        mGoalDetailViewModel.start(mGoal.getId());

        // Use a captor to get a reference for the callback.
        verify(mGoalsRepository).getGoal(eq(mGoal.getId()), mGetGoalCallbackCaptor.capture());

        mGetGoalCallbackCaptor.getValue().onGoalLoaded(mGoal); // Trigger callback
    }

    @Test
    public void updateSnackbar_nullValue() {
        // Before setting the Snackbar text, get its current value
        String snackbarText = mGoalDetailViewModel.getSnackbarText();

        // Check that the value is null
        assertThat("Snackbar text does not match", snackbarText, is(nullValue()));
    }

    @Test
    public void updateSnackbar_nonNullValue() {
        // Set a new value for the Snackbar text via the public Observable
        mGoalDetailViewModel.snackbarText.set(SNACKBAR_TEXT);

        // Get its current value with the Snackbar text getter
        String snackbarText = mGoalDetailViewModel.getSnackbarText();

        // Check that the value matches the observable's.
        assertThat("Snackbar text does not match", snackbarText, is(SNACKBAR_TEXT));
    }
}
