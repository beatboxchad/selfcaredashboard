package com.beatboxchad.android.selfcaredashboard;

import java.sql.Date;
import java.util.UUID;

/**
 * Created by chad on 12/21/17.
 */

public class Goal {

    private UUID mId;
    private String mTitle;
    private int mInterval; // goal mInterval in days
    private boolean mPolarity; // true for chase, false for avoid
    private Date mTouched; //

    public void setTouched(Date touched) {
        mTouched = touched;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public Date getTouched() { return mTouched; }

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
        return mInterval;
    }

    public void setInterval(int interval) {
        this.mInterval = interval;
    }

    public boolean isPolarity() {
        return mPolarity;
    }

    public void setPolarity(boolean polarity) {
        this.mPolarity = polarity;
    }

    public Goal() {
        mId = UUID.randomUUID();
        mTitle  = "moo";
        mPolarity = false;
        mInterval = 2;
        mTouched = new Date(System.currentTimeMillis() - (86400000));
    }
}