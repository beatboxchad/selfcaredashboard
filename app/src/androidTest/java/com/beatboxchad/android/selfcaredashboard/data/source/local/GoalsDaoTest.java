/*
 * Copyright 2017, The Android Open Source Project
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

package com.beatboxchad.android.selfcaredashboard.data.source.local;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.beatboxchad.android.selfcaredashboard.data.Goal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GoalsDaoTest {


    private long DAY_IN_MS = 1000 * 60 * 60 * 24;
    private long A_DAY_AGO = System.currentTimeMillis() - (1 * DAY_IN_MS);
    private long A_WEEK_AGO = System.currentTimeMillis() - (7 * DAY_IN_MS);

    private String ID = "12345booya";
    private String TITLE = "eat a bucket of candy";
    private String TITLE2 = "shit a bucket of candy";
    private boolean POLARITY = false;
    private boolean POLARITY2 = true;
    private int INTERVAL = 7;
    private int INTERVAL2 = 14;
    private boolean ARCHIVED = false;
    private boolean ARCHIVED2 = true;
    private long TOUCHED = A_DAY_AGO;
    private long TOUCHED2 = A_WEEK_AGO;


    Goal GOAL = new Goal.Builder(ID)
            .setTitle(TITLE)
            .setInterval(INTERVAL)
            .setPolarity(POLARITY)
            .setTouched(TOUCHED)
            .setArchived(ARCHIVED)
            .build();

    private SelfCareDatabase mDatabase;

    @Before
    public void initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                SelfCareDatabase.class).build();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertGoalAndGetById() {
        // When inserting a goal
        mDatabase.goalDao().insertGoal(GOAL);

        // When getting the goal by id from the database
        Goal loaded = mDatabase.goalDao().getGoalById(GOAL.getId());

        // The loaded data contains the expected values
        assertGoal(loaded, ID, TITLE, INTERVAL, POLARITY, ARCHIVED, TOUCHED);
    }

    @Test
    public void insertGoalReplacesOnConflict() {
        //Given that a goal is inserted
        mDatabase.goalDao().insertGoal(GOAL);

        // When a goal with the same id is inserted
        Goal newGoal = new Goal.Builder(ID)
                .setTitle(TITLE2)
                .setPolarity(POLARITY2)
                .setInterval(INTERVAL2)
                .setArchived(ARCHIVED2)
                .setTouched(TOUCHED2)
                .build();

        mDatabase.goalDao().insertGoal(newGoal);
        // When getting the goal by id from the database
        Goal loaded = mDatabase.goalDao().getGoalById(GOAL.getId());

        // The loaded data contains the expected values
        assertGoal(loaded, ID, TITLE2, INTERVAL2, POLARITY2, ARCHIVED2, TOUCHED2);
    }

    @Test
    public void insertGoalAndGetGoals() {
        // When inserting a goal
        mDatabase.goalDao().insertGoal(GOAL);

        // When getting the goals from the database
        List<Goal> goals = mDatabase.goalDao().getGoals();

        // There is only 1 goal in the database
        assertThat(goals.size(), is(1));
        // The loaded data contains the expected values
        assertGoal(goals.get(0), ID, TITLE, INTERVAL, POLARITY, ARCHIVED, TOUCHED);
    }

    @Test
    public void updateGoalAndGetById() {
        // When inserting a goal
        mDatabase.goalDao().insertGoal(GOAL);

        // When the goal is updated
        Goal updatedGoal = new Goal.Builder(ID)
                .setTitle(TITLE2)
                .setPolarity(POLARITY2)
                .setInterval(INTERVAL2)
                .setArchived(ARCHIVED2)
                .setTouched(TOUCHED2)
                .build();

        mDatabase.goalDao().updateGoal(updatedGoal);

        // When getting the goal by id from the database
        Goal loaded = mDatabase.goalDao().getGoalById(ID);

        // The loaded data contains the expected values
        assertGoal(loaded, ID, TITLE2, INTERVAL2, POLARITY2, ARCHIVED2, TOUCHED2);
    }

    @Test
    public void updateArchivedAndGetById() {
        // When inserting a goal
        mDatabase.goalDao().insertGoal(GOAL);

        // When the goal is updated
        mDatabase.goalDao().updateArchived(GOAL.getId(), true);

        // When getting the goal by id from the database
        Goal loaded = mDatabase.goalDao().getGoalById(ID);

        // The loaded data contains the expected values
        assertGoal(loaded, GOAL.getId(), GOAL.getTitle(), GOAL.getInterval(), GOAL.getPolarity(), true, GOAL.getTouched());
    }

    @Test
    public void deleteGoalByIdAndGettingGoals() {
        //Given a goal inserted
        mDatabase.goalDao().insertGoal(GOAL);

        //When deleting a goal by id
        mDatabase.goalDao().deleteGoalById(GOAL.getId());

        //When getting the goals
        List<Goal> goals = mDatabase.goalDao().getGoals();
        // The list is empty
        assertThat(goals.size(), is(0));
    }

    @Test
    public void deleteGoalsAndGettingGoals() {
        //Given a goal inserted
        mDatabase.goalDao().insertGoal(GOAL);

        //When deleting all goals
        mDatabase.goalDao().deleteGoals();

        //When getting the goals
        List<Goal> goals = mDatabase.goalDao().getGoals();
        // The list is empty
        assertThat(goals.size(), is(0));
    }

    @Test
    public void deleteArchivedGoalsAndGettingGoals() {


        //When getting the goals
        List<Goal> goals = mDatabase.goalDao().getGoals();

        for (Goal goal : goals) {
            mDatabase.goalDao().insertGoal(new Goal.Builder(goal)
                    .setArchived(true).build());
        }

        //When deleting archived goals
        mDatabase.goalDao().deleteArchivedGoals();

        // The list is empty
        assertThat(goals.size(), is(0));
    }

    private void assertGoal(Goal goal,
                            String id,
                            String title,
                            int interval,
                            boolean polarity,
                            boolean archived,
                            long touched) {
        assertThat(goal, notNullValue());
        assertThat(goal.getId(), is(id));
        assertThat(goal.getTitle(), is(title));
        assertThat(goal.getInterval(), is(interval));
        assertThat(goal.isArchived(), is(archived));
        assertThat(goal.getPolarity(), is(polarity));
        assertThat(goal.getTouched(), is(touched));
    }
}