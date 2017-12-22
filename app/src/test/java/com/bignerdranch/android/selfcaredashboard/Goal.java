package com.bignerdranch.android.selfcaredashboard;

import android.icu.util.DateInterval;

import java.util.UUID;

/**
 * Created by chad on 12/21/17.
 */

public class Goal {

    private UUID mId;
    private String mTitle;
    private int interval; // goal interval in days
    private boolean polarity; // true for chase, false for avoid


    public UUID getId() {
        return mId;
    }
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public boolean isPolarity() {
        return polarity;
    }

    public void setPolarity(boolean polarity) {
        this.polarity = polarity;
    }



    public Goal() {
        mId = UUID.randomUUID();
    }
}
