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

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.beatboxchad.android.selfcaredashboard.Injection;
import com.beatboxchad.android.selfcaredashboard.R;
import com.beatboxchad.android.selfcaredashboard.ScrollChildSwipeRefreshLayout;
import com.beatboxchad.android.selfcaredashboard.data.Goal;
import com.beatboxchad.android.selfcaredashboard.data.source.GoalsRepository;
import com.beatboxchad.android.selfcaredashboard.databinding.GoalItemBinding;
import com.beatboxchad.android.selfcaredashboard.databinding.GoalsFragBinding;
import com.beatboxchad.android.selfcaredashboard.util.SnackbarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Display a grid of {@link Goal}s. User can choose to view all, active or archived goals.
 */
public class GoalsFragment extends Fragment {

    private GoalsViewModel mGoalsViewModel;

    private GoalsFragBinding mGoalsFragBinding;

    private GoalsAdapter mListAdapter;

    private Observable.OnPropertyChangedCallback mSnackbarCallback;

    public GoalsFragment() {
        // Requires empty public constructor
    }

    public static GoalsFragment newInstance() {
        return new GoalsFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoalsViewModel.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGoalsFragBinding = GoalsFragBinding.inflate(inflater, container, false);

        mGoalsFragBinding.setView(this);

        mGoalsFragBinding.setViewmodel(mGoalsViewModel);

        setHasOptionsMenu(true);

        View root = mGoalsFragBinding.getRoot();

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mGoalsViewModel.clearArchivedGoals();
                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mGoalsViewModel.loadGoals(true);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.goals_fragment_menu, menu);
    }

    public void setViewModel(GoalsViewModel viewModel) {
        mGoalsViewModel = viewModel;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSnackbar();

        setupFab();

        setupListAdapter();

        setupRefreshLayout();
    }

    @Override
    public void onDestroy() {
        mListAdapter.onDestroy();
        if (mSnackbarCallback != null) {
            mGoalsViewModel.snackbarText.removeOnPropertyChangedCallback(mSnackbarCallback);
        }
        super.onDestroy();
    }

    private void setupSnackbar() {
        mSnackbarCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SnackbarUtils.showSnackbar(getView(), mGoalsViewModel.getSnackbarText());
            }
        };
        mGoalsViewModel.snackbarText.addOnPropertyChangedCallback(mSnackbarCallback);
    }

    private void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_goals, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:
                        mGoalsViewModel.setFiltering(GoalsFilterType.ACTIVE_GOALS);
                        break;
                    case R.id.archived:
                        mGoalsViewModel.setFiltering(GoalsFilterType.ARCHIVED_GOALS);
                        break;
                    default:
                        mGoalsViewModel.setFiltering(GoalsFilterType.ALL_GOALS);
                        break;
                }
                mGoalsViewModel.loadGoals(false);
                return true;
            }
        });

        popup.show();
    }

    private void setupFab() {
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_goal);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoalsViewModel.addNewGoal();
            }
        });
    }

    private void setupListAdapter() {
        ListView listView =  mGoalsFragBinding.goalsList;

        mListAdapter = new GoalsAdapter(
                new ArrayList<Goal>(0),
                (GoalsActivity) getActivity(),
                Injection.provideGoalsRepository(getContext().getApplicationContext()),
                mGoalsViewModel);
        listView.setAdapter(mListAdapter);
    }

    private void setupRefreshLayout() {
        ListView listView =  mGoalsFragBinding.goalsList;
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = mGoalsFragBinding.refreshLayout;
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);
    }

    public static class GoalsAdapter extends BaseAdapter {

        @Nullable private GoalItemNavigator mGoalItemNavigator;

        private final GoalsViewModel mGoalsViewModel;

        private List<Goal> mGoals;

        private GoalsRepository mGoalsRepository;

        public GoalsAdapter(List<Goal> goals, GoalsActivity goalItemNavigator,
                            GoalsRepository goalsRepository,
                            GoalsViewModel goalsViewModel) {
            mGoalItemNavigator = goalItemNavigator;
            mGoalsRepository = goalsRepository;
            mGoalsViewModel = goalsViewModel;
            setList(goals);

        }

        public void onDestroy() {
            mGoalItemNavigator = null;
        }

        public void replaceData(List<Goal> goals) {
            setList(goals);
        }

        @Override
        public int getCount() {
            return mGoals != null ? mGoals.size() : 0;
        }

        @Override
        public Goal getItem(int i) {
            return mGoals.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Goal goal = getItem(i);
            GoalItemBinding binding;
            if (view == null) {
                // Inflate
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

                // Create the binding
                binding = GoalItemBinding.inflate(inflater, viewGroup, false);
            } else {
                // Recycling view
                binding = DataBindingUtil.getBinding(view);
            }

            final GoalItemViewModel viewmodel = new GoalItemViewModel(
                    viewGroup.getContext().getApplicationContext(),
                    mGoalsRepository
            );

            viewmodel.setNavigator(mGoalItemNavigator);
            viewmodel.calcColor();

            binding.setViewmodel(viewmodel);
            // To save on PropertyChangedCallbacks, wire the item's snackbar text observable to the
            // fragment's.
            viewmodel.snackbarText.addOnPropertyChangedCallback(
                    new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable observable, int i) {
                    mGoalsViewModel.snackbarText.set(viewmodel.getSnackbarText());
                }
            });
            viewmodel.setGoal(goal);

            return binding.getRoot();
        }


        private void setList(List<Goal> goals) {
            mGoals = goals;
            notifyDataSetChanged();
        }
    }
}
