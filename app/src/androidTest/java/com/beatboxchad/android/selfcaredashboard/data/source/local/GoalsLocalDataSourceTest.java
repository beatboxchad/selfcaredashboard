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

package com.beatboxchad.android.selfcaredashboard.data.source.local;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.util.SingleExecutors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

/**
 * Integration test for the {@link GoalsDataSource}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class GoalsLocalDataSourceTest {

    private final static String TITLE = "title";

    private final static String TITLE2 = "title2";

    private final static String TITLE3 = "title3";

    private GoalsLocalDataSource mLocalDataSource;

    private ToDoDatabase mDatabase;

    @Before
    public void setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                ToDoDatabase.class)
                .build();
        GoalsDao goalsDao = mDatabase.goalDao();

        // Make sure that we're not keeping a reference to the wrong instance.
        GoalsLocalDataSource.clearInstance();
        mLocalDataSource = GoalsLocalDataSource.getInstance(new SingleExecutors(), goalsDao);
    }

    @After
    public void cleanUp() {
        mDatabase.close();
        GoalsLocalDataSource.clearInstance();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mLocalDataSource);
    }

    @Test
    public void saveGoal_retrievesGoal() {
        // Given a new goal
        final Goal newGoal = new Goal(TITLE, 3, false);

        // When saved into the persistent repository
        mLocalDataSource.saveGoal(newGoal);

        // Then the goal can be retrieved from the persistent repository
        mLocalDataSource.getGoal(newGoal.getId(), new GoalsDataSource.GetGoalCallback() {
            @Override
            public void onGoalLoaded(Goal goal) {
                assertThat(goal, is(newGoal));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void archiveGoal_retrievedGoalIsArchive() {
        // Initialize mock for the callback.
        GoalsDataSource.GetGoalCallback callback = mock(GoalsDataSource.GetGoalCallback.class);
        // Given a new goal in the persistent repository
        final Goal newGoal = new Goal(TITLE, 1, false);
        mLocalDataSource.saveGoal(newGoal);

        // When archived in the persistent repository
        mLocalDataSource.archiveGoal(newGoal);

        // Then the goal can be retrieved from the persistent repository and is archive
        mLocalDataSource.getGoal(newGoal.getId(), new GoalsDataSource.GetGoalCallback() {
            @Override
            public void onGoalLoaded(Goal goal) {
                assertThat(goal, is(newGoal));
                assertThat(goal.isPolarity(), is(true));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void activateGoal_retrievedGoalIsActive() {
        // Initialize mock for the callback.
        GoalsDataSource.GetGoalCallback callback = mock(GoalsDataSource.GetGoalCallback.class);

        // Given a new archived goal in the persistent repository
        final Goal newGoal = new Goal(TITLE, 1, false);
        mLocalDataSource.saveGoal(newGoal);
        mLocalDataSource.archiveGoal(newGoal);

        // When activated in the persistent repository
        mLocalDataSource.activateGoal(newGoal);

        // Then the goal can be retrieved from the persistent repository and is active
        mLocalDataSource.getGoal(newGoal.getId(), callback);

        verify(callback, never()).onDataNotAvailable();
        verify(callback).onGoalLoaded(newGoal);

        assertThat(newGoal.isPolarity(), is(false));
    }

    @Test
    public void clearArchivedGoal_goalNotRetrievable() {
        // Initialize mocks for the callbacks.
        GoalsDataSource.GetGoalCallback callback1 = mock(GoalsDataSource.GetGoalCallback.class);
        GoalsDataSource.GetGoalCallback callback2 = mock(GoalsDataSource.GetGoalCallback.class);
        GoalsDataSource.GetGoalCallback callback3 = mock(GoalsDataSource.GetGoalCallback.class);

        // Given 2 new archived goals and 1 active goal in the persistent repository
        final Goal newGoal1 = new Goal(TITLE);
        mLocalDataSource.saveGoal(newGoal1);
        mLocalDataSource.archiveGoal(newGoal1);
        final Goal newGoal2 = new Goal(TITLE2);
        mLocalDataSource.saveGoal(newGoal2);
        mLocalDataSource.archiveGoal(newGoal2);
        final Goal newGoal3 = new Goal(TITLE3, "");
        mLocalDataSource.saveGoal(newGoal3);

        // When archived goals are cleared in the repository
        mLocalDataSource.clearArchivedGoals();

        // Then the archived goals cannot be retrieved and the active one can
        mLocalDataSource.getGoal(newGoal1.getId(), callback1);

        verify(callback1).onDataNotAvailable();
        verify(callback1, never()).onGoalLoaded(newGoal1);

        mLocalDataSource.getGoal(newGoal2.getId(), callback2);

        verify(callback2).onDataNotAvailable();
        verify(callback2, never()).onGoalLoaded(newGoal2);

        mLocalDataSource.getGoal(newGoal3.getId(), callback3);

        verify(callback3, never()).onDataNotAvailable();
        verify(callback3).onGoalLoaded(newGoal3);
    }

    @Test
    public void deleteAllGoals_emptyListOfRetrievedGoal() {
        // Given a new goal in the persistent repository and a mocked callback
        Goal newGoal = new Goal(TITLE, "");
        mLocalDataSource.saveGoal(newGoal);
        GoalsDataSource.LoadGoalsCallback callback = mock(GoalsDataSource.LoadGoalsCallback.class);

        // When all goals are deleted
        mLocalDataSource.deleteAllGoals();

        // Then the retrieved goals is an empty list
        mLocalDataSource.getGoals(callback);

        verify(callback).onDataNotAvailable();
        verify(callback, never()).onGoalsLoaded(anyList());
    }

    @Test
    public void getGoals_retrieveSavedGoals() {
        // Given 2 new goals in the persistent repository
        final Goal newGoal1 = new Goal(TITLE, "");
        mLocalDataSource.saveGoal(newGoal1);
        final Goal newGoal2 = new Goal(TITLE, "");
        mLocalDataSource.saveGoal(newGoal2);

        // Then the goals can be retrieved from the persistent repository
        mLocalDataSource.getGoals(new GoalsDataSource.LoadGoalsCallback() {
            @Override
            public void onGoalsLoaded(List<Goal> goals) {
                assertNotNull(goals);
                assertTrue(goals.size() >= 2);

                boolean newGoal1IdFound = false;
                boolean newGoal2IdFound = false;
                for (Goal goal : goals) {
                    if (goal.getId().equals(newGoal1.getId())) {
                        newGoal1IdFound = true;
                    }
                    if (goal.getId().equals(newGoal2.getId())) {
                        newGoal2IdFound = true;
                    }
                }
                assertTrue(newGoal1IdFound);
                assertTrue(newGoal2IdFound);
            }

            @Override
            public void onDataNotAvailable() {
                fail();
            }
        });
    }
}
