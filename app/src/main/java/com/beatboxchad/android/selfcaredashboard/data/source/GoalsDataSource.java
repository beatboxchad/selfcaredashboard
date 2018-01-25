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

import android.support.annotation.NonNull;

import com.beatboxchad.android.selfcaredashboard.data.Goal;

import java.util.List;

/**
 * Main entry point for accessing goals data.
 */
public interface GoalsDataSource {

    interface LoadGoalsCallback {

        void onGoalsLoaded(List<Goal> goals);

        void onDataNotAvailable();
    }

    interface GetGoalCallback {

        void onGoalLoaded(Goal goal);

        void onDataNotAvailable();
    }

    void getGoals(@NonNull LoadGoalsCallback callback);

    void getGoal(@NonNull String goalId, @NonNull GetGoalCallback callback);

    void saveGoal(@NonNull Goal goal);

    void archiveGoal(@NonNull Goal goal);

    void archiveGoal(@NonNull String goalId);

    void activateGoal(@NonNull Goal goal);

    void activateGoal(@NonNull String goalId);

    void clearArchivedGoals();

    void refreshGoals();

    void deleteAllGoals();

    void deleteGoal(@NonNull String goalId);
}
