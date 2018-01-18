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

import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.databinding.GoaldetailFragBinding;
import com.beatboxchad.android.selfcaredashboard.util.SnackbarUtils;


/**
 * Main UI for the goal detail screen.
 */
public class GoalDetailFragment extends Fragment {

    public static final String ARGUMENT_GOAL_ID = "GOAL_ID";

    public static final int REQUEST_EDIT_GOAL = 1;

    private GoalDetailViewModel mViewModel;
    private Observable.OnPropertyChangedCallback mSnackbarCallback;

    public static GoalDetailFragment newInstance(String goalId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_GOAL_ID, goalId);
        GoalDetailFragment fragment = new GoalDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    public void setViewModel(GoalDetailViewModel goalViewModel) {
        mViewModel = goalViewModel;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupFab();

        setupSnackbar();
    }

    @Override
    public void onDestroy() {
        if (mSnackbarCallback != null) {
            mViewModel.snackbarText.removeOnPropertyChangedCallback(mSnackbarCallback);
        }
        super.onDestroy();
    }

    private void setupSnackbar() {
        mSnackbarCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SnackbarUtils.showSnackbar(getView(), mViewModel.getSnackbarText());
            }
        };
        mViewModel.snackbarText.addOnPropertyChangedCallback(mSnackbarCallback);
    }

    private void setupFab() {
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_goal);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.startEditGoal();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.start(getArguments().getString(ARGUMENT_GOAL_ID));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.goaldetail_frag, container, false);

        GoaldetailFragBinding viewDataBinding = GoaldetailFragBinding.bind(view);
        viewDataBinding.setViewmodel(mViewModel);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mViewModel.deleteGoal();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.goaldetail_fragment_menu, menu);
    }
}
