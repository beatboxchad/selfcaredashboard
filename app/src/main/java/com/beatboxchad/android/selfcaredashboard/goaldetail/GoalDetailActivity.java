/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.beatboxchad.android.selfcaredashboard.Injection;
import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.ViewModelHolder;
import com.beatboxchad.android.selfcaredashboard.addeditgoal.AddEditGoalActivity;
import com.beatboxchad.android.selfcaredashboard.addeditgoal.AddEditGoalFragment;
import com.beatboxchad.android.selfcaredashboard.util.ActivityUtils;

import static com.beatboxchad.android.selfcaredashboard.addeditgoal.AddEditGoalActivity.ADD_EDIT_RESULT_OK;
import static com.beatboxchad.android.selfcaredashboard.goaldetail.GoalDetailFragment.REQUEST_EDIT_GOAL;

/**
 * Displays goal details screen.
 */
public class GoalDetailActivity extends AppCompatActivity implements GoalDetailNavigator {

    public static final String EXTRA_GOAL_ID = "GOAL_ID";

    public static final String GOALDETAIL_VIEWMODEL_TAG = "GOALDETAIL_VIEWMODEL_TAG";

    public static final int DELETE_RESULT_OK = RESULT_FIRST_USER + 2;

    public static final int EDIT_RESULT_OK = RESULT_FIRST_USER + 3;

    private GoalDetailViewModel mGoalViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.goaldetail_act);

        setupToolbar();

        GoalDetailFragment goalDetailFragment = findOrCreateViewFragment();

        mGoalViewModel = findOrCreateViewModel();
        mGoalViewModel.setNavigator(this);

        // Link View and ViewModel
        goalDetailFragment.setViewModel(mGoalViewModel);
    }

    @Override
    protected void onDestroy() {
        mGoalViewModel.onActivityDestroyed();
        super.onDestroy();
    }

    @NonNull
    private GoalDetailViewModel findOrCreateViewModel() {
        // In a configuration change we might have a ViewModel present. It's retained using the
        // Fragment Manager.
        @SuppressWarnings("unchecked")
        ViewModelHolder<GoalDetailViewModel> retainedViewModel =
                (ViewModelHolder<GoalDetailViewModel>) getSupportFragmentManager()
                        .findFragmentByTag(GOALDETAIL_VIEWMODEL_TAG);

        if (retainedViewModel != null && retainedViewModel.getViewmodel() != null) {
            // If the model was retained, return it.
            return retainedViewModel.getViewmodel();
        } else {
            // There is no ViewModel yet, create it.
            GoalDetailViewModel viewModel = new GoalDetailViewModel(
                    getApplicationContext(),
                    Injection.provideGoalsRepository(getApplicationContext()));

            // and bind it to this Activity's lifecycle using the Fragment Manager.
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    ViewModelHolder.createContainer(viewModel),
                    GOALDETAIL_VIEWMODEL_TAG);
            return viewModel;
        }
    }

    @NonNull
    private GoalDetailFragment findOrCreateViewFragment() {
        // Get the requested goal id
        String goalId = getIntent().getStringExtra(EXTRA_GOAL_ID);

        GoalDetailFragment goalDetailFragment = (GoalDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (goalDetailFragment == null) {
            goalDetailFragment = GoalDetailFragment.newInstance(goalId);

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    goalDetailFragment, R.id.contentFrame);
        }
        return goalDetailFragment;
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_GOAL) {
            // If the goal was edited successfully, go back to the list.
            if (resultCode == ADD_EDIT_RESULT_OK) {
                // If the result comes from the add/edit screen, it's an edit.
                setResult(EDIT_RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onGoalDeleted() {
        setResult(DELETE_RESULT_OK);
        // If the goal was deleted successfully, go back to the list.
        finish();
    }

    @Override
    public void onStartEditGoal() {
        String goalId = getIntent().getStringExtra(EXTRA_GOAL_ID);
        Intent intent = new Intent(this, AddEditGoalActivity.class);
        intent.putExtra(AddEditGoalFragment.ARGUMENT_EDIT_GOAL_ID, goalId);
        startActivityForResult(intent, REQUEST_EDIT_GOAL);
    }
}
