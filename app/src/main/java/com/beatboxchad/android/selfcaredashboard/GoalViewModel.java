package com.beatboxchad.android.selfcaredashboard;

import android.app.Activity;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.widget.TextView;

public class GoalViewModel extends BaseObservable {
    private Goal mGoal;
    private Activity mActivity;
    private String mGoalColor;

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
        notifyChange();
    }

    public void editGoal() {
        Intent intent = GoalPagerActivity.newIntent(mActivity, mGoal.getId());
        mActivity.startActivity(intent);
    }

    // set it on the goal's TextView here. Basically I have to tell the binding library
    // how to bind it. There's not a built-in adapter.
    @BindingAdapter("android:textColor")
    public static void customGoalColor(TextView textView, String color) {
        textView.setTextColor(Color.parseColor(color));
    }

    private void calcColor() {
        // do your logarithmic fade based on polarity, interval, and touched.
        // and put it in mColor
        mGoalColor = "#0000FF";
    }

    @Bindable
    public String getColor() {
        calcColor();
        notifyPropertyChanged(BR.color);
        return mGoalColor;
    }
}