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

package com.beatboxchad.android.selfcaredashboard.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeGoalsRemoteDataSource implements GoalsDataSource {

    private static FakeGoalsRemoteDataSource INSTANCE;

    private static final Map<String, Goal> GOALS_SERVICE_DATA = new LinkedHashMap<>();

    // Prevent direct instantiation.
    private FakeGoalsRemoteDataSource() {
    }

    public static FakeGoalsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeGoalsRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public void getGoals(@NonNull LoadGoalsCallback callback) {
        callback.onGoalsLoaded(Lists.newArrayList(GOALS_SERVICE_DATA.values()));
    }

    @Override
    public void getGoal(@NonNull String goalId, @NonNull GetGoalCallback callback) {
        Goal goal = GOALS_SERVICE_DATA.get(goalId);
        callback.onGoalLoaded(goal);
    }

    @Override
    public void saveGoal(@NonNull Goal goal) {
        GOALS_SERVICE_DATA.put(goal.getId(), goal);
    }

    @Override
    public void archiveGoal(@NonNull Goal goal) {
        Goal archivedGoal = new Goal(goal.getId(),
                goal.getTitle(),
                goal.getPolarity(),
                goal.getInterval(),
                goal.getTouched(),
                true);
        GOALS_SERVICE_DATA.put(goal.getId(), archivedGoal);
    }

    @Override
    public void archiveGoal(@NonNull String goalId) {
        // Not required for the remote data source.
    }

    @Override
    public void activateGoal(@NonNull Goal goal) {
        Goal activeGoal = new Goal(goal.getId(),
                goal.getTitle(),
                goal.getPolarity(),
                goal.getInterval(),
                goal.getTouched(),
                false);
        GOALS_SERVICE_DATA.put(goal.getId(), activeGoal);
    }

    @Override
    public void activateGoal(@NonNull String goalId) {
        // Not required for the remote data source.
    }

    @Override
    public void clearArchivedGoals() {
        Iterator<Map.Entry<String, Goal>> it = GOALS_SERVICE_DATA.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Goal> entry = it.next();
            if (entry.getValue().isArchived()) {
                it.remove();
            }
        }
    }

    public void refreshGoals() {
        // Not required because the {@link GoalsRepository} handles the logic of refreshing the
        // goals from all the available data sources.
    }

    @Override
    public void deleteGoal(@NonNull String goalId) {
        GOALS_SERVICE_DATA.remove(goalId);
    }

    @Override
    public void deleteAllGoals() {
        GOALS_SERVICE_DATA.clear();
    }

    @VisibleForTesting
    public void addGoals(Goal... goals) {
        if (goals != null) {
            for (Goal goal : goals) {
                GOALS_SERVICE_DATA.put(goal.getId(), goal);
            }
        }
    }
}
