package com.beatboxchad.android.selfcaredashboard;

import java.util.Date;
import java.util.UUID;

/**
 * Created by chad on 12/21/17.
 */

public class Goal {

    private UUID mId;
    private String mTitle;
    private int mInterval; // goal mInterval in days
    private boolean mPolarity; // true for chase, false for avoid
    private Date mCreateTime; //

    public void setCreateTime(Date createTime) {
        mCreateTime = createTime;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public Date getCreateTime() { return mCreateTime; }

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
    }
}
