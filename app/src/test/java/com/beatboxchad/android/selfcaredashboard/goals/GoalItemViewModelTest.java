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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link GoalsViewModel}
 */
public class GoalItemViewModelTest {

    private static final String NO_DATA_STRING = "NO_DATA_STRING";

    private static final String NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING";

    @Mock
    private GoalsRepository mGoalsRepository;

    @Mock
    private Context mContext;

    @Mock
    private GoalsActivity mGoalItemNavigator;

    @Captor
    private ArgumentCaptor<GoalsDataSource.GetGoalCallback> mLoadGoalsCallbackCaptor;

    private GoalItemViewModel mGoalItemViewModel;

    private Goal mGoal;

    @Before
    public void setupGoalsViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        // Get a reference to the class under test
        mGoalItemViewModel = new GoalItemViewModel(mContext, mGoalsRepository);
        mGoalItemViewModel.setNavigator(mGoalItemNavigator);

    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.no_data)).thenReturn(NO_DATA_STRING);
        when(mContext.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING);

        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void clickOnGoal_ShowsDetailUi() {
        loadGoalIntoViewModel();

        mLoadGoalsCallbackCaptor.getValue().onGoalLoaded(mGoal); // Trigger callback

        // Then goal detail UI is shown
        assertEquals(mGoalItemViewModel.mTitle.get(), mGoal.getTitle());
        assertEquals(mGoalItemViewModel.description.get(), mGoal.getDescription());
    }

    @Test
    public void nullGoal_showsNoData() {
        loadGoalIntoViewModel();

        // Load something different from null first (otherwise the change callback doesn't run)
        mLoadGoalsCallbackCaptor.getValue().onGoalLoaded(mGoal);
        mLoadGoalsCallbackCaptor.getValue().onGoalLoaded(null); // Trigger callback

        // Then goal detail UI is shown
        assertEquals(mGoalItemViewModel.mTitle.get(), NO_DATA_STRING);
        assertEquals(mGoalItemViewModel.description.get(), NO_DATA_DESC_STRING);
    }

    @Test
    public void completeGoal_ShowsGoalMarkedComplete() {
        loadGoalIntoViewModel();

        mLoadGoalsCallbackCaptor.getValue().onGoalLoaded(mGoal); // Trigger callback

        // When goal is marked as complete
        mGoalItemViewModel.setArchived(true);

        // Then repository is called
        verify(mGoalsRepository).completeGoal(mGoal);
    }

    @Test
    public void activateGoal_ShowsGoalMarkedActive() {
        loadGoalIntoViewModel();

        mLoadGoalsCallbackCaptor.getValue().onGoalLoaded(mGoal); // Trigger callback

        // When goal is marked as complete
        mGoalItemViewModel.setArchived(false);

        // Then repository is called
        verify(mGoalsRepository).activateGoal(mGoal);
    }

    @Test
    public void unavailableGoals_ShowsError() {
        loadGoalIntoViewModel();

        mLoadGoalsCallbackCaptor.getValue().onDataNotAvailable(); // Trigger callback

        // Then repository is called
        assertFalse(mGoalItemViewModel.isDataAvailable());
    }

    private void loadGoalIntoViewModel() {
        // Given a stubbed active goal
        mGoal = new Goal("Details Requested", "For this goal");

        // When open goal details is requested
        mGoalItemViewModel.start(mGoal.getId());

        // Use a captor to get a reference for the callback.
        verify(mGoalsRepository).getGoal(eq(mGoal.getId()), mLoadGoalsCallbackCaptor.capture());
    }
}