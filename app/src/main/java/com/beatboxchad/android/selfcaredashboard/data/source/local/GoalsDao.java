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

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.beatboxchad.android.selfcaredashboard.data.Goal;

import java.util.List;

/**
 * Data Access Object for the goals table.
 */
@Dao
public interface GoalsDao {

    /**
     * Select all goals from the goals table.
     *
     * @return all goals.
     */
    @Query("SELECT * FROM Goals")
    List<Goal> getGoals();

    /**
     * Select a goal by id.
     *
     * @param goalId the goal id.
     * @return the goal with goalId.
     */
    @Query("SELECT * FROM Goals WHERE entryid = :goalId")
    Goal getGoalById(String goalId);

    /**
     * Insert a goal in the database. If the goal already exists, replace it.
     *
     * @param goal the goal to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGoal(Goal goal);

    /**
     * Update a goal.
     *
     * @param goal goal to be updated
     * @return the number of goals updated. This should always be 1.
     */
    @Update
    int updateGoal(Goal goal);

    /**
     * Update the complete status of a goal
     *
     * @param goalId    id of the goal
     * @param completed status to be updated
     */
    @Query("UPDATE goals SET completed = :completed WHERE entryid = :goalId")
    void updateCompleted(String goalId, boolean completed);

    /**
     * Delete a goal by id.
     *
     * @return the number of goals deleted. This should always be 1.
     */
    @Query("DELETE FROM Goals WHERE entryid = :goalId")
    int deleteGoalById(String goalId);

    /**
     * Delete all goals.
     */
    @Query("DELETE FROM Goals")
    void deleteGoals();

    /**
     * Delete all completed goals from the table.
     *
     * @return the number of goals deleted.
     */
    @Query("DELETE FROM Goals WHERE completed = 1")
    int deleteCompletedGoals();
}
