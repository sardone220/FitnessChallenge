package it.fitnesschallenge.model.view;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.fitnesschallenge.model.room.FitnessChallengeRepository;
import it.fitnesschallenge.model.room.WorkoutType;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.room.entity.PersonalExerciseWorkoutCrossReference;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;

public class EditListViewModel extends AndroidViewModel {

    private static final String TAG = "EditListViewModel";

    private MutableLiveData<List<PersonalExercise>> mPersonalExerciseListLiveData;
    private List<PersonalExercise> mPersonalExerciseList;
    private List<PersonalExercise> mDeleteExerciseList;
    private FitnessChallengeRepository mRepository;
    private boolean mFirstExecution;
    private Workout mWorkout;

    public EditListViewModel(@NonNull Application application) {
        super(application);
        mRepository = new FitnessChallengeRepository(application);
        mPersonalExerciseListLiveData = new MutableLiveData<>();
        mDeleteExerciseList = new ArrayList<>();
        mFirstExecution = true;
        mWorkout = null;
    }

    public MutableLiveData<List<PersonalExercise>> getPersonalExerciseList() {
        return mPersonalExerciseListLiveData;
    }

    public void setPersonalExerciseList(List<PersonalExercise> personalExerciseList) {
        this.mPersonalExerciseList = personalExerciseList;
        this.mPersonalExerciseListLiveData.setValue(personalExerciseList);
    }

    public LiveData<WorkoutWithExercise> getSavedExerciseList(long workoutId) {
        return mRepository.getWorkoutWithExerciseList(workoutId);
    }

    public LiveData<List<Workout>> getWorkoutList() {
        return mRepository.getWorkoutList();
    }

    public boolean isFirstExecution() {
        return mFirstExecution;
    }

    public void setFirstExecution(boolean mFirstExecution) {
        this.mFirstExecution = mFirstExecution;
    }

    public Workout getWorkout() {
        return mWorkout;
    }

    public void setWorkout(Workout mWorkout) {
        this.mWorkout = mWorkout;
    }

    public void deleteExerciseFromList(PersonalExercise personalExercise) {
        mDeleteExerciseList.add(personalExercise);
        mPersonalExerciseList.remove(personalExercise);
    }

    public void reAddExerciseToList(PersonalExercise personalExercise) {
        mDeleteExerciseList.remove(personalExercise);
        mPersonalExerciseList.add(personalExercise);
    }

    public LiveData<Boolean> addNewExerciseToWorkout() {
        AddNewExerciseToWorkout addNewExerciseToWorkout = new AddNewExerciseToWorkout(mRepository);
        addNewExerciseToWorkout.execute(new WorkoutWithExercise(mWorkout, mPersonalExerciseList));
        return addNewExerciseToWorkout.isFinish();
    }

    public LiveData<Boolean> deleteExerciseFromWorkout() {
        if (mDeleteExerciseList.size() > 0) {
            DeleteExerciseFromWorkout deleteExerciseFromWorkout = new DeleteExerciseFromWorkout(mRepository);
            deleteExerciseFromWorkout.execute(mDeleteExerciseList);
            return deleteExerciseFromWorkout.isFinish();
        } else
            return new MutableLiveData<>(false);
    }

    public LiveData<Boolean> addWorkoutWithExercise() {
        AddWorkoutWithExercise addWorkoutWithExercise = new AddWorkoutWithExercise(mRepository);
        addWorkoutWithExercise.execute(mPersonalExerciseList);
        return addWorkoutWithExercise.isFinish();
    }

    static class AddNewExerciseToWorkout extends AsyncTask<WorkoutWithExercise, Void, Boolean> {

        private FitnessChallengeRepository mRepository;
        private MutableLiveData<Boolean> mFinishState;

        AddNewExerciseToWorkout(FitnessChallengeRepository repository) {
            this.mRepository = repository;
            this.mFinishState = new MutableLiveData<>();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mFinishState.setValue(aBoolean);
        }

