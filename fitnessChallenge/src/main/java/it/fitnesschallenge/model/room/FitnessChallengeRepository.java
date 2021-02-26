/**
 * Questa classe implementa il pattern di Repository facendo da mediatore tra le sorgenti dati,
 * così che non sia l'app stessa ad accedere ai dati, consentendo una modifica del metodo di persistenza
 * senza variare l'interazione con i dati stessi
 */
package it.fitnesschallenge.model.room;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

import it.fitnesschallenge.model.room.dao.ExerciseDAO;
import it.fitnesschallenge.model.room.dao.ExerciseExecutionDAO;
import it.fitnesschallenge.model.room.dao.PersonalExerciseDAO;
import it.fitnesschallenge.model.room.dao.PersonalExerciseWorkoutCrossReferenceDAO;
import it.fitnesschallenge.model.room.dao.WorkoutDAO;
import it.fitnesschallenge.model.room.dao.WorkoutWithExerciseDAO;
import it.fitnesschallenge.model.room.entity.Exercise;
import it.fitnesschallenge.model.room.entity.ExerciseExecution;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.room.entity.PersonalExerciseWorkoutCrossReference;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;

public class FitnessChallengeRepository {

    private static final String TAG = "FitnessChallengeReposit";

    private ExerciseDAO exerciseDAO;
    private WorkoutDAO workoutDAO;
    private WorkoutWithExerciseDAO workoutWithExerciseDAO;
    private PersonalExerciseDAO personalExerciseDAO;
    private ExerciseExecutionDAO exerciseExecutionDAO;
    private PersonalExerciseWorkoutCrossReferenceDAO personalExerciseWorkoutCrossReferenceDAO;

    public FitnessChallengeRepository(Application application){
        FitnessChallengeDatabase database = FitnessChallengeDatabase.getInstance(application);
        exerciseDAO = database.getExerciseDAO();
        workoutWithExerciseDAO = database.getWorkoutWithExerciseDAO();
        workoutDAO = database.getWorkoutDAO();
        personalExerciseDAO = database.getPersonalExerciseDAO();
        personalExerciseWorkoutCrossReferenceDAO = database.getPersonalExerciseWorkoutCrossReferenceDAO();
        exerciseExecutionDAO = database.getExerciseExecutionDAO();
    }

    public LiveData<List<Exercise>> getListExerciseLiveData() {
        return exerciseDAO.selectAllExercise();
    }

    public LiveData<WorkoutWithExercise> getWorkoutWithExerciseList(long workoutId) {
        return workoutWithExerciseDAO.getWorkoutWithExercise(workoutId);
    }
/*
    public List<PersonalExercise> getPersonalExerciseList(long workoutId) {
        return workoutWithExerciseDAO.getPersonalExerciseList(workoutId);
    }*/

    public LiveData<List<Workout>> getWorkoutList() {
        return workoutDAO.getAllWorkOut();
    }

    public LiveData<Exercise> getExercise(int exerciseId) {
        return exerciseDAO.selectExercise(exerciseId);
    }

    public LiveData<Long> getWorkoutIdWithStartDate(Date date) {
        Log.d(TAG, "Start date: " + date.toString());
        return workoutDAO.getWorkoutStartDate(date);
    }

    public long insertWorkout(Workout workout) {
        return workoutDAO.insertWorkout(workout);
    }

    public void updateWorkout(Workout workout) {
        workoutDAO.update(workout);
    }

    public LiveData<ExerciseExecution> getLastExecutionExecution(long personalExerciseId) {
        return exerciseExecutionDAO.selectLastExerciseExecution(personalExerciseId);
    }

    public long[] insertPersonalExerciseList(List<PersonalExercise> personalExerciseList) {
        return personalExerciseDAO.insertPersonalExercise(personalExerciseList);
    }

    public void insertPersonalExerciseWorkoutReference(PersonalExerciseWorkoutCrossReference personalExerciseWorkoutCrossReference) {
        personalExerciseWorkoutCrossReferenceDAO.createReference(personalExerciseWorkoutCrossReference);
    }

    public void insertExecution(ExerciseExecution exerciseExecution) {
        exerciseExecutionDAO.insertExecution(exerciseExecution);
    }

    public LiveData<List<ExerciseExecution>> selectLastWorkoutExecution(Date currentDate) {
        return exerciseExecutionDAO.selectExecutionInDate(currentDate);
    }

    public LiveData<Integer> getNumberOfExecution() {
        return exerciseExecutionDAO.selectNumberOfExecution();
    }

    public LiveData<List<ExerciseExecution>> getLastUsedKilograms() {
        return exerciseExecutionDAO.selectLastUsedKilograms();
    }

    public int updatePersonalExerciseList(List<PersonalExercise> personalExerciseList) {
        return personalExerciseDAO.updatePersonalExerciseList(personalExerciseList);
    }

    public int deleteWorkoutExerciseCrossReference(PersonalExerciseWorkoutCrossReference crossReference) {
        return personalExerciseWorkoutCrossReferenceDAO.deleteReference(crossReference);
    }

    public int deletePersonalExerciseList(List<PersonalExercise> personalExerciseList) {
        return personalExerciseDAO.deletePersonalExerciseList(personalExerciseList);
    }

    public long insertPersonalExercise(PersonalExercise personalExercise) {
        return personalExerciseDAO.insertPersonalExercise(personalExercise);
    }
}
