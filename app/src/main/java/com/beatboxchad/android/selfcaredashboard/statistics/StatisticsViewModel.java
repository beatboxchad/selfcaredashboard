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

package com.beatboxchad.android.selfcaredashboard.statistics;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.VisibleForTesting;

import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsDataSource;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;
import com.beatboxchad.android.selfcaredashboard.util.EspressoIdlingResource;

import java.util.List;

/**
 * Exposes the data to be used in the statistics screen.
 * <p>
 * This ViewModel uses both {@link ObservableField}s ({@link ObservableBoolean}s in this case) and
 * {@link Bindable} getters. The values in {@link ObservableField}s are used directly in the layout,
 * whereas the {@link Bindable} getters allow us to add some logic to it. This is
 * preferable to having logic in the XML layout.
 */
public class StatisticsViewModel extends BaseObservable {

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    final ObservableBoolean error = new ObservableBoolean(false);

    @VisibleForTesting
    int mNumberOfActiveGoals = 0;

    @VisibleForTesting
    int mNumberOfCompletedGoals = 0;

    private Context mContext;

    private final GoalsRepository mGoalsRepository;

    public StatisticsViewModel(Context context, GoalsRepository goalsRepository) {
        mContext = context;
        mGoalsRepository = goalsRepository;
    }

    public void start() {
        loadStatistics();
    }

    public void loadStatistics() {
        dataLoading.set(true);

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mGoalsRepository.getGoals(new GoalsDataSource.LoadGoalsCallback() {
            @Override
            public void onGoalsLoaded(List<Goal> goals) {

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                computeStats(goals);
            }

            @Override
            public void onDataNotAvailable() {
                error.set(true);
            }
        });
    }
    /**
     * Returns a String showing the number of active goals.
     */
    @Bindable
    public String getNumberOfActiveGoals() {
        return mContext.getString(R.string.statistics_active_goals, mNumberOfActiveGoals);
    }

    /**
     * Returns a String showing the number of completed goals.
     */
    @Bindable
    public String getNumberOfCompletedGoals() {
        return mContext.getString(R.string.statistics_completed_goals, mNumberOfCompletedGoals);
    }

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    @Bindable
    public boolean isEmpty() {
        return mNumberOfActiveGoals + mNumberOfCompletedGoals == 0;
    }

    /**
     * Called when new data is ready.
     */
    private void computeStats(List<Goal> goals) {
        int completed = 0;
        int active = 0;

        for (Goal goal : goals) {
            if (goal.isCompleted()) {
                completed += 1;
            } else {
                active += 1;
            }
        }
        mNumberOfActiveGoals = active;
        mNumberOfCompletedGoals = completed;

        // There are multiple @Bindable fields in this ViewModel, calling notifyChange() will
        // update all the UI elements that depend on them.
        notifyChange();

        // To update just one of them and avoid unnecessary UI updates,
        // use notifyPropertyChanged(BR.field)

        // Observable fields don't need to be notified. set() will trigger an update.
        dataLoading.set(false);
        error.set(false);
    }
}
