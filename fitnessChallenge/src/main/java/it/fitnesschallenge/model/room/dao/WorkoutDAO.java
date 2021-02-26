/**
 * Questo DAO permette di eseguire operazioni sulle tuple dell'entità workout
 */
package it.fitnesschallenge.model.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

import it.fitnesschallenge.model.room.entity.Workout;

@Dao
public interface WorkoutDAO {

    @Insert
    long insertWorkout(Workout workout);

    @Update
    int update(Workout workout);

    @Transaction
    @Query("SELECT * FROM workout WHERE workout_id = :workoutId")
    LiveData<Workout> getWorkout(int workoutId);

    @Transaction
    @Query("SELECT workout_id FROM workout WHERE start_date = :startDate")
    LiveData<Long> getWorkoutStartDate(Date startDate);

    @Transaction
    @Query("SELECT * FROM workout")
    LiveData<List<Workout>> getAllWorkOut();
}
