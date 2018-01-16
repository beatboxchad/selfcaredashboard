package com.beatboxchad.android.selfcaredashboard;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Color;

import java.sql.Date;

public class GoalViewModel extends BaseObservable {
    private Goal mGoal;
    private int mGoalColor;

    public GoalViewModel() {

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

    // FIXME this *might* not belong in the viewmodel.
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

    //FIXME yeah this definitely doesn't belong in the viewmodel. Will refactor after further reading
//    public void editGoal() {
//        Intent intent = GoalPagerActivity.newIntent(mActivity, mGoal.getId());
//        mActivity.startActivity(intent);
//    }
}