package com.beatboxchad.android.selfcaredashboard;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beatboxchad.android.selfcaredashboard.databinding.FragmentGoalBinding;

import java.util.UUID;

public class GoalFragment extends Fragment {

    private static final String ARG_GOAL_ID = "goal_id";

    private Goal mGoal;

    public static GoalFragment newInstance(UUID goalId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_GOAL_ID, goalId);

        GoalFragment fragment = new GoalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID goalId = (UUID) getArguments().getSerializable(ARG_GOAL_ID);
        mGoal = GoalList.get(getActivity()).getGoal(goalId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentGoalBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_goal, container, false);

        GoalViewModel viewModel = new GoalViewModel(this.getActivity());
        viewModel.setGoal(mGoal);
        binding.setViewModel(viewModel);
        return binding.getRoot();
    }
}