package it.fitnesschallenge.model.room.entity.reference;

import androidx.room.Embedded;
import androidx.room.Relation;

import it.fitnesschallenge.model.room.entity.Exercise;
import it.fitnesschallenge.model.room.entity.PersonalExercise;

public class ExerciseAndPersonalExercise {

    @Embedded
    private Exercise exercise;
    @Relation(
            parentColumn = "exercise_id",
            entityColumn = "exercise_id"
    )
    private PersonalExercise personalExercise;

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public PersonalExercise getPersonalExercise() {
        return personalExercise;
    }

    public void setPersonalExercise(PersonalExercise personalExercise) {
        this.personalExercise = personalExercise;
    }
}
