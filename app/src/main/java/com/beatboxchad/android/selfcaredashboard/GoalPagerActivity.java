package com.beatboxchad.android.selfcaredashboard;

import android.content.Context;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

public class GoalPagerActivity extends AppCompatActivity {
    private static final String EXTRA_GOAL_ID =
            "com.beatboxchad.android.selfcaredashboard.goal_id";

    private ViewPager mViewPager;
    private List<Goal> mGoals;

    public static Intent newIntent(Context packageContext, UUID goalId) {
        Intent intent = new Intent(packageContext, GoalPagerActivity.class);
        intent.putExtra(EXTRA_GOAL_ID, goalId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_goal_pager);

        UUID goalId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_GOAL_ID);

        mViewPager = (ViewPager) findViewById(R.id.activity_goal_pager_view_pager);

        mGoals = GoalList.get(this).getGoals();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Goal goal = mGoals.get(position);
                return GoalFragment.newInstance(goal.getId());
            }

            @Override
            public int getCount() {
                return mGoals.size();
            }
        });

        for (int i = 0; i < mGoals.size(); i++) {
            if (mGoals.get(i).getId().equals(goalId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
}