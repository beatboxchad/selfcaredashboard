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

package com.beatboxchad.android.selfcaredashboard.goals;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.graphics.drawable.Drawable;

import com.beatboxchad.android.selfcaredashboard.BR;
import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.addeditgoal.AddEditGoalActivity;
import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;
import com.beatboxchad.android.selfcaredashboard.goaldetail.GoalDetailActivity;
import com.beatboxchad.android.selfcaredashboard.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Exposes the data to be used in the goal list screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class GoalsViewModel extends BaseObservable {

    // These observable fields will update Views automatically
    public final ObservableList<Goal> items = new ObservableArrayList<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableField<String> currentFilteringLabel = new ObservableField<>();

    public final ObservableField<String> noGoalsLabel = new ObservableField<>();

    public final ObservableField<Drawable> noGoalIconRes = new ObservableField<>();

    public final ObservableBoolean goalsAddViewVisible = new ObservableBoolean();

    final ObservableField<String> snackbarText = new ObservableField<>();

    private GoalsFilterType mCurrentFiltering = GoalsFilterType.ALL_GOALS;

    private final GoalsRepository mGoalsRepository;

    private final ObservableBoolean mIsDataLoadingError = new ObservableBoolean(false);

    private Context mContext; // To avoid leaks, this must be an Application Context.

    private GoalsNavigator mNavigator;

    public GoalsViewModel(
            GoalsRepository repository,
            Context context) {
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mGoalsRepository = repository;

        // Set initial state
        setFiltering(GoalsFilterType.ALL_GOALS);
    }

    void setNavigator(GoalsNavigator navigator) {
        mNavigator = navigator;
    }

    void onActivityDestroyed() {
        // Clear references to avoid potential memory leaks.
        mNavigator = null;
    }

    public void start() {
        loadGoals(false);
    }

    @Bindable
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void loadGoals(boolean forceUpdate) {
        loadGoals(forceUpdate, true);
    }

    /**
     * Sets the current goal filtering type.
     *
     * @param requestType Can be {@link GoalsFilterType#ALL_GOALS},
     *                    {@link GoalsFilterType#COMPLETED_GOALS}, or
     *                    {@link GoalsFilterType#ACTIVE_GOALS}
     */
    public void setFiltering(GoalsFilterType requestType) {
        mCurrentFiltering = requestType;

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        switch (requestType) {
            case ALL_GOALS:
                currentFilteringLabel.set(mContext.getString(R.string.label_all));
                noGoalsLabel.set(mContext.getResources().getString(R.string.no_goals_all));
                noGoalIconRes.set(mContext.getResources().getDrawable(
                        R.drawable.ic_assignment_turned_in_24dp));
                goalsAddViewVisible.set(true);
                break;
            case ACTIVE_GOALS:
                currentFilteringLabel.set(mContext.getString(R.string.label_active));
                noGoalsLabel.set(mContext.getResources().getString(R.string.no_goals_active));
                noGoalIconRes.set(mContext.getResources().getDrawable(
                        R.drawable.ic_check_circle_24dp));
                goalsAddViewVisible.set(false);
                break;
            case COMPLETED_GOALS:
                currentFilteringLabel.set(mContext.getString(R.string.label_completed));
                noGoalsLabel.set(mContext.getResources().getString(R.string.no_goals_completed));
                noGoalIconRes.set(mContext.getResources().getDrawable(
                        R.drawable.ic_verified_user_24dp));
                goalsAddViewVisible.set(false);
                break;
        }
    }

    public void clearCompletedGoals() {
        mGoalsRepository.clearCompletedGoals();
        snackbarText.set(mContext.getString(R.string.completed_goals_cleared));
        loadGoals(false, false);
    }

    public String getSnackbarText() {
        return snackbarText.get();
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    public void addNewGoal() {
        if (mNavigator != null) {
            mNavigator.addNewGoal();
        }
    }

    void handleActivityResult(int requestCode, int resultCode) {
        if (AddEditGoalActivity.REQUEST_CODE == requestCode) {
            switch (resultCode) {
                case GoalDetailActivity.EDIT_RESULT_OK:
                    snackbarText.set(
                            mContext.getString(R.string.successfully_saved_goal_message));
                    break;
                case AddEditGoalActivity.ADD_EDIT_RESULT_OK:
                    snackbarText.set(
                            mContext.getString(R.string.successfully_added_goal_message));
                    break;
                case GoalDetailActivity.DELETE_RESULT_OK:
                    snackbarText.set(
                            mContext.getString(R.string.successfully_deleted_goal_message));
                    break;
            }
        }
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link GoalsDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadGoals(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            dataLoading.set(true);
        }
        if (forceUpdate) {

            mGoalsRepository.refreshGoals();
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mGoalsRepository.getGoals(new GoalsDataSource.LoadGoalsCallback() {
            @Override
            public void onGoalsLoaded(List<Goal> goals) {
                List<Goal> goalsToShow = new ArrayList<Goal>();

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                // We filter the goals based on the requestType
                for (Goal goal : goals) {
                    switch (mCurrentFiltering) {
                        case ALL_GOALS:
                            goalsToShow.add(goal);
                            break;
                        case ACTIVE_GOALS:
                            if (goal.isActive()) {
                                goalsToShow.add(goal);
                            }
                            break;
                        case COMPLETED_GOALS:
                            if (goal.isCompleted()) {
                                goalsToShow.add(goal);
                            }
                            break;
                        default:
                            goalsToShow.add(goal);
                            break;
                    }
                }
                if (showLoadingUI) {
                    dataLoading.set(false);
                }
                mIsDataLoadingError.set(false);

                items.clear();
                items.addAll(goalsToShow);
                notifyPropertyChanged(BR.empty); // It's a @Bindable so update manually
            }

            @Override
            public void onDataNotAvailable() {
                mIsDataLoadingError.set(true);
            }
        });
    }

}
