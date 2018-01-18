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

package com.beatboxchad.android.selfcaredashboard;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.support.annotation.Nullable;

import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;


/**
 * Abstract class for View Models that expose a single {@link Goal}.
 */
public abstract class GoalViewModel extends BaseObservable
        implements GoalsDataSource.GetGoalCallback {

    public final ObservableField<String> snackbarText = new ObservableField<>();

    public final ObservableField<String> title = new ObservableField<>();

    public final ObservableField<String> description = new ObservableField<>();

    private final ObservableField<Goal> mGoalObservable = new ObservableField<>();

    private final GoalsRepository mGoalsRepository;

    private final Context mContext;

    private boolean mIsDataLoading;

    public GoalViewModel(Context context, GoalsRepository goalsRepository) {
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mGoalsRepository = goalsRepository;

        // Exposed observables depend on the mGoalObservable observable:
        mGoalObservable.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                Goal goal = mGoalObservable.get();
                if (goal != null) {
                    title.set(goal.getTitle());
                    description.set(goal.getDescription());
                } else {
                    title.set(mContext.getString(R.string.no_data));
                    description.set(mContext.getString(R.string.no_data_description));
                }
            }
        });
    }

    public void start(String goalId) {
        if (goalId != null) {
            mIsDataLoading = true;
            mGoalsRepository.getGoal(goalId, this);
        }
    }

    public void setGoal(Goal goal) {
        mGoalObservable.set(goal);
    }

    // "completed" is two-way bound, so in order to intercept the new value, use a @Bindable
    // annotation and process it in the setter.
    @Bindable
    public boolean getCompleted() {
        Goal goal = mGoalObservable.get();
        return goal != null && goal.isCompleted();
    }

    public void setCompleted(boolean completed) {
        if (mIsDataLoading) {
            return;
        }
        Goal goal = mGoalObservable.get();

        // Notify repository and user
        if (completed) {
            mGoalsRepository.completeGoal(goal);
            snackbarText.set(mContext.getResources().getString(R.string.goal_marked_complete));
        } else {
            mGoalsRepository.activateGoal(goal);
            snackbarText.set(mContext.getResources().getString(R.string.goal_marked_active));
        }
    }

    @Bindable
    public boolean isDataAvailable() {
        return mGoalObservable.get() != null;
    }

    @Bindable
    public boolean isDataLoading() {
        return mIsDataLoading;
    }

    // This could be an observable, but we save a call to Goal.getTitleForList() if not needed.
    @Bindable
    public String getTitleForList() {
        if (mGoalObservable.get() == null) {
            return "No data";
        }
        return mGoalObservable.get().getTitleForList();
    }

    @Override
    public void onGoalLoaded(Goal goal) {
        mGoalObservable.set(goal);
        mIsDataLoading = false;
        notifyChange(); // For the @Bindable properties
    }

    @Override
    public void onDataNotAvailable() {
        mGoalObservable.set(null);
        mIsDataLoading = false;
    }

    public void deleteGoal() {
        if (mGoalObservable.get() != null) {
            mGoalsRepository.deleteGoal(mGoalObservable.get().getId());
        }
    }

    public void onRefresh() {
        if (mGoalObservable.get() != null) {
            start(mGoalObservable.get().getId());
        }
    }

    public String getSnackbarText() {
        return snackbarText.get();
    }

    @Nullable
    protected String getGoalId() {
        return mGoalObservable.get().getId();
    }
}
