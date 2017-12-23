package com.beatboxchad.android.selfcaredashboard;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GoalActivity extends AppCompatActivity {
    @Override
    protected Fragment createFragment() {
        return new GoalFragment();
    }
}