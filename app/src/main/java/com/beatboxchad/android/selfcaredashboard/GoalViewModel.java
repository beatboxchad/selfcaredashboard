package com.beatboxchad.android.selfcaredashboard;

import android.app.Activity;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.List;

public class GoalViewModel extends BaseObservable {
    private Goal mGoal;
    private Activity mActivity;

    public GoalViewModel(Activity activity) {
        mActivity = activity;
    }

    @Bindable
    public String getTitle() {
        return mGoal.getTitle();
    }

    @Bindable
    public String getRenderedDate() {
        return mGoal.getTouched().toString();
    }

    @Bindable
    public boolean isPolarity() {
        return mGoal.isPolarity();
    }

    public Goal getGoal() {
        return mGoal;
    }

    public void setGoal(Goal goal) {
        mGoal = goal;
        List<String> strings;
        notifyChange();
    }

    public void onGoalLongClicked() {
        Intent intent = GoalPagerActivity.newIntent(mActivity, mGoal.getId());
        mActivity.startActivity(intent);
    }
}
