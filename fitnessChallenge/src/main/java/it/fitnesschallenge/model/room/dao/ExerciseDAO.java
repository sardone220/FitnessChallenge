package it.fitnesschallenge.model.room.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import it.fitnesschallenge.model.room.entity.Exercise;

@Dao
public interface ExerciseDAO {

    @Insert
    void insert(Exercise listLiveData);

    @Transaction
    @Query("SELECT * FROM Exercise")
    LiveData<List<Exercise>> selectAllExercise();

    @Transaction
    @Query("SELECT * FROM Exercise WHERE exercise_id = :exerciseId")
    LiveData<Exercise> selectExercise(int exerciseId);
}
