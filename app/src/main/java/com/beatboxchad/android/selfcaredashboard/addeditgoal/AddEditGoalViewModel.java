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

package com.beatboxchad.android.selfcaredashboard.addeditgoal;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;

import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link android.databinding.BaseObservable} and updates are notified automatically. See
 * {@link com.beatboxchad.android.selfcaredashboard.statistics.StatisticsViewModel} for
 * how to deal with more complex scenarios.
 */
public class AddEditGoalViewModel implements GoalsDataSource.GetGoalCallback {

    public final ObservableField<String> title = new ObservableField<>();

    public final ObservableField<String> description = new ObservableField<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableField<String> snackbarText = new ObservableField<>();

    private final GoalsRepository mGoalsRepository;

    private final Context mContext;  // To avoid leaks, this must be an Application Context.

    @Nullable
    private String mGoalId;

    private boolean mIsNewGoal;

    private boolean mIsDataLoaded = false;

    private AddEditGoalNavigator mAddEditGoalNavigator;

    AddEditGoalViewModel(Context context, GoalsRepository goalsRepository) {
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mGoalsRepository = goalsRepository;
    }

    void onActivityCreated(AddEditGoalNavigator navigator) {
        mAddEditGoalNavigator = navigator;
    }

    void onActivityDestroyed() {
        // Clear references to avoid potential memory leaks.
        mAddEditGoalNavigator = null;
    }

    public void start(String goalId) {
        if (dataLoading.get()) {
            // Already loading, ignore.
            return;
        }
        mGoalId = goalId;
        if (goalId == null) {
            // No need to populate, it's a new goal
            mIsNewGoal = true;
            return;
        }
        if (mIsDataLoaded) {
            // No need to populate, already have data.
            return;
        }
        mIsNewGoal = false;
        dataLoading.set(true);
        mGoalsRepository.getGoal(goalId, this);
    }

    @Override
    public void onGoalLoaded(Goal goal) {
        title.set(goal.getTitle());
        description.set(goal.getDescription());
        dataLoading.set(false);
        mIsDataLoaded = true;

        // Note that there's no need to notify that the values changed because we're using
        // ObservableFields.
    }

    @Override
    public void onDataNotAvailable() {
        dataLoading.set(false);
    }

    // Called when clicking on fab.
    public void saveGoal() {
        if (isNewGoal()) {
            createGoal(title.get(), description.get());
        } else {
            updateGoal(title.get(), description.get());
        }
    }

    @Nullable
    public String getSnackbarText() {
        return snackbarText.get();
    }

    private boolean isNewGoal() {
        return mIsNewGoal;
    }

    private void createGoal(String title, String description) {
        Goal newGoal = new Goal(title, description);
        if (newGoal.isEmpty()) {
            snackbarText.set(mContext.getString(R.string.empty_goal_message));
        } else {
            mGoalsRepository.saveGoal(newGoal);
            navigateOnGoalSaved();
        }
    }

    private void updateGoal(String title, String description) {
        if (isNewGoal()) {
            throw new RuntimeException("updateGoal() was called but goal is new.");
        }
        mGoalsRepository.saveGoal(new Goal(title, description, mGoalId));
        navigateOnGoalSaved(); // After an edit, go back to the list.
    }

    private void navigateOnGoalSaved() {
        if (mAddEditGoalNavigator!= null) {
            mAddEditGoalNavigator.onGoalSaved();
        }
    }
}
