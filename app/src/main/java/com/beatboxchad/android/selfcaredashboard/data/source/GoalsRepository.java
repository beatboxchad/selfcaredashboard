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

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.beatboxchad.android.selfcaredashboard.data.Goal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation to load goals from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class GoalsRepository implements GoalsDataSource {

    private static GoalsRepository INSTANCE = null;

    private final GoalsDataSource mGoalsRemoteDataSource;

    private final GoalsDataSource mGoalsLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Goal> mCachedGoals;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private GoalsRepository(@NonNull GoalsDataSource goalsRemoteDataSource,
                            @NonNull GoalsDataSource goalsLocalDataSource) {
        mGoalsRemoteDataSource = checkNotNull(goalsRemoteDataSource);
        mGoalsLocalDataSource = checkNotNull(goalsLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param goalsRemoteDataSource the backend data source
     * @param goalsLocalDataSource  the device storage data source
     * @return the {@link GoalsRepository} instance
     */
    public static GoalsRepository getInstance(GoalsDataSource goalsRemoteDataSource,
                                              GoalsDataSource goalsLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new GoalsRepository(goalsRemoteDataSource, goalsLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(GoalsDataSource, GoalsDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets goals from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadGoalsCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getGoals(@NonNull final LoadGoalsCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedGoals != null && !mCacheIsDirty) {
            callback.onGoalsLoaded(new ArrayList<>(mCachedGoals.values()));
            return;
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getGoalsFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mGoalsLocalDataSource.getGoals(new LoadGoalsCallback() {
                @Override
                public void onGoalsLoaded(List<Goal> goals) {
                    refreshCache(goals);
                    callback.onGoalsLoaded(new ArrayList<>(mCachedGoals.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getGoalsFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveGoal(@NonNull Goal goal) {
        checkNotNull(goal);
        mGoalsRemoteDataSource.saveGoal(goal);
        mGoalsLocalDataSource.saveGoal(goal);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGoals == null) {
            mCachedGoals = new LinkedHashMap<>();
        }
        mCachedGoals.put(goal.getId(), goal);
    }

    @Override
    public void completeGoal(@NonNull Goal goal) {
        checkNotNull(goal);
        mGoalsRemoteDataSource.completeGoal(goal);
        mGoalsLocalDataSource.completeGoal(goal);

        Goal completedGoal = new Goal(goal.getTitle(), goal.getDescription(), goal.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGoals == null) {
            mCachedGoals = new LinkedHashMap<>();
        }
        mCachedGoals.put(goal.getId(), completedGoal);
    }

    @Override
    public void completeGoal(@NonNull String goalId) {
        checkNotNull(goalId);
        completeGoal(getGoalWithId(goalId));
    }

    @Override
    public void activateGoal(@NonNull Goal goal) {
        checkNotNull(goal);
        mGoalsRemoteDataSource.activateGoal(goal);
        mGoalsLocalDataSource.activateGoal(goal);

        Goal activeGoal = new Goal(goal.getTitle(), goal.getDescription(), goal.getId());

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGoals == null) {
            mCachedGoals = new LinkedHashMap<>();
        }
        mCachedGoals.put(goal.getId(), activeGoal);
    }

    @Override
    public void activateGoal(@NonNull String goalId) {
        checkNotNull(goalId);
        activateGoal(getGoalWithId(goalId));
    }

    @Override
    public void clearCompletedGoals() {
        mGoalsRemoteDataSource.clearCompletedGoals();
        mGoalsLocalDataSource.clearCompletedGoals();

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGoals == null) {
            mCachedGoals = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Goal>> it = mCachedGoals.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Goal> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    /**
     * Gets goals from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetGoalCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getGoal(@NonNull final String goalId, @NonNull final GetGoalCallback callback) {
        checkNotNull(goalId);
        checkNotNull(callback);

        Goal cachedGoal = getGoalWithId(goalId);

        // Respond immediately with cache if available
        if (cachedGoal != null) {
            callback.onGoalLoaded(cachedGoal);
            return;
        }

        // Load from server/persisted if needed.

        // Is the goal in the local data source? If not, query the network.
        mGoalsLocalDataSource.getGoal(goalId, new GetGoalCallback() {
            @Override
            public void onGoalLoaded(Goal goal) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedGoals == null) {
                    mCachedGoals = new LinkedHashMap<>();
                }
                mCachedGoals.put(goal.getId(), goal);
                callback.onGoalLoaded(goal);
            }

            @Override
            public void onDataNotAvailable() {
                mGoalsRemoteDataSource.getGoal(goalId, new GetGoalCallback() {
                    @Override
                    public void onGoalLoaded(Goal goal) {
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedGoals == null) {
                            mCachedGoals = new LinkedHashMap<>();
                        }
                        mCachedGoals.put(goal.getId(), goal);
                        callback.onGoalLoaded(goal);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void refreshGoals() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllGoals() {
        mGoalsRemoteDataSource.deleteAllGoals();
        mGoalsLocalDataSource.deleteAllGoals();

        if (mCachedGoals == null) {
            mCachedGoals = new LinkedHashMap<>();
        }
        mCachedGoals.clear();
    }

    @Override
    public void deleteGoal(@NonNull String goalId) {
        mGoalsRemoteDataSource.deleteGoal(checkNotNull(goalId));
        mGoalsLocalDataSource.deleteGoal(checkNotNull(goalId));

        mCachedGoals.remove(goalId);
    }

    private void getGoalsFromRemoteDataSource(@NonNull final LoadGoalsCallback callback) {
        mGoalsRemoteDataSource.getGoals(new LoadGoalsCallback() {
            @Override
            public void onGoalsLoaded(List<Goal> goals) {
                refreshCache(goals);
                refreshLocalDataSource(goals);
                callback.onGoalsLoaded(new ArrayList<>(mCachedGoals.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Goal> goals) {
        if (mCachedGoals == null) {
            mCachedGoals = new LinkedHashMap<>();
        }
        mCachedGoals.clear();
        for (Goal goal : goals) {
            mCachedGoals.put(goal.getId(), goal);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Goal> goals) {
        mGoalsLocalDataSource.deleteAllGoals();
        for (Goal goal : goals) {
            mGoalsLocalDataSource.saveGoal(goal);
        }
    }

    @Nullable
    private Goal getGoalWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedGoals == null || mCachedGoals.isEmpty()) {
            return null;
        } else {
            return mCachedGoals.get(id);
        }
    }
}
