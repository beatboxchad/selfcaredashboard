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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.IdlingResource;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.beatboxchad.android.selfcaredashboard.Injection;
import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.ViewModelHolder;
import com.beatboxchad.android.selfcaredashboard.addeditgoal.AddEditGoalActivity;
import com.beatboxchad.android.selfcaredashboard.goaldetail.GoalDetailActivity;
import com.beatboxchad.android.selfcaredashboard.util.ActivityUtils;
import com.beatboxchad.android.selfcaredashboard.util.EspressoIdlingResource;


public class GoalsActivity extends AppCompatActivity implements GoalItemNavigator, GoalsNavigator {

    private DrawerLayout mDrawerLayout;

    public static final String GOALS_VIEWMODEL_TAG = "GOALS_VIEWMODEL_TAG";

    private GoalsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_act);

        setupToolbar();

        setupNavigationDrawer();

        GoalsFragment goalsFragment = findOrCreateViewFragment();

        mViewModel = findOrCreateViewModel();
        mViewModel.setNavigator(this);

        // Link View and ViewModel
        goalsFragment.setViewModel(mViewModel);
    }

    @Override
    protected void onDestroy() {
        mViewModel.onActivityDestroyed();
        super.onDestroy();
    }

    private GoalsViewModel findOrCreateViewModel() {
        // In a configuration change we might have a ViewModel present. It's retained using the
        // Fragment Manager.
        @SuppressWarnings("unchecked")
        ViewModelHolder<GoalsViewModel> retainedViewModel =
                (ViewModelHolder<GoalsViewModel>) getSupportFragmentManager()
                        .findFragmentByTag(GOALS_VIEWMODEL_TAG);

        if (retainedViewModel != null && retainedViewModel.getViewmodel() != null) {
            // If the model was retained, return it.
            return retainedViewModel.getViewmodel();
        } else {
            // There is no ViewModel yet, create it.
            GoalsViewModel viewModel = new GoalsViewModel(
                    Injection.provideGoalsRepository(getApplicationContext()),
                    getApplicationContext());
            // and bind it to this Activity's lifecycle using the Fragment Manager.
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    ViewModelHolder.createContainer(viewModel),
                    GOALS_VIEWMODEL_TAG);
            return viewModel;
        }
    }

    @NonNull
    private GoalsFragment findOrCreateViewFragment() {
        GoalsFragment goalsFragment =
                (GoalsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (goalsFragment == null) {
            // Create the fragment
            goalsFragment = GoalsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), goalsFragment, R.id.contentFrame);
        }
        return goalsFragment;
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.list_navigation_menu_item:
                                // Do nothing, we're already on that screen
                                break;
                            case R.id.statistics_navigation_menu_item:
//                                Intent intent =
//                                        new Intent(GoalsActivity.this, StatisticsActivity.class);
//                                startActivity(intent);
                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode);
    }

    @Override
    public void openGoalDetails(String goalId) {
        Intent intent = new Intent(this, GoalDetailActivity.class);
        intent.putExtra(GoalDetailActivity.EXTRA_GOAL_ID, goalId);
        startActivityForResult(intent, AddEditGoalActivity.REQUEST_CODE);

    }

    @Override
    public void addNewGoal() {
        Intent intent = new Intent(this, AddEditGoalActivity.class);
        startActivityForResult(intent, AddEditGoalActivity.REQUEST_CODE);
    }
}
