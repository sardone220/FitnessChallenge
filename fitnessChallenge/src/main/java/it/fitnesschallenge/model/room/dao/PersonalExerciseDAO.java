package it.fitnesschallenge.model.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import java.util.List;

import it.fitnesschallenge.model.room.entity.PersonalExercise;

@Dao
public interface PersonalExerciseDAO {

    @Insert
    long insertPersonalExercise(PersonalExercise personalExercise);

    @Insert
    long[] insertPersonalExercise(List<PersonalExercise> personalExerciseList);

    @Update
    int updatePersonalExerciseList(List<PersonalExercise> personalExerciseList);

    @Delete
    int deletePersonalExerciseList(List<PersonalExercise> personalExerciseList);
}
