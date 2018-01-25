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

package com.beatboxchad.android.selfcaredashboard.data.source.remote;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the data source that adds a latency simulating network.
 */
public class GoalsRemoteDataSource implements GoalsDataSource {

    private static GoalsRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 2000;

    private final static Map<String, Goal> GOALS_SERVICE_DATA;

    static {
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        long A_WEEK = System.currentTimeMillis() - (7 * DAY_IN_MS);
        long A_DAY = System.currentTimeMillis() - (1 * DAY_IN_MS);
        long FOUR_DAYS = System.currentTimeMillis() - (4 * DAY_IN_MS);
        long A_MONTH = System.currentTimeMillis() - (30 * DAY_IN_MS);


        GOALS_SERVICE_DATA = new LinkedHashMap<>(2);
        addGoal(UUID.randomUUID().toString(), "eat a bucket of candy", false, 7, A_MONTH, false);
        addGoal(UUID.randomUUID().toString(), "go for a run", true, 4, A_DAY, false);
        addGoal(UUID.randomUUID().toString(), "go for a bender", false, 30, A_MONTH - A_WEEK, true);
        addGoal(UUID.randomUUID().toString(), "kick a sack of adorable puppies", false, 7, A_MONTH, false);
        addGoal(UUID.randomUUID().toString(), "take a shower", true, 2, A_DAY, false);
    }

    public static GoalsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GoalsRemoteDataSource();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private GoalsRemoteDataSource() {}

    private static void addGoal(String id,
                                String title,
                                Boolean polarity,
                                Integer interval,
                                Long touched,
                                Boolean archived) {
        Goal newGoal = new Goal(id, title, polarity, interval, touched, archived);
        GOALS_SERVICE_DATA.put(newGoal.getId(), newGoal);
    }

    /**
     * Note: {@link LoadGoalsCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getGoals(final @NonNull LoadGoalsCallback callback) {
        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onGoalsLoaded(Lists.newArrayList(GOALS_SERVICE_DATA.values()));
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    /**
     * Note: {@link GetGoalCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getGoal(@NonNull String goalId, final @NonNull GetGoalCallback callback) {
        final Goal goal = GOALS_SERVICE_DATA.get(goalId);

        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onGoalLoaded(goal);
            }
        }, SERVICE_LATENCY_IN_MILLIS);
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
        // Not required for the remote data source because the {@link GoalsRepository} handles
        // converting from a {@code goalId} to a {@link goal} using its cached data.
    }

    @Override
    public void activateGoal(@NonNull Goal goal) {
        Goal activeGoal = new Goal(goal.getId(),
                goal.getTitle(),
                goal.getPolarity(),
                goal.getInterval(),
                goal.getTouched(),
                true);
        GOALS_SERVICE_DATA.put(goal.getId(), activeGoal);
    }

    @Override
    public void activateGoal(@NonNull String goalId) {
        // Not required for the remote data source because the {@link GoalsRepository} handles
        // converting from a {@code goalId} to a {@link goal} using its cached data.
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

    @Override
    public void refreshGoals() {
        // Not required because the {@link GoalsRepository} handles the logic of refreshing the
        // goals from all the available data sources.
    }

    @Override
    public void deleteAllGoals() {
        GOALS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteGoal(@NonNull String goalId) {
        GOALS_SERVICE_DATA.remove(goalId);
    }
}
