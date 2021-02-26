package it.fitnesschallenge.model.room.entity.reference;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.room.entity.PersonalExerciseWorkoutCrossReference;
import it.fitnesschallenge.model.room.entity.Workout;

public class WorkoutWithExercise {

    @Embedded
    private Workout workout;
    @Relation(
            parentColumn = "workout_id",
            entityColumn = "exercise_id",
            associateBy = @Junction(PersonalExerciseWorkoutCrossReference.class)
    )
    private List<PersonalExercise> personalExerciseList;

    @Ignore
    public WorkoutWithExercise() {
        // Required empty constructor
    }

    public WorkoutWithExercise(Workout workout, List<PersonalExercise> personalExerciseList) {
        this.workout = workout;
        this.personalExerciseList = personalExerciseList;
    }

    public Workout getWorkout() {
        return workout;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    public List<PersonalExercise> getPersonalExerciseList() {
        return personalExerciseList;
    }

    public void setPersonalExerciseList(List<PersonalExercise> personalExerciseList) {
        this.personalExerciseList = personalExerciseList;
    }
}
