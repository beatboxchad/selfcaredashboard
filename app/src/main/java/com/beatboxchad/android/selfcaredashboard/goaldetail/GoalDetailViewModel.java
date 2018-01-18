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
import android.support.annotation.Nullable;

import com.beatboxchad.android.selfcaredashboard.GoalViewModel;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;
import com.beatboxchad.android.selfcaredashboard.goals.GoalsFragment;


/**
 * Listens to user actions from the list item in ({@link GoalsFragment}) and redirects them to the
 * Fragment's actions listener.
 */
public class GoalDetailViewModel extends GoalViewModel {

    @Nullable
    private GoalDetailNavigator mGoalDetailNavigator;

    public GoalDetailViewModel(Context context, GoalsRepository goalsRepository) {
        super(context, goalsRepository);
    }

    public void setNavigator(GoalDetailNavigator goalDetailNavigator) {
        mGoalDetailNavigator = goalDetailNavigator;
    }

    public void onActivityDestroyed() {
        // Clear references to avoid potential memory leaks.
        mGoalDetailNavigator = null;
    }

    /**
     * Can be called by the Data Binding Library or the delete menu item.
     */
    public void deleteGoal() {
        super.deleteGoal();
        if (mGoalDetailNavigator != null) {
            mGoalDetailNavigator.onGoalDeleted();
        }
    }

    public void startEditGoal() {
        if (mGoalDetailNavigator != null) {
            mGoalDetailNavigator.onStartEditGoal();
        }
    }
}
