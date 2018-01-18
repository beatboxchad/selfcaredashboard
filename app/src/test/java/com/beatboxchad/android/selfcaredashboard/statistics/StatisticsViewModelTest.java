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

package com.beatboxchad.android.selfcaredashboard.statistics;


import android.content.Context;

import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link StatisticsViewModel}
 */
public class StatisticsViewModelTest {

    private static List<Goal> GOALS;

    @Mock
    private GoalsRepository mGoalsRepository;

    @Captor
    private ArgumentCaptor<GoalsDataSource.LoadGoalsCallback> mLoadGoalsCallbackCaptor;

    private StatisticsViewModel mStatisticsViewModel;

    @Before
    public void setupStatisticsViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mStatisticsViewModel = new StatisticsViewModel(mock(Context.class), mGoalsRepository);

        // We initialise the goals to 3, with one active and two completed
        GOALS = Lists.newArrayList(new Goal("Title1", "Description1"),
                new Goal("Title2", "Description2", true), new Goal("Title3", "Description3", true));
    }

    @Test
    public void loadEmptyGoalsFromRepository_CallViewToDisplay() {
        // Given an initialized StatisticsViewModel with no goals
        GOALS.clear();

        // When loading of Goals is requested
        mStatisticsViewModel.loadStatistics();

        // Callback is captured and invoked with stubbed goals
        verify(mGoalsRepository).getGoals(mLoadGoalsCallbackCaptor.capture());
        mLoadGoalsCallbackCaptor.getValue().onGoalsLoaded(GOALS);

        // Then progress indicator is hidden and correct data is passed on to the view
        assertEquals(mStatisticsViewModel.isEmpty(), true);
        assertEquals(mStatisticsViewModel.mNumberOfActiveGoals, 0);
        assertEquals(mStatisticsViewModel.mNumberOfCompletedGoals, 0);
    }

    @Test
    public void loadNonEmptyGoalsFromRepository_CallViewToDisplay() {
        // When loading of Goals is requested
        mStatisticsViewModel.loadStatistics();

        // Callback is captured and invoked with stubbed goals
        verify(mGoalsRepository).getGoals(mLoadGoalsCallbackCaptor.capture());
        mLoadGoalsCallbackCaptor.getValue().onGoalsLoaded(GOALS);

        // Then progress indicator is hidden and correct data is passed on to the view
        assertEquals(mStatisticsViewModel.mNumberOfActiveGoals, 1);
        assertEquals(mStatisticsViewModel.mNumberOfCompletedGoals, 2);
    }


    @Test
    public void loadStatisticsWhenGoalsAreUnavailable_CallErrorToDisplay() {
        // When statistics are loaded
        mStatisticsViewModel.loadStatistics();

        // And goals data isn't available
        verify(mGoalsRepository).getGoals(mLoadGoalsCallbackCaptor.capture());
        mLoadGoalsCallbackCaptor.getValue().onDataNotAvailable();

        // Then an error message is shown
        assertEquals(mStatisticsViewModel.isEmpty(), true);
        assertEquals(mStatisticsViewModel.error.get(), true);
    }
}
