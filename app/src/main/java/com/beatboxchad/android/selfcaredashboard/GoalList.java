package com.beatboxchad.android.selfcaredashboard;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GoalList {
    private static com.beatboxchad.android.selfcaredashboard.GoalList sGoalList;

    private ArrayList<Goal> mGoals;

    public static com.beatboxchad.android.selfcaredashboard.GoalList get(Context context) {
        if (sGoalList == null) {
            sGoalList = new com.beatboxchad.android.selfcaredashboard.GoalList(context);
        }
        return sGoalList;
    }

    private GoalList(Context context) {
        mGoals = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Goal goal = new Goal();
            goal.setPolarity(i % 2 == 0);
            goal.setTitle("Case #" + (i + 1));
            mGoals.add(goal);
        }
    }

    public void addGoal(Goal c) {
        mGoals.add(c);
    }

    public List<Goal> getGoals() {
        return mGoals;
    }

    public Goal getGoal(UUID id) {
        for (Goal goal : mGoals) {
            if (goal.getId().equals(id)) {
                return goal;
            }
        }
        return null;
    }
}
