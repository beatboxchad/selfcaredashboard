/*
 * Copyright 2017, Chad Cassady
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beatboxchad.android.selfcaredashboard.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Immutable model class for a Goal.
 */
@Entity(tableName = "goals")
public final class Goal {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "entryid")
    private final String mId;

    @Nullable
    @ColumnInfo(name = "title")
    private final String mTitle;

    @ColumnInfo(name = "polarity")
    private final boolean mPolarity;

    @Nullable
    @ColumnInfo(name = "interval")
    private final int mInterval;

    @ColumnInfo(name = "touched")
    private final long mTouched;


    @ColumnInfo(name = "archived")
    private final boolean mArchived;


    public Goal(@NonNull String id,
                @Nullable String title,
                @Nullable boolean polarity,
                @Nullable int interval,
                @Nullable long touched,
                @Nullable boolean archived) {
        this(new Goal.Builder(id)
                .setTitle(title)
                .setPolarity(polarity)
                .setInterval(interval)
                .setTouched(touched)
                .setArchived(archived));
    }

    public static class Builder {
        private final String mId;
        private String mTitle;
        private int mInterval;
        private boolean mPolarity;
        private boolean mArchived;
        private long mTouched;

        public Builder(String id) {
            this.mId = id;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setInterval(int interval) {
            this.mInterval = interval;
            return this;
        }

        public Builder setPolarity(boolean polarity) {
            this.mPolarity = polarity;
            return this;
        }

        public Builder setArchived(boolean archived) {
            this.mArchived = archived;
            return this;
        }

        public Builder setTouched(long touched) {
            this.mTouched = touched;
            return this;
        }

        public Goal build() {
            return new Goal(this);
        }

    }

    private Goal(Builder builder) {
        mId = builder.mId;
        mTitle = builder.mTitle;
        mPolarity = builder.mPolarity;
        mTouched = builder.mTouched;
        mInterval = builder.mInterval;
        mArchived = builder.mArchived;
    }


    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }


    @Nullable
    public String getTitleForList() {
            return mTitle;
    }

    @Nullable
    public boolean getPolarity() {
        return mPolarity;
    }

    @Nullable
    public boolean isActive() {
        return !mArchived;
    }

    @Nullable
    public boolean isArchived() {
        return mArchived;
    }

    @Nullable
    public int getInterval() {
        return mInterval;
    }

    @Nullable
    public long getTouched() {
        return mTouched;
    }


    public boolean isEmpty() {
        return Strings.isNullOrEmpty(mTitle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goal goal = (Goal) o;
        return Objects.equal(mId, goal.mId) &&
                Objects.equal(mTitle, goal.mTitle) &&
                Objects.equal(mPolarity, goal.mPolarity) &&
                Objects.equal(mInterval, goal.mInterval) &&
                Objects.equal(mArchived, goal.mArchived) &&
                Objects.equal(mTouched, goal.mTouched);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mTitle, mPolarity, mInterval, mTouched, mArchived);
    }

    @Override
    public String toString() {
        return "Goal with title " + mTitle;
    }
}
