package com.beatboxchad.android.selfcaredashboard.database;

public class GoalDbSchema {
    public static final class GoalTable {
        public static final String NAME = "goals";

        public static final class Cols {
            public static final String UID = "uid";
            public static final String TITLE = "title";
            public static final String TOUCHED = "touched";
            public static final String POLARITY = "polarity";
            public static final String INTERVAL = "interval";
        }
    }
}