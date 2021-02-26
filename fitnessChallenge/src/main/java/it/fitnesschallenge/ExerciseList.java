/**
 * Questo fragment permette, durante la creazione della scheda di allenamento di visualizzare gli
 * esercizi inseriti, cambiarnel l'ordine e permette di rimuoverli se la selezione non è più necessaria.
 */
package it.fitnesschallenge;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import it.fitnesschallenge.adapter.ShowAdapter;
import it.fitnesschallenge.adapter.ShowAdapterDrag;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.view.CreationViewModel;


public class ExerciseList extends Fragment {

    private CreationViewModel mCreationViewModel;
    private ShowAdapter mShowAdapter;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<PersonalExercise> mActualList;
    private static final String TAG = "ExerciseList";


    public ExerciseList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_execise_list, container, false);

        mRecyclerView = view.findViewById(R.id.show_exercise_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mCreationViewModel = ViewModelProviders.of(getActivity()).get(CreationViewModel.class);
        mCreationViewModel.getPersonalExerciseList().observe(getViewLifecycleOwner(), new Observer<List<PersonalExercise>>() {
            @Override
            public void onChanged(final List<PersonalExercise> personalExerciseList) {
                Log.d(TAG, "Ottenuta lista esercizi personale: " + personalExerciseList.toString());
                mActualList = personalExerciseList;
                mShowAdapter = new ShowAdapter(mActualList, getActivity().getApplication(), getViewLifecycleOwner());
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                mRecyclerView.setAdapter(mShowAdapter);
                ItemTouchHelper.Callback callback = new ShowAdapterDrag(mShowAdapter);
                ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                touchHelper.attachToRecyclerView(mRecyclerView);
                mShowAdapter.setOnClickListener(new ShowAdapter.OnClickListener() {
                    @Override
                    public void onClickListener(View view, int position) {
                        ImageButton removeButton = view.findViewById(R.id.exercise_item_action);
                        Log.d(TAG, "E' rimosso: " + mActualList.get(position).isDeleted());
                        if (!mActualList.get(position).isDeleted()) {
                            removeButton.setImageResource(R.drawable.ic_undo);
                            Log.d(TAG, "Rimuovo esercizio");
                            mActualList.get(position).setDeleted(true);
                        } else {
                            removeButton.setImageResource(R.drawable.ic_remove_circle);
                            Log.d(TAG, "Riaggiungo esercizio");
                            mActualList.get(position).setDeleted(false);
                        }
                        mCreationViewModel.setPersonalExerciseList(mActualList);
                    }
                });
            }
        });
        mCreationViewModel.getIsError().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.d(TAG, "Error status changed: " + aBoolean);
                if (aBoolean)
                    Snackbar.make(getView(), getContext()
                            .getResources()
                            .getString(R.string.add_exercise_to_list), Snackbar.LENGTH_LONG).show();
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
