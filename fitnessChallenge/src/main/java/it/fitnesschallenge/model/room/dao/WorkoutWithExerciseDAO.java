package it.fitnesschallenge.model.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;

@Dao
public interface WorkoutWithExerciseDAO {

    @Transaction
    @Query("SELECT * FROM workout WHERE workout_id = :workoutId")
    LiveData<WorkoutWithExercise> getWorkoutWithExercise(long workoutId);

    @Transaction
    @Query("SELECT personal_exercise_id, exercise_id, steps, repetition, cool_down FROM personal_exercise " +
            "NATURAL JOIN workout WHERE workout_id = :workoutId")
    List<PersonalExercise> getPersonalExerciseList(long workoutId);
}
