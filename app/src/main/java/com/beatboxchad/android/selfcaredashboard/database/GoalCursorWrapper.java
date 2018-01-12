package com.beatboxchad.android.selfcaredashboard.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.beatboxchad.android.selfcaredashboard.Goal;
import com.beatboxchad.android.selfcaredashboard.database.GoalDbSchema.GoalTable;

import java.sql.Date;
import java.util.UUID;

public class GoalCursorWrapper extends CursorWrapper {

    public GoalCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Goal getGoal() {
        String uuidString = getString(getColumnIndex(GoalTable.Cols.UID));
        String title = getString(getColumnIndex(GoalTable.Cols.TITLE));
        long touched = getLong(getColumnIndex(GoalTable.Cols.TOUCHED));
        int isPolarity = getInt(getColumnIndex(GoalTable.Cols.POLARITY));
        int interval = getInt(getColumnIndex(GoalTable.Cols.INTERVAL));

        Goal goal = new Goal(UUID.fromString(uuidString));
        goal.setTitle(title);
        goal.setTouched(new Date(touched));
        goal.setPolarity(isPolarity != 0);
        goal.setInterval(interval);

        return goal;
    }
}