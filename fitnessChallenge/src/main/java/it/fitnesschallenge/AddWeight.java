/**
 * Questo fragment consente di inserirei i pesi utilizzati nell'allenameto.
 * Utilizza una RecyclerView che viene impostata a seconda del numero di ripetizioni impostate alla
 * creazione dell'esercizio da parte del trainer.
 */
package it.fitnesschallenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import it.fitnesschallenge.adapter.WeightInputAdapter;
import it.fitnesschallenge.model.room.entity.ExerciseExecution;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.view.PlayingWorkoutModelView;

public class AddWeight extends Fragment {

    private static final String TAG = "AddWeight";

    private RecyclerView mWeightRecyclerView;
    private TextView mLastTime;
    private MaterialButton mSaveButton;
    private PlayingWorkoutModelView mViewModel;
    private PersonalExercise mCurrentExercise;
    private WeightInputAdapter mAdapter;

    public AddWeight() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_weight, container, false);
        mWeightRecyclerView = view.findViewById(R.id.add_weight_weight_recycler_view);
        mLastTime = view.findViewById(R.id.add_weight_last_time);
        mSaveButton = view.findViewById(R.id.add_weight_save_button);
        mViewModel = ViewModelProviders.of(getActivity()).get(PlayingWorkoutModelView.class);
        mViewModel.getExerciseExecution().observe(getViewLifecycleOwner(), new Observer<ExerciseExecution>() {
            @Override
            public void onChanged(ExerciseExecution exerciseExecution) {
                if (exerciseExecution != null)
                    mLastTime.setText(new SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault()).format(
                            exerciseExecution.getExecutionDate()
                    ));
                else mLastTime.setText(R.string.no_past_execution);
                ArrayList<Float> arrayList = new ArrayList<>();
                for (int i = 0; i < mViewModel.getCurrentExercise().getSteps(); i++) {
                    arrayList.add(0.00F);
                }
                mAdapter = new WeightInputAdapter(arrayList, getContext());
                mWeightRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mWeightRecyclerView.setAdapter(mAdapter);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExecution();
            }
        });

        return view;
    }

    /**
     * Questo metodo salva l'esecuzione del workout in locale. Prendendo la data dal sistema.
     */
    private void saveExecution() {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        ExerciseExecution exerciseExecution = new ExerciseExecution(currentDate, mAdapter.getRecyclerValue(), mViewModel.getCurrentExercise().getExerciseId());
        mViewModel.writeExerciseExecutionRoom(exerciseExecution);
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }
}
