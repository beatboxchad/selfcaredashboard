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

import com.beatboxchad.android.selfcaredashboard.data.Goal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GoalsDaoTest {

    private static final Goal GOAL = new Goal("title", "description", "id", true);

    private ToDoDatabase mDatabase;

    @Before
    public void initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                ToDoDatabase.class).build();
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
        assertGoal(loaded, "id", "title", "description", true);
    }

    @Test
    public void insertGoalReplacesOnConflict() {
        //Given that a goal is inserted
        mDatabase.goalDao().insertGoal(GOAL);

        // When a goal with the same id is inserted
        Goal newGoal = new Goal("title2", "description2", "id", true);
        mDatabase.goalDao().insertGoal(newGoal);
        // When getting the goal by id from the database
        Goal loaded = mDatabase.goalDao().getGoalById(GOAL.getId());

        // The loaded data contains the expected values
        assertGoal(loaded, "id", "title2", "description2", true);
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
        assertGoal(goals.get(0), "id", "title", "description", true);
    }

    @Test
    public void updateGoalAndGetById() {
        // When inserting a goal
        mDatabase.goalDao().insertGoal(GOAL);

        // When the goal is updated
        Goal updatedGoal = new Goal("title2", "description2", "id", true);
        mDatabase.goalDao().updateGoal(updatedGoal);

        // When getting the goal by id from the database
        Goal loaded = mDatabase.goalDao().getGoalById("id");

        // The loaded data contains the expected values
        assertGoal(loaded, "id", "title2", "description2", true);
    }

    @Test
    public void updateArchivedAndGetById() {
        // When inserting a goal
        mDatabase.goalDao().insertGoal(GOAL);

        // When the goal is updated
        mDatabase.goalDao().updateArchived(GOAL.getId(), false);

        // When getting the goal by id from the database
        Goal loaded = mDatabase.goalDao().getGoalById("id");

        // The loaded data contains the expected values
        assertGoal(loaded, GOAL.getId(), GOAL.getTitle(), GOAL.getDescription(), false);
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
        //Given a archived goal inserted
        mDatabase.goalDao().insertGoal(GOAL);

        //When deleting archived goals
        mDatabase.goalDao().deleteArchivedGoals();

        //When getting the goals
        List<Goal> goals = mDatabase.goalDao().getGoals();
        // The list is empty
        assertThat(goals.size(), is(0));
    }

    private void assertGoal(Goal goal, String id, String title,
            String description, boolean archived) {
        assertThat(goal, notNullValue());
        assertThat(goal.getId(), is(id));
        assertThat(goal.getTitle(), is(title));
        assertThat(goal.getDescription(), is(description));
        assertThat(goal.isPolarity(), is(archived));
    }
}
