package it.fitnesschallenge.model.view;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import it.fitnesschallenge.model.room.FitnessChallengeRepository;
import it.fitnesschallenge.model.room.entity.Exercise;
import it.fitnesschallenge.model.room.entity.PersonalExercise;

public class AddExerciseToListModel extends AndroidViewModel {

    private LiveData<List<Exercise>> mExerciseList;
    private MutableLiveData<List<PersonalExercise>> mPersonalExerciseLiveData;
    private ArrayList<PersonalExercise> mPersonalExerciseList;

    public AddExerciseToListModel(@NonNull Application application) {
        super(application);
        FitnessChallengeRepository repository = new FitnessChallengeRepository(application);
        mExerciseList = repository.getListExerciseLiveData();
        mPersonalExerciseLiveData = new MutableLiveData<>();
        mPersonalExerciseList = new ArrayList<>();
    }

    public LiveData<List<Exercise>> getExerciseList(){
        return mExerciseList;
    }

    public void addPersonalExercise(PersonalExercise personalExercise){
        mPersonalExerciseList.add(personalExercise);
        mPersonalExerciseLiveData.setValue(mPersonalExerciseList);
    }

    public void removePersonalExercise(PersonalExercise personalExercise){
        mPersonalExerciseList.remove(personalExercise);
        mPersonalExerciseLiveData.setValue(mPersonalExerciseList);
    }

    public int getPersonalIndexOf(PersonalExercise personalExercise){
        return mPersonalExerciseList.indexOf(personalExercise);
    }

    public List<PersonalExercise> getPersonalExercise() {
        return mPersonalExerciseList;
    }

    public void setPersonalExerciseList(List<PersonalExercise> personalExerciseList) {
        this.mPersonalExerciseList = (ArrayList<PersonalExercise>) personalExerciseList;
        this.mPersonalExerciseLiveData.setValue(personalExerciseList);
    }

    public LiveData<List<PersonalExercise>> getPersonalExerciseListLiveData() {
        return mPersonalExerciseLiveData;
    }
}
