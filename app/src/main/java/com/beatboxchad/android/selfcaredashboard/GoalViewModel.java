package com.beatboxchad.android.selfcaredashboard;

import android.app.Activity;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Color;

import java.sql.Date;

public class GoalViewModel extends BaseObservable {
    private Goal mGoal;
    private Activity mActivity;
    private int mGoalColor;

    public GoalViewModel(Activity activity) {
        mActivity = activity;
    }

    @Bindable
    public String getTitle() {
        return mGoal.getTitle();
    }

    public void setTitle(String title) {
        if (mGoal.getTitle() != title) {
            mGoal.setTitle(title);
            notifyPropertyChanged(BR.title);
        }
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public Date getTouched() {
        return mGoal.getTouched();
    }

    public void setTouched(Date touched) {
        mGoal.setTouched(touched);
        notifyPropertyChanged(BR.touched);
    }

    @Bindable
    public int getInterval() {
        return mGoal.getInterval();
    }

    public void setInterval(int interval) {
        mGoal.setInterval(interval);
        notifyPropertyChanged(BR.interval);
    }

    public Goal getGoal() {
        return mGoal;
    }

    public void setGoal(Goal goal) {
        mGoal = goal;
        notifyChange();
    }

    private void calcColor() {
        long diff = new Date(System.currentTimeMillis()).getTime() - mGoal.getTouched().getTime();
        float diffInDays = diff / 1000 / 60 / 60 / 24;
        float percent = (diffInDays / mGoal.getInterval());
        float hue = mGoal.isPolarity() ? 120 * percent : 120 - (120 * percent);
        mGoalColor = Color.HSVToColor(new float[]{hue, 1, 1});
    }

    @Bindable
    public int getColor() {
        calcColor();
        notifyPropertyChanged(BR.color);
        return mGoalColor;
    }

    //TODO not sure if this belongs here

    public void editGoal() {
        Intent intent = GoalPagerActivity.newIntent(mActivity, mGoal.getId());
        mActivity.startActivity(intent);
    }
}