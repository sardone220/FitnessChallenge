package it.fitnesschallenge.model.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import it.fitnesschallenge.model.room.entity.reference.ExerciseAndPersonalExercise;

@Dao
public interface ExerciseAndPersonalExerciseDAO {

    @Insert
    long insert(ExerciseAndPersonalExercise exerciseAndPersonalExercise);

    @Transaction
    @Query("SELECT * FROM exercise")
    LiveData<List<ExerciseAndPersonalExercise>> selectAllPersonalExercise();
}
