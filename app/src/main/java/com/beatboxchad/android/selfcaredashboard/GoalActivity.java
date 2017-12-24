package com.beatboxchad.android.selfcaredashboard;

import android.support.v4.app.Fragment;

public class GoalActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new GoalFragment();
    }
}