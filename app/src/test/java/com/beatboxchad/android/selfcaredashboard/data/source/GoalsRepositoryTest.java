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

package com.beatboxchad.android.selfcaredashboard.data.source;

import android.content.Context;

import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class GoalsRepositoryTest {

    private final static String GOAL_TITLE = "title";

    private final static String GOAL_TITLE2 = "title2";

    private final static String GOAL_TITLE3 = "title3";

    private static List<Goal> GOALS = Lists.newArrayList(new Goal("Title1", "Description1"),
            new Goal("Title2", "Description2"));

    private GoalsRepository mGoalsRepository;

    @Mock
    private GoalsDataSource mGoalsRemoteDataSource;

    @Mock
    private GoalsDataSource mGoalsLocalDataSource;

    @Mock
    private GoalsDataSource.GetGoalCallback mGetGoalCallback;

    @Mock
    private GoalsDataSource.LoadGoalsCallback mLoadGoalsCallback;

    @Captor
    private ArgumentCaptor<GoalsDataSource.LoadGoalsCallback> mGoalsCallbackCaptor;

    @Captor
    private ArgumentCaptor<GoalsDataSource.GetGoalCallback> mGoalCallbackCaptor;

    @Before
    public void setupGoalsRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mGoalsRepository = GoalsRepository.getInstance(
                mGoalsRemoteDataSource, mGoalsLocalDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        GoalsRepository.destroyInstance();
    }

    @Test
    public void getGoals_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the goals repository
        twoGoalsLoadCallsToRepository(mLoadGoalsCallback);

        // Then goals were only requested once from Service API
        verify(mGoalsRemoteDataSource).getGoals(any(GoalsDataSource.LoadGoalsCallback.class));
    }

    @Test
    public void getGoals_requestsAllGoalsFromLocalDataSource() {
        // When goals are requested from the goals repository
        mGoalsRepository.getGoals(mLoadGoalsCallback);

        // Then goals are loaded from the local data source
        verify(mGoalsLocalDataSource).getGoals(any(GoalsDataSource.LoadGoalsCallback.class));
    }

    @Test
    public void saveGoal_savesGoalToServiceAPI() {
        // Given a stub goal with title and description
        Goal newGoal = new Goal(GOAL_TITLE, "Some Goal Description");

        // When a goal is saved to the goals repository
        mGoalsRepository.saveGoal(newGoal);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mGoalsRemoteDataSource).saveGoal(newGoal);
        verify(mGoalsLocalDataSource).saveGoal(newGoal);
        assertThat(mGoalsRepository.mCachedGoals.size(), is(1));
    }

    @Test
    public void completeGoal_completesGoalToServiceAPIUpdatesCache() {
        // Given a stub active goal with title and description added in the repository
        Goal newGoal = new Goal(GOAL_TITLE, "Some Goal Description");
        mGoalsRepository.saveGoal(newGoal);

        // When a goal is completed to the goals repository
        mGoalsRepository.completeGoal(newGoal);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mGoalsRemoteDataSource).completeGoal(newGoal);
        verify(mGoalsLocalDataSource).completeGoal(newGoal);
        assertThat(mGoalsRepository.mCachedGoals.size(), is(1));
        assertThat(mGoalsRepository.mCachedGoals.get(newGoal.getId()).isActive(), is(false));
    }

    @Test
    public void completeGoalId_completesGoalToServiceAPIUpdatesCache() {
        // Given a stub active goal with title and description added in the repository
        Goal newGoal = new Goal(GOAL_TITLE, "Some Goal Description");
        mGoalsRepository.saveGoal(newGoal);

        // When a goal is completed using its id to the goals repository
        mGoalsRepository.completeGoal(newGoal.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mGoalsRemoteDataSource).completeGoal(newGoal);
        verify(mGoalsLocalDataSource).completeGoal(newGoal);
        assertThat(mGoalsRepository.mCachedGoals.size(), is(1));
        assertThat(mGoalsRepository.mCachedGoals.get(newGoal.getId()).isActive(), is(false));
    }

    @Test
    public void activateGoal_activatesGoalToServiceAPIUpdatesCache() {
        // Given a stub completed goal with title and description in the repository
        Goal newGoal = new Goal(GOAL_TITLE, "Some Goal Description", true);
        mGoalsRepository.saveGoal(newGoal);

        // When a completed goal is activated to the goals repository
        mGoalsRepository.activateGoal(newGoal);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mGoalsRemoteDataSource).activateGoal(newGoal);
        verify(mGoalsLocalDataSource).activateGoal(newGoal);
        assertThat(mGoalsRepository.mCachedGoals.size(), is(1));
        assertThat(mGoalsRepository.mCachedGoals.get(newGoal.getId()).isActive(), is(true));
    }

    @Test
    public void activateGoalId_activatesGoalToServiceAPIUpdatesCache() {
        // Given a stub completed goal with title and description in the repository
        Goal newGoal = new Goal(GOAL_TITLE, "Some Goal Description", true);
        mGoalsRepository.saveGoal(newGoal);

        // When a completed goal is activated with its id to the goals repository
        mGoalsRepository.activateGoal(newGoal.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mGoalsRemoteDataSource).activateGoal(newGoal);
        verify(mGoalsLocalDataSource).activateGoal(newGoal);
        assertThat(mGoalsRepository.mCachedGoals.size(), is(1));
        assertThat(mGoalsRepository.mCachedGoals.get(newGoal.getId()).isActive(), is(true));
    }

    @Test
    public void getGoal_requestsSingleGoalFromLocalDataSource() {
        // When a goal is requested from the goals repository
        mGoalsRepository.getGoal(GOAL_TITLE, mGetGoalCallback);

        // Then the goal is loaded from the database
        verify(mGoalsLocalDataSource).getGoal(eq(GOAL_TITLE), any(
                GoalsDataSource.GetGoalCallback.class));
    }

    @Test
    public void deleteCompletedGoals_deleteCompletedGoalsToServiceAPIUpdatesCache() {
        // Given 2 stub completed goals and 1 stub active goals in the repository
        Goal newGoal = new Goal(GOAL_TITLE, "Some Goal Description", true);
        mGoalsRepository.saveGoal(newGoal);
        Goal newGoal2 = new Goal(GOAL_TITLE2, "Some Goal Description");
        mGoalsRepository.saveGoal(newGoal2);
        Goal newGoal3 = new Goal(GOAL_TITLE3, "Some Goal Description", true);
        mGoalsRepository.saveGoal(newGoal3);

        // When a completed goals are cleared to the goals repository
        mGoalsRepository.clearCompletedGoals();


        // Then the service API and persistent repository are called and the cache is updated
        verify(mGoalsRemoteDataSource).clearCompletedGoals();
        verify(mGoalsLocalDataSource).clearCompletedGoals();

        assertThat(mGoalsRepository.mCachedGoals.size(), is(1));
        assertTrue(mGoalsRepository.mCachedGoals.get(newGoal2.getId()).isActive());
        assertThat(mGoalsRepository.mCachedGoals.get(newGoal2.getId()).getTitle(), is(GOAL_TITLE2));
    }

    @Test
    public void deleteAllGoals_deleteGoalsToServiceAPIUpdatesCache() {
        // Given 2 stub completed goals and 1 stub active goals in the repository
        Goal newGoal = new Goal(GOAL_TITLE, "Some Goal Description", true);
        mGoalsRepository.saveGoal(newGoal);
        Goal newGoal2 = new Goal(GOAL_TITLE2, "Some Goal Description");
        mGoalsRepository.saveGoal(newGoal2);
        Goal newGoal3 = new Goal(GOAL_TITLE3, "Some Goal Description", true);
        mGoalsRepository.saveGoal(newGoal3);

        // When all goals are deleted to the goals repository
        mGoalsRepository.deleteAllGoals();

        // Verify the data sources were called
        verify(mGoalsRemoteDataSource).deleteAllGoals();
        verify(mGoalsLocalDataSource).deleteAllGoals();

        assertThat(mGoalsRepository.mCachedGoals.size(), is(0));
    }

    @Test
    public void deleteGoal_deleteGoalToServiceAPIRemovedFromCache() {
        // Given a goal in the repository
        Goal newGoal = new Goal(GOAL_TITLE, "Some Goal Description", true);
        mGoalsRepository.saveGoal(newGoal);
        assertThat(mGoalsRepository.mCachedGoals.containsKey(newGoal.getId()), is(true));

        // When deleted
        mGoalsRepository.deleteGoal(newGoal.getId());

        // Verify the data sources were called
        verify(mGoalsRemoteDataSource).deleteGoal(newGoal.getId());
        verify(mGoalsLocalDataSource).deleteGoal(newGoal.getId());

        // Verify it's removed from repository
        assertThat(mGoalsRepository.mCachedGoals.containsKey(newGoal.getId()), is(false));
    }

    @Test
    public void getGoalsWithDirtyCache_goalsAreRetrievedFromRemote() {
        // When calling getGoals in the repository with dirty cache
        mGoalsRepository.refreshGoals();
        mGoalsRepository.getGoals(mLoadGoalsCallback);

        // And the remote data source has data available
        setGoalsAvailable(mGoalsRemoteDataSource, GOALS);

        // Verify the goals from the remote data source are returned, not the local
        verify(mGoalsLocalDataSource, never()).getGoals(mLoadGoalsCallback);
        verify(mLoadGoalsCallback).onGoalsLoaded(GOALS);
    }

    @Test
    public void getGoalsWithLocalDataSourceUnavailable_goalsAreRetrievedFromRemote() {
        // When calling getGoals in the repository
        mGoalsRepository.getGoals(mLoadGoalsCallback);

        // And the local data source has no data available
        setGoalsNotAvailable(mGoalsLocalDataSource);

        // And the remote data source has data available
        setGoalsAvailable(mGoalsRemoteDataSource, GOALS);

        // Verify the goals from the local data source are returned
        verify(mLoadGoalsCallback).onGoalsLoaded(GOALS);
    }

    @Test
    public void getGoalsWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getGoals in the repository
        mGoalsRepository.getGoals(mLoadGoalsCallback);

        // And the local data source has no data available
        setGoalsNotAvailable(mGoalsLocalDataSource);

        // And the remote data source has no data available
        setGoalsNotAvailable(mGoalsRemoteDataSource);

        // Verify no data is returned
        verify(mLoadGoalsCallback).onDataNotAvailable();
    }

    @Test
    public void getGoalWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a goal id
        final String goalId = "123";

        // When calling getGoal in the repository
        mGoalsRepository.getGoal(goalId, mGetGoalCallback);

        // And the local data source has no data available
        setGoalNotAvailable(mGoalsLocalDataSource, goalId);

        // And the remote data source has no data available
        setGoalNotAvailable(mGoalsRemoteDataSource, goalId);

        // Verify no data is returned
        verify(mGetGoalCallback).onDataNotAvailable();
    }

    @Test
    public void getGoals_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mGoalsRepository.refreshGoals();

        // When calling getGoals in the repository
        mGoalsRepository.getGoals(mLoadGoalsCallback);

        // Make the remote data source return data
        setGoalsAvailable(mGoalsRemoteDataSource, GOALS);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mGoalsLocalDataSource, times(GOALS.size())).saveGoal(any(Goal.class));
    }

    /**
     * Convenience method that issues two calls to the goals repository
     */
    private void twoGoalsLoadCallsToRepository(GoalsDataSource.LoadGoalsCallback callback) {
        // When goals are requested from repository
        mGoalsRepository.getGoals(callback); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mGoalsLocalDataSource).getGoals(mGoalsCallbackCaptor.capture());

        // Local data source doesn't have data yet
        mGoalsCallbackCaptor.getValue().onDataNotAvailable();


        // Verify the remote data source is queried
        verify(mGoalsRemoteDataSource).getGoals(mGoalsCallbackCaptor.capture());

        // Trigger callback so goals are cached
        mGoalsCallbackCaptor.getValue().onGoalsLoaded(GOALS);

        mGoalsRepository.getGoals(callback); // Second call to API
    }

    private void setGoalsNotAvailable(GoalsDataSource dataSource) {
        verify(dataSource).getGoals(mGoalsCallbackCaptor.capture());
        mGoalsCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setGoalsAvailable(GoalsDataSource dataSource, List<Goal> goals) {
        verify(dataSource).getGoals(mGoalsCallbackCaptor.capture());
        mGoalsCallbackCaptor.getValue().onGoalsLoaded(goals);
    }

    private void setGoalNotAvailable(GoalsDataSource dataSource, String goalId) {
        verify(dataSource).getGoal(eq(goalId), mGoalCallbackCaptor.capture());
        mGoalCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setGoalAvailable(GoalsDataSource dataSource, Goal goal) {
        verify(dataSource).getGoal(eq(goal.getId()), mGoalCallbackCaptor.capture());
        mGoalCallbackCaptor.getValue().onGoalLoaded(goal);
    }
}
