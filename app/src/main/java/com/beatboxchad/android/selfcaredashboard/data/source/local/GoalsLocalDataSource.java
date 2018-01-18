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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.util.AppExecutors;

import java.util.List;


/**
 * Concrete implementation of a data source as a db.
 */
public class GoalsLocalDataSource implements GoalsDataSource {

    private static volatile GoalsLocalDataSource INSTANCE;

    private GoalsDao mGoalsDao;

    private AppExecutors mAppExecutors;

    // Prevent direct instantiation.
    private GoalsLocalDataSource(@NonNull AppExecutors appExecutors,
            @NonNull GoalsDao goalsDao) {
        mAppExecutors = appExecutors;
        mGoalsDao = goalsDao;
    }

    public static GoalsLocalDataSource getInstance(@NonNull AppExecutors appExecutors,
            @NonNull GoalsDao goalsDao) {
        if (INSTANCE == null) {
            synchronized (GoalsLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GoalsLocalDataSource(appExecutors, goalsDao);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Note: {@link LoadGoalsCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public void getGoals(@NonNull final LoadGoalsCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<Goal> goals = mGoalsDao.getGoals();
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (goals.isEmpty()) {
                            // This will be called if the table is new or just empty.
                            callback.onDataNotAvailable();
                        } else {
                            callback.onGoalsLoaded(goals);
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    /**
     * Note: {@link GetGoalCallback#onDataNotAvailable()} is fired if the {@link Goal} isn't
     * found.
     */
    @Override
    public void getGoal(@NonNull final String goalId, @NonNull final GetGoalCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final Goal goal = mGoalsDao.getGoalById(goalId);

                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (goal != null) {
                            callback.onGoalLoaded(goal);
                        } else {
                            callback.onDataNotAvailable();
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveGoal(@NonNull final Goal goal) {
        checkNotNull(goal);
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                mGoalsDao.insertGoal(goal);
            }
        };
        mAppExecutors.diskIO().execute(saveRunnable);
    }

    @Override
    public void completeGoal(@NonNull final Goal goal) {
        Runnable completeRunnable = new Runnable() {
            @Override
            public void run() {
                mGoalsDao.updateCompleted(goal.getId(), true);
            }
        };

        mAppExecutors.diskIO().execute(completeRunnable);
    }

    @Override
    public void completeGoal(@NonNull String goalId) {
        // Not required for the local data source because the {@link GoalsRepository} handles
        // converting from a {@code goalId} to a {@link goal} using its cached data.
    }

    @Override
    public void activateGoal(@NonNull final Goal goal) {
        Runnable activateRunnable = new Runnable() {
            @Override
            public void run() {
                mGoalsDao.updateCompleted(goal.getId(), false);
            }
        };
        mAppExecutors.diskIO().execute(activateRunnable);
    }

    @Override
    public void activateGoal(@NonNull String goalId) {
        // Not required for the local data source because the {@link GoalsRepository} handles
        // converting from a {@code goalId} to a {@link goal} using its cached data.
    }

    @Override
    public void clearCompletedGoals() {
        Runnable clearGoalsRunnable = new Runnable() {
            @Override
            public void run() {
                mGoalsDao.deleteCompletedGoals();

            }
        };

        mAppExecutors.diskIO().execute(clearGoalsRunnable);
    }

    @Override
    public void refreshGoals() {
        // Not required because the {@link GoalsRepository} handles the logic of refreshing the
        // goals from all the available data sources.
    }

    @Override
    public void deleteAllGoals() {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mGoalsDao.deleteGoals();
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void deleteGoal(@NonNull final String goalId) {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mGoalsDao.deleteGoalById(goalId);
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @VisibleForTesting
    static void clearInstance() {
        INSTANCE = null;
    }
}
