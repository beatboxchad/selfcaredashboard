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

import android.databinding.BindingAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.beatboxchad.android.selfcaredashboard.GoalViewModel;
import com.beatboxchad.android.selfcaredashboard.data.Goal;

import java.util.List;

/**
 * Contains {@link BindingAdapter}s for the {@link Goal} list.
 */
public class GoalsListBindings {

    @SuppressWarnings("unchecked")
    @BindingAdapter("app:items")
    public static void setItems(ListView listView, List<Goal> items) {
        GoalsFragment.GoalsAdapter adapter = (GoalsFragment.GoalsAdapter) listView.getAdapter();
        if (adapter != null)
        {
            adapter.replaceData(items);
        }
    }

    @BindingAdapter("android:backgroundColor")
    public static void customGoalColor(TextView textView, int color) {
        textView.setBackgroundColor(color);
    }
}