        @Override
        protected Boolean doInBackground(WorkoutWithExercise... workoutWithExercises) {
            Log.d(TAG, "Eseguo in back ground");
            Workout workout = workoutWithExercises[0].getWorkout();
            Log.d(TAG, "Workout: " + workout.getWorkOutId());
            List<PersonalExercise> personalExerciseList = workoutWithExercises[0].getPersonalExerciseList();
            Log.d(TAG, "Esercizi: " + personalExerciseList.toString());
            ArrayList<Long> newIdArrayList = new ArrayList<>();
            for (PersonalExercise personalExercise : personalExerciseList) {
                Log.d(TAG, "Inserisco esercizio: " + personalExercise.getPersonalExerciseId());
                if (personalExercise.getPersonalExerciseId() == 0)
                    newIdArrayList.add(mRepository.insertPersonalExercise(personalExercise));
            }
            for (Long id : newIdArrayList) {
                Log.d(TAG, "Creo il collegamento tra esercizio e workout");
                PersonalExerciseWorkoutCrossReference crossReference = new PersonalExerciseWorkoutCrossReference(workout.getWorkOutId(), id);
                mRepository.insertPersonalExerciseWorkoutReference(crossReference);
            }
            return true;
        }

        MutableLiveData<Boolean> isFinish() {
            return mFinishState;
        }
    }

    static class DeleteExerciseFromWorkout extends AsyncTask<List<PersonalExercise>, Void, Boolean> {

        private FitnessChallengeRepository mRepository;
        private MutableLiveData<Boolean> mFinishState;

        DeleteExerciseFromWorkout(FitnessChallengeRepository mRepository) {
            this.mRepository = mRepository;
            this.mFinishState = new MutableLiveData<>();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            this.mFinishState.setValue(aBoolean);
        }

        @SafeVarargs
        @Override
        protected final Boolean doInBackground(List<PersonalExercise>... personalExercises) {
            List<PersonalExercise> personalExerciseList = personalExercises[0];
            for (PersonalExercise personalExercise : personalExerciseList) {
                if (personalExercise.getPersonalExerciseId() > 0) {
                    personalExerciseList.remove(personalExercise);
                }
            }
            mRepository.deletePersonalExerciseList(personalExerciseList);
            return true;
        }

        MutableLiveData<Boolean> isFinish() {
            return mFinishState;
        }
    }

    static class AddWorkoutWithExercise extends AsyncTask<List<PersonalExercise>, Void, Boolean> {

        private FitnessChallengeRepository mRepository;
        private MutableLiveData<Boolean> mFinishState;

        AddWorkoutWithExercise(FitnessChallengeRepository mRepository) {
            this.mRepository = mRepository;
            this.mFinishState = new MutableLiveData<>();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mFinishState.setValue(aBoolean);
        }

        @SafeVarargs
        @Override
        protected final Boolean doInBackground(List<PersonalExercise>... lists) {
            Calendar calendar = Calendar.getInstance();
            Workout workout = new Workout(true, calendar.getTime(), calendar.getTime(), WorkoutType.OUTDOOR);
            long idWorkout = mRepository.insertWorkout(workout);
            Log.d(TAG, "Id del nuovo workout: " + idWorkout);
            long[] personalExerciseIds = mRepository.insertPersonalExerciseList(lists[0]);
            Log.d(TAG, "Id degli esercizi inseriti: " + personalExerciseIds.toString());
            for (long id : personalExerciseIds) {
                PersonalExerciseWorkoutCrossReference reference = new PersonalExerciseWorkoutCrossReference(idWorkout, id);
                Log.d(TAG, "Cross reference: " + reference.getExerciseId() + " : " + reference.getWorkoutId());
                mRepository.insertPersonalExerciseWorkoutReference(reference);
            }
            return true;
        }

        MutableLiveData<Boolean> isFinish() {
            return mFinishState;
        }
    }
}
