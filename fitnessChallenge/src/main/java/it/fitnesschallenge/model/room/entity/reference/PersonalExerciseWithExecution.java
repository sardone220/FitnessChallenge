package it.fitnesschallenge.model.room.entity.reference;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import it.fitnesschallenge.model.room.entity.ExerciseExecution;
import it.fitnesschallenge.model.room.entity.PersonalExercise;

public class PersonalExerciseWithExecution {

    @Embedded
    private PersonalExercise personalExercise;
    @Relation(parentColumn = "exercise_id",
            entityColumn = "exercise_id")
    private List<ExerciseExecution> personalExerciseList;

    public PersonalExercise getPersonalExercise() {
        return personalExercise;
    }

    public void setPersonalExercise(PersonalExercise personalExercise) {
        this.personalExercise = personalExercise;
    }

    public List<ExerciseExecution> getPersonalExerciseList() {
        return personalExerciseList;
    }

    public void setPersonalExerciseList(List<ExerciseExecution> personalExerciseList) {
        this.personalExerciseList = personalExerciseList;
    }
}
