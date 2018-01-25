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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.beatboxchad.android.selfcaredashboard.Injection;
import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.ViewModelHolder;
import com.beatboxchad.android.selfcaredashboard.util.ActivityUtils;
import com.beatboxchad.android.selfcaredashboard.util.EspressoIdlingResource;

/**
 * Displays an add or edit goal screen.
 */
public class AddEditGoalActivity extends AppCompatActivity implements AddEditGoalNavigator {

    public static final int REQUEST_CODE = 1;

    public static final int ADD_EDIT_RESULT_OK = RESULT_FIRST_USER + 1;

    public static final String ADD_EDIT_VIEWMODEL_TAG = "ADD_EDIT_VIEWMODEL_TAG";

    private AddEditGoalViewModel mViewModel;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }

    @Override
    public void onGoalSaved() {
        setResult(ADD_EDIT_RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addgoal_act);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        AddEditGoalFragment addEditGoalFragment = findOrCreateViewFragment();

        mViewModel = findOrCreateViewModel();

        // Link View and ViewModel
        addEditGoalFragment.setViewModel(mViewModel);

        mViewModel.onActivityCreated(this);
    }

    @Override
    protected void onDestroy() {
        mViewModel.onActivityDestroyed();
        super.onDestroy();
    }

    @NonNull
    private AddEditGoalFragment findOrCreateViewFragment() {
        // View Fragment
        AddEditGoalFragment addEditGoalFragment = (AddEditGoalFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (addEditGoalFragment == null) {
            addEditGoalFragment = AddEditGoalFragment.newInstance();

            // Send the goal ID to the fragment
            Bundle bundle = new Bundle();
            bundle.putString(AddEditGoalFragment.ARGUMENT_EDIT_GOAL_ID,
                    getIntent().getStringExtra(AddEditGoalFragment.ARGUMENT_EDIT_GOAL_ID));
            addEditGoalFragment.setArguments(bundle);

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    addEditGoalFragment, R.id.contentFrame);
        }
        return addEditGoalFragment;
    }

    private AddEditGoalViewModel findOrCreateViewModel() {
        // In a configuration change we might have a ViewModel present. It's retained using the
        // Fragment Manager.
        @SuppressWarnings("unchecked")
        ViewModelHolder<AddEditGoalViewModel> retainedViewModel =
                (ViewModelHolder<AddEditGoalViewModel>) getSupportFragmentManager()
                        .findFragmentByTag(ADD_EDIT_VIEWMODEL_TAG);

        if (retainedViewModel != null && retainedViewModel.getViewmodel() != null) {
            // If the model was retained, return it.
            return retainedViewModel.getViewmodel();
        } else {
            // There is no ViewModel yet, create it.
            AddEditGoalViewModel viewModel = new AddEditGoalViewModel(
                    getApplicationContext(),
                    Injection.provideGoalsRepository(getApplicationContext()));

            // and bind it to this Activity's lifecycle using the Fragment Manager.
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    ViewModelHolder.createContainer(viewModel),
                    ADD_EDIT_VIEWMODEL_TAG);
            return viewModel;
        }
    }
}