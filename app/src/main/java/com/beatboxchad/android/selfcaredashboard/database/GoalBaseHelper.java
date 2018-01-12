package com.beatboxchad.android.selfcaredashboard.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.beatboxchad.android.selfcaredashboard.database.GoalDbSchema.GoalTable;

public class GoalBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "goalBase.db";

    public GoalBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + GoalTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                GoalTable.Cols.UID + ", " +
                GoalTable.Cols.TITLE + ", " +
                GoalTable.Cols.TOUCHED + ", " +
                GoalTable.Cols.POLARITY + ", " +
                GoalTable.Cols.INTERVAL +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}