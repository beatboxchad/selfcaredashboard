package com.beatboxchad.android.selfcaredashboard;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by chad on 12/22/17.
 */

public class GoalList {
    private static GoalList sGoalList;

    private List<Goal> mGoals;

    private GoalList(Context context) {
        mGoals = new ArrayList<>();
        // load the goals from storage (eventually, TODO replace these stand-ins)
        for (int i = 0; i < 5; i++) {
            Goal goal = new Goal();
            goal.setTitle("Goal #" + i);
            goal.setPolarity(i % 2 == 0); // Every other one
            mGoals.add(goal);
        }
    }

    public static GoalList get(Context context) {
        return sGoalList; // I think?
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