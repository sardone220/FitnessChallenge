package it.fitnesschallenge.model.view;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;

public class CreationViewModel extends AndroidViewModel {

    private static final String TAG = "CreationViewModel";

    private MutableLiveData<Integer> mLiveDataProgress;
    private ArrayList<Integer> mListSteps;
    private MutableLiveData<ArrayList<Integer>> mLiveDataSteps;
    private MutableLiveData<String> mEmail;
    private MutableLiveData<String> mGoal;
    private MutableLiveData<Date> mStartDate;
    private MutableLiveData<Date> mFinishDate;
    private MutableLiveData<WorkoutWithExercise> mWorkoutWithExercise;
    private MutableLiveData<Integer> mWorkoutId;
    private MutableLiveData<List<PersonalExercise>> mPersonalExerciseList;
    private MutableLiveData<Boolean> mIsError;
    private boolean mError;

    public CreationViewModel(@NonNull Application application) {
        super(application);
        mListSteps = new ArrayList<>();
        mListSteps.add(1);
        mLiveDataSteps = new MutableLiveData<>();
        mGoal = new MutableLiveData<>();
        mEmail = new MutableLiveData<>();
        mStartDate = new MutableLiveData<>();
        mLiveDataProgress = new MutableLiveData<>();
        mWorkoutWithExercise = new MutableLiveData<>();
        mWorkoutId = new MutableLiveData<>();
        mPersonalExerciseList = new MutableLiveData<>();
        mFinishDate = new MutableLiveData<>();
        mError = false;
        mIsError = new MutableLiveData<>(false);

        setWorkoutId(-1);
        setLiveDataSteps();
    }

    public MutableLiveData<Date> getFinishDate() {
        return mFinishDate;
    }

    public void setFinishDate(Date mFinishDate) {
        this.mFinishDate.setValue(mFinishDate);
    }

    public LiveData<String> getEmail() {
        return mEmail;
    }

    public MutableLiveData<Integer> getWorkoutId() {
        return mWorkoutId;
    }

    public void setWorkoutId(Integer workoutId) {
        this.mWorkoutId.setValue(workoutId);
    }

    public void setEmail(String email) {
        mEmail.setValue(email);
    }

    public LiveData<String> getGoal() {
        return mGoal;
    }

    public void setGoal(String goal) {
        mGoal.setValue(goal);
    }

    public LiveData<Date> getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date date) {
        mStartDate.setValue(date);
    }

    public void setLiveDataProgress(int progress) {
        mLiveDataProgress.setValue(progress);
    }

    public void setLiveDataSteps(){
        mLiveDataSteps.setValue(mListSteps);
    }

    public void setPersonalExerciseList(List<PersonalExercise> personalExerciseList) {
        mPersonalExerciseList.setValue(personalExerciseList);
    }

    public MutableLiveData<List<PersonalExercise>> getPersonalExerciseList() {
        return mPersonalExerciseList;
    }

    public MutableLiveData<Boolean> getIsError() {
        return mIsError;
    }

    public void setIsError(Boolean isError) {
        this.mIsError.setValue(isError);
    }

    public void nextStep(){
        Log.d(TAG, "NextStep");
        mListSteps.add(mListSteps.get(mListSteps.size() - 1) + 1);
        Log.d(TAG, "Step: " + mListSteps.size());
        setLiveDataSteps();
    }

    public void prevStep(){
        Log.d(TAG, "PrevStep");
        mListSteps.remove(mListSteps.size() - 1);
        setLiveDataSteps();
    }

    public LiveData<Integer> getLiveDataProgress(){
        return mLiveDataProgress;
    }

    public LiveData<ArrayList<Integer>> getLiveDataSteps(){
        return mLiveDataSteps;
    }

    public void resetLiveData() {
        mListSteps = new ArrayList<>();
        mListSteps.add(1);
        mLiveDataSteps = new MutableLiveData<>();
        mGoal = new MutableLiveData<>();
        mEmail = new MutableLiveData<>();
        mStartDate = new MutableLiveData<>();
        mLiveDataProgress = new MutableLiveData<>();
        mWorkoutWithExercise = new MutableLiveData<>();
        mWorkoutId = new MutableLiveData<>();
        mPersonalExerciseList = new MutableLiveData<>();
        mFinishDate = new MutableLiveData<>();
        mError = false;
        mIsError = new MutableLiveData<>(false);

        setWorkoutId(-1);
        setLiveDataSteps();
    }

    public boolean isError() {
        return mError;
    }
}
