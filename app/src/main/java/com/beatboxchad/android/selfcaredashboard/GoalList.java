package com.beatboxchad.android.selfcaredashboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beatboxchad.android.selfcaredashboard.database.GoalBaseHelper;
import com.beatboxchad.android.selfcaredashboard.database.GoalCursorWrapper;
import com.beatboxchad.android.selfcaredashboard.database.GoalDbSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.beatboxchad.android.selfcaredashboard.database.GoalDbSchema.*;
import static com.beatboxchad.android.selfcaredashboard.database.GoalDbSchema.GoalTable.Cols.INTERVAL;
import static com.beatboxchad.android.selfcaredashboard.database.GoalDbSchema.GoalTable.Cols.POLARITY;
import static com.beatboxchad.android.selfcaredashboard.database.GoalDbSchema.GoalTable.Cols.TITLE;
import static com.beatboxchad.android.selfcaredashboard.database.GoalDbSchema.GoalTable.Cols.TOUCHED;
import static com.beatboxchad.android.selfcaredashboard.database.GoalDbSchema.GoalTable.Cols.UID;

public class GoalList {
    private static GoalList sGoalList;
    private Context mContext;
    private SQLiteDatabase mDatabase;


    public static GoalList get(Context context) {
        if (sGoalList == null) {
            sGoalList = new GoalList(context);
        }
        return sGoalList;
    }

    private GoalList(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new GoalBaseHelper(mContext)
                .getWritableDatabase();
    }

    public void addGoal(Goal g) {
                ContentValues values = getContentValues(g);
        mDatabase.insert(GoalTable.NAME, null, values);
    }

    public List<Goal> getGoals() {
        List<Goal> goals = new ArrayList<>();
        GoalCursorWrapper cursor = queryGoals(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                goals.add(cursor.getGoal());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return goals;
    }

    public Goal getGoal(UUID id) {
        GoalCursorWrapper cursor = queryGoals(
                GoalTable.Cols.UID + " = ?",
                new String[]{id.toString()}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getGoal();
        } finally {
            cursor.close();
        }

    }
        public void updateGoal(Goal goal) {
        String uuidString = goal.getId().toString();
        ContentValues values = getContentValues(goal);
        mDatabase.update(GoalTable.NAME, values,
                GoalTable.Cols.UID + " = ?",
                new String[]{uuidString});
    }

    private GoalCursorWrapper queryGoals(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                GoalTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );
        return new GoalCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Goal goal) {
        ContentValues values = new ContentValues();
        values.put(UID, goal.getId().toString());
        values.put(TITLE, goal.getTitle());
        values.put(TOUCHED, goal.getTouched().getTime());
        values.put(POLARITY, goal.isPolarity() ? 1 : 0);
        values.put(INTERVAL, goal.getInterval());
        return values;
    }

}
