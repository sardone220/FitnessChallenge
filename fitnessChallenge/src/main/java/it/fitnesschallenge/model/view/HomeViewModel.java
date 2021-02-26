package it.fitnesschallenge.model.view;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import it.fitnesschallenge.model.room.FitnessChallengeRepository;
import it.fitnesschallenge.model.room.entity.Exercise;

public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";
    private LiveData<List<Exercise>> exerciseList;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        FitnessChallengeRepository fitnessChallengeRepository = new FitnessChallengeRepository(application);
        exerciseList = fitnessChallengeRepository.getListExerciseLiveData();
        Log.d(TAG, "Repository creato");
    }

    public LiveData<List<Exercise>> getExerciseList() {
        return exerciseList;
    }
}
