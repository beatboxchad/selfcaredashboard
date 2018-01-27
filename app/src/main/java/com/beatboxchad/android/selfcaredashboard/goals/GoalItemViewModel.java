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
import android.databinding.Bindable;
import android.databinding.ObservableField;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.View;

import com.beatboxchad.android.selfcaredashboard.GoalViewModel;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;

import java.lang.ref.WeakReference;
import java.sql.Date;


/**
 * Listens to user actions from the list item in ({@link GoalsFragment}) and redirects them to the
 * Fragment's actions listener.
 */
public class GoalItemViewModel extends GoalViewModel {


    public final ObservableField<Integer> mColor = new ObservableField<>();

    // This navigator is s wrapped in a WeakReference to avoid leaks because it has references to an
    // activity. There's no straightforward way to clear it for each item in a list adapter.
    @Nullable
    private WeakReference<GoalItemNavigator> mNavigator;

    public GoalItemViewModel(Context context, GoalsRepository goalsRepository) {
        super(context, goalsRepository);
    }

    public void setNavigator(GoalItemNavigator navigator) {
        mNavigator = new WeakReference<>(navigator);
    }

    /**
     * Called by the Data Binding library when the row is clicked.
     */
    public void goalClicked() {
        String goalId = getGoalId();
        if (goalId == null) {
            // Click happened before goal was loaded, no-op.
            return;
        }
        if (mNavigator != null && mNavigator.get() != null) {
            mNavigator.get().openGoalDetails(goalId);
        }
    }

    public boolean goalLongClicked(View view) {
        touchGoal();
        calcColor();
        return true;
    }

    private void calcColor() {
            long diff = new Date(System.currentTimeMillis()).getTime() - mTouched.get();
            float diffInDays = diff / 1000 / 60 / 60 / 24;
            float percent = (diffInDays / mInterval.get());
            float hue = mPolarity.get() ? 120 - (120 * percent) : 120 * percent;
            mColor.set(Color.HSVToColor(new float[]{hue, 1, 1}));
            notifyChange();
    }

    @Bindable
    public int getColor() {
        calcColor();
        return mColor.get();
    }
}
