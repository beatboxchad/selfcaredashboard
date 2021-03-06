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
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;

import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;

import java.util.UUID;

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link android.databinding.BaseObservable} and updates are notified automatically.
 */
public class AddEditGoalViewModel implements GoalsDataSource.GetGoalCallback {

    public final ObservableField<String> title = new ObservableField<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableField<String> snackbarText = new ObservableField<>();

    public final ObservableField<Integer> interval = new ObservableField<>(1);

    public final ObservableField<Boolean> polarity = new ObservableField<>(false);

    public final ObservableField<Boolean> archived = new ObservableField<>(false);

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

    public int getInterval() {
        return interval.get();
    }

    public void setInterval(int days) {
        interval.set(days);
    }

    public boolean getPolarity() {
        return polarity.get();
    }

    public void setPolarity(boolean gpol) {
        polarity.set(gpol);
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String gtitle) {
        title.set(gtitle);
    }

    @Override
    public void onGoalLoaded(Goal goal) {
        title.set(goal.getTitle());
        interval.set(goal.getInterval());
        polarity.set(goal.getPolarity());
        archived.set(goal.isArchived());
        dataLoading.set(false);
        mIsDataLoaded = true;

        // Note that there's no need to notify that the values changed because we're using
        // ObservableFields.
    }

    public void onIntervalChange(int newv) {
        interval.set(newv);
    }

    @Override
    public void onDataNotAvailable() {
        dataLoading.set(false);
    }

    // Called when clicking on fab.
    public void saveGoal() {
        if (isNewGoal()) {
            createGoal(title.get(), interval.get(), polarity.get());
        } else {
            updateGoal(title.get(), interval.get(), polarity.get());
        }
    }

    @Nullable
    public String getSnackbarText() {
        return snackbarText.get();
    }

    private boolean isNewGoal() {
        return mIsNewGoal;
    }

    private void createGoal(String title, int interval, boolean polarity) {
        Goal newGoal = new Goal.Builder(UUID.randomUUID().toString())
                .setTitle(title)
                .setInterval(interval)
                .setPolarity(polarity)
                .build();
        if (newGoal.isEmpty()) {
            snackbarText.set(mContext.getString(R.string.empty_goal_message));
        } else {
            mGoalsRepository.saveGoal(newGoal);
            navigateOnGoalSaved();
        }
    }

    private void updateGoal(String title, int interval, boolean polarity) {
        if (isNewGoal()) {
            throw new RuntimeException("updateGoal() was called but goal is new.");
        }
        mGoalsRepository.saveGoal(new Goal.Builder(mGoalId)
                .setTitle(title)
                .setInterval(interval)
                .setPolarity(polarity)
                .build());
        navigateOnGoalSaved(); // After an edit, go back to the list.
    }

    private void navigateOnGoalSaved() {
        if (mAddEditGoalNavigator != null) {
            mAddEditGoalNavigator.onGoalSaved();
        }
    }
}