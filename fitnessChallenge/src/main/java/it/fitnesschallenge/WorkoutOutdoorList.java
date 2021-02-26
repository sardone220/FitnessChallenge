package it.fitnesschallenge;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import it.fitnesschallenge.adapter.ShowAdapter;
import it.fitnesschallenge.model.room.WorkoutType;
import it.fitnesschallenge.model.room.entity.Exercise;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;
import it.fitnesschallenge.model.view.PlayingWorkoutModelView;

import static it.fitnesschallenge.model.SharedConstance.EDIT_LIST_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.PLAYING_WORKOUT;
import static it.fitnesschallenge.model.SharedConstance.SIGN_UP_FRAGMENT;

public class WorkoutOutdoorList extends Fragment {

    private static final String TAG = "WorkoutOutdoorList";

    private RecyclerView mRecyclerView;
    private ShowAdapter mShowAdapter;
    private PlayingWorkoutModelView mViewModel;

    public WorkoutOutdoorList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_outdoor_list, container, false);
        mRecyclerView = view.findViewById(R.id.outdoor_workout_recycler);
        mViewModel = ViewModelProviders.of(getActivity()).get(PlayingWorkoutModelView.class);

        com.github.clans.fab.FloatingActionMenu menuFab = view.findViewById(R.id.outdoor_workout_list_fab_menu);
        menuFab.setVisibility(View.VISIBLE);
        com.github.clans.fab.FloatingActionButton editFab = view.findViewById(R.id.outdoor_workout_list_fab_edit);
        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditList editList = new EditList();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                transaction.replace(R.id.fragmentContainer, editList, EDIT_LIST_FRAGMENT)
                        .addToBackStack(EDIT_LIST_FRAGMENT)
                        .commit();
            }
        });

        com.github.clans.fab.FloatingActionButton fab2 = view.findViewById(R.id.outdoor_workout_list_fab_play);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayingWorkout playingWorkout = new PlayingWorkout();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                transaction.replace(R.id.fragmentContainer, playingWorkout, PLAYING_WORKOUT)
                        .addToBackStack(PLAYING_WORKOUT)
                        .commit();
            }
        });

        mViewModel.getWorkout().observe(getViewLifecycleOwner(), new Observer<List<Workout>>() {
            @Override
            public void onChanged(List<Workout> workoutList) {
                checkWorkoutInLocalDB(workoutList);
            }
        });

        mViewModel.getWorkoutId().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                setCurrentWorkout(aLong);
            }
        });

        return view;
    }

    private void setCurrentWorkout(Long workoutId) {
        if (workoutId != -1) {
            Log.d(TAG, "Workout settatto.");
            mViewModel.getExerciseListLiveData().observe(getViewLifecycleOwner(), new Observer<List<Exercise>>() {
                @Override
                public void onChanged(List<Exercise> exercises) {
                    Log.d(TAG, "Settata lista esercizi.");
                    mViewModel.setExerciseList(exercises);
                }
            });
            mViewModel.getWorkoutWithExercise().observe(getViewLifecycleOwner(), new Observer<WorkoutWithExercise>() {
                @Override
                public void onChanged(WorkoutWithExercise workoutWithExercise) {
                    setUI(workoutWithExercise);
                }
            });
        }
    }

    private void checkWorkoutInLocalDB(List<Workout> workoutList) {
        boolean found = false;
        for (int i = 0; i < workoutList.size() && !found; i++) {
            Workout workout = workoutList.get(i);
            Log.d(TAG, "workout[" + i + "]: " + workout.getStartDate());
            if (workout.getWorkoutType().equals(WorkoutType.OUTDOOR)) {
                mViewModel.setWorkoutId(workout.getWorkOutId());
                found = true;
            }
        }
        if (!found)
            errorDialog(R.string.no_active_workout);
    }

    /**
     * Questo metodo mostra dei dialog di errore.
     *
     * @param message contiene il messaggio da visualizzare.
     */
    private void errorDialog(int message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.ops)
                .setMessage(message);
        final FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
        if (message == R.string.registration_error) {
            builder.setNegativeButton(R.string.sign_in, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SignUp signUp = new SignUp();
                    transaction.replace(R.id.fragmentContainer, signUp, SIGN_UP_FRAGMENT)
                            .addToBackStack(SIGN_UP_FRAGMENT)
                            .commit();
                }
            });
        }
        builder.show();
    }

    /**
     * Questo metodo permette di settare il layout sulla recycler view, e il relativo contenuto.
     *
     * @param workoutWithExercise contiene il workout e la lista degli esercizi da eseguire.
     */
    private void setUI(WorkoutWithExercise workoutWithExercise) {
        Log.d(TAG, "Observer di WorkoutWithExercise");
        mViewModel.setWorkoutWithExercise(workoutWithExercise);
        Log.d(TAG, "WorkoutWithExercise: " + workoutWithExercise.getPersonalExerciseList().toString());
        mViewModel.setPersonalExerciseList((ArrayList<PersonalExercise>) workoutWithExercise.getPersonalExerciseList());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mShowAdapter = new ShowAdapter(mViewModel.getPersonalExerciseList(), getActivity().getApplication(), getViewLifecycleOwner());
        mRecyclerView.setAdapter(mShowAdapter);
    }
}
