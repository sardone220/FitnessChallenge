package it.fitnesschallenge.model.view;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import it.fitnesschallenge.model.ExecutionList;
import it.fitnesschallenge.model.room.entity.Workout;

public class GetWorkoutFromDBModel extends AndroidViewModel {
    private static final String TAG = "GetWorkoutFromDBModel";

    private MutableLiveData<Workout> workoutMutableLiveData;
    private MutableLiveData<ExecutionList> executionListMutableLiveData;

    public GetWorkoutFromDBModel(@NonNull Application application) {
        super(application);
        workoutMutableLiveData = new MutableLiveData<>();
        executionListMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Workout> getWorkoutMutableLiveData() {
        return workoutMutableLiveData;
    }

    public void setWorkoutMutableLiveData(Workout workout) {
        Log.d(TAG, "Salvo workout.");
        this.workoutMutableLiveData.setValue(workout);
    }

    public MutableLiveData<ExecutionList> getExecutionListMutableLiveData() { return executionListMutableLiveData; }

    public void setExecutionListMutableLiveData(ExecutionList executionList) {
        Log.d(TAG, "Salvo esecuzione.");
        this.executionListMutableLiveData.setValue(executionList);
    }
}
