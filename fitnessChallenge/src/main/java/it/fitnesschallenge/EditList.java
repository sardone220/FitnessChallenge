
package it.fitnesschallenge;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import it.fitnesschallenge.adapter.ShowAdapter;
import it.fitnesschallenge.adapter.ShowAdapterDrag;
import it.fitnesschallenge.model.room.WorkoutType;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;
import it.fitnesschallenge.model.view.AddExerciseToListModel;
import it.fitnesschallenge.model.view.EditListViewModel;

import static it.fitnesschallenge.model.SharedConstance.ADD_EXERCISE_TO_LIST;
import static it.fitnesschallenge.model.SharedConstance.EDIT_LIST_FRAGMENT;

public class EditList extends Fragment {

    private static final String TAG = "EditList";

    private Context mContext;
    private EditListViewModel mViewModel;

    private RecyclerView mRecyclerView;
    private ShowAdapter mShowAdapter;
    private ProgressBar mProgressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_list, container, false);
        FloatingActionButton addFAB = view.findViewById(R.id.edit_list_fab_add);
        FloatingActionButton saveFAB = view.findViewById(R.id.edit_list_fab_save);
        mRecyclerView = view.findViewById(R.id.edit_list_recycler_view);
        mProgressBar = view.findViewById(R.id.edit_list_progress_bar);
        mViewModel = ViewModelProviders.of(getActivity()).get(EditListViewModel.class);

        if (mViewModel.isFirstExecution()) {
            mViewModel.setFirstExecution(false);
            mViewModel.getWorkoutList().observe(getViewLifecycleOwner(), new Observer<List<Workout>>() {
                @Override
                public void onChanged(List<Workout> workoutList) {
                    selectOutdoorWorkout(workoutList);
                }
            });
        }

        mViewModel.getPersonalExerciseList().observe(getViewLifecycleOwner(), new Observer<List<PersonalExercise>>() {
            @Override
            public void onChanged(List<PersonalExercise> personalExerciseList) {
                Log.d(TAG, "Esercizi inseriti.");
                Log.d(TAG, "Ottenuta lista esercizi personale: " + personalExerciseList.toString());
                mShowAdapter = new ShowAdapter(cloneList(personalExerciseList),
                        getActivity().getApplication(), getViewLifecycleOwner());
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                mRecyclerView.setAdapter(mShowAdapter);
                ItemTouchHelper.Callback callback = new ShowAdapterDrag(mShowAdapter);
                ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                touchHelper.attachToRecyclerView(mRecyclerView);
                mShowAdapter.setOnClickListener(new ShowAdapter.OnClickListener() {
                    @Override
                    public void onClickListener(View view, int position) {
                        ImageButton removeButton = view.findViewById(R.id.exercise_item_action);
                        if (!mShowAdapter.getAdapterList().get(position).isDeleted()) {
                            removeButton.setImageResource(R.drawable.ic_undo);
                            Log.d(TAG, "Rimuovo esercizio");
                            mShowAdapter.getAdapterList().get(position).setDeleted(true);
                            mViewModel.deleteExerciseFromList(mShowAdapter.getAdapterList().get(position));
                        } else {
                            removeButton.setImageResource(R.drawable.ic_remove_circle);
                            Log.d(TAG, "Riaggiungo esercizio");
                            mShowAdapter.getAdapterList().get(position).setDeleted(false);
                            mViewModel.reAddExerciseToList(mShowAdapter.getAdapterList().get(position));
                        }
                        mShowAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShowAdapter != null) {
                    AddExerciseToListModel addExerciseToListModel = ViewModelProviders.of(getActivity()).get(AddExerciseToListModel.class);
                    addExerciseToListModel.setPersonalExerciseList(mShowAdapter.getAdapterList());
                }
                AddExerciseToList addExerciseToList = AddExerciseToList.newInstance(EDIT_LIST_FRAGMENT);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                        .replace(R.id.fragmentContainer, addExerciseToList, ADD_EXERCISE_TO_LIST)
                        .addToBackStack(ADD_EXERCISE_TO_LIST)
                        .commit();
            }
        });

        saveFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                if (mViewModel.getWorkout() != null) {
                    Log.d(TAG, "Aggiorno il workout");
                    mViewModel.addNewExerciseToWorkout().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean aBoolean) {
                            mViewModel.deleteExerciseFromWorkout().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                                @Override
                                public void onChanged(Boolean aBoolean) {
                                    if (aBoolean) {
                                        mProgressBar.setVisibility(View.GONE);
                                        showFinishDialog();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    mViewModel.addWorkoutWithExercise().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean aBoolean) {
                            if (aBoolean == true) {
                                mProgressBar.setVisibility(View.GONE);
                                showFinishDialog();
                            }
                        }
                    });
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void showFinishDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.success)
                .setMessage(R.string.saving_complete)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    }
                });
        builder.show();
    }

    private void selectOutdoorWorkout(List<Workout> workoutList) {
        for (Workout workout : workoutList) {
            if (workout.getWorkoutType().getValue().equals(WorkoutType.OUTDOOR.getValue())) {
                Log.d(TAG, "Outdoor workout trovato");
                mViewModel.setWorkout(workout);
                getWorkoutWithExercise(workout.getWorkOutId());
            } else {
                Log.d(TAG, "Trovato workout di tipo indoor");
            }
        }
    }

    private void getWorkoutWithExercise(final long workoutId) {
        mViewModel.getSavedExerciseList(workoutId).observe(getViewLifecycleOwner(), new Observer<WorkoutWithExercise>() {
            @Override
            public void onChanged(WorkoutWithExercise workoutWithExercise) {
                Log.d(TAG, "Prelevato workout con esercizi");
                mViewModel.setPersonalExerciseList(workoutWithExercise.getPersonalExerciseList());
            }
        });
    }

    private List<PersonalExercise> cloneList(List<PersonalExercise> list) {
        List<PersonalExercise> cloneList = new ArrayList<>();
        for (PersonalExercise exercise : list) {
            cloneList.add((PersonalExercise) exercise.clone());
        }
        return cloneList;
    }
}
