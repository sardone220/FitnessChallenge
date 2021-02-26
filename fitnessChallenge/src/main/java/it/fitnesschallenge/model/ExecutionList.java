package it.fitnesschallenge.model;

import java.util.List;

import it.fitnesschallenge.model.room.entity.ExerciseExecution;

public class ExecutionList {

    private List<ExerciseExecution> mExerciseList;

    public ExecutionList() {
        // Necessario per deserializzazione Firestore
    }

    public ExecutionList(List<ExerciseExecution> mExerciseList) {
        this.mExerciseList = mExerciseList;
    }

    public List<ExerciseExecution> getExerciseList() {
        return mExerciseList;
    }

    public void setExerciseList(List<ExerciseExecution> mExerciseList) {
        this.mExerciseList = mExerciseList;
    }
}
