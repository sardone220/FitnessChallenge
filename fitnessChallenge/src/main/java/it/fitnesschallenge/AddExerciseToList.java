package it.fitnesschallenge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.fitnesschallenge.adapter.AddAdapter;
import it.fitnesschallenge.model.room.entity.Exercise;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.view.AddExerciseToListModel;
import it.fitnesschallenge.model.view.CreationViewModel;
import it.fitnesschallenge.model.view.EditListViewModel;

import static android.app.Activity.RESULT_OK;
import static it.fitnesschallenge.model.SharedConstance.CREATE_TRAINING_LIST;
import static it.fitnesschallenge.model.SharedConstance.DATE_PICKER;
import static it.fitnesschallenge.model.SharedConstance.EDIT_LIST_FRAGMENT;


public class AddExerciseToList extends Fragment {

    private static final String TAG = "AddExerciseToList";
    private static final int TIMER_PICKER_RESULT = 1;
    private static final String CALLER_FRAGMENT = "callerFragment";

    private List<Exercise> mExerciseList;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private AddExerciseToListModel mViewModel;
    private AddAdapter mAddAdapter;
    private List<PersonalExercise> mPersonalExerciseList;
    private String mCallerFragment;

    public AddExerciseToList() {
        // Required empty public constructor
    }

    public static AddExerciseToList newInstance(String callerFragment) {
        AddExerciseToList fragment = new AddExerciseToList();
        Bundle bundle = new Bundle();
        bundle.putString(CALLER_FRAGMENT, callerFragment);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCallerFragment = getArguments().getString(CALLER_FRAGMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_exercise_to_list, container, false);
        FloatingActionButton saveButton = view.findViewById(R.id.save_button_FAB);
        mRecyclerView = view.findViewById(R.id.adding_exercise_list);
        mViewModel = ViewModelProviders.of(getActivity()).get(AddExerciseToListModel.class);
        /*
         * Questo observer rimane in ascolto degli esercizi presenti nel DB, quando questi vengono resi
         * disponibili avvia l'inizializzazione della View con i relativi parametri e implementa il
         * click sul bottone di espansione e selezione
         */
        mViewModel.getExerciseList().observe(getViewLifecycleOwner(), new Observer<List<Exercise>>() {
            @Override
            public void onChanged(List<Exercise> exercises) {
                mExerciseList = exercises;
                mAddAdapter = new AddAdapter(mExerciseList);
                mRecyclerView.setAdapter(mAddAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                if (mAddAdapter != null) {
                    setOnClickListClickListener();
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Quando viene richiesto il salvataggio inizia il processo di controllo sui dati che
                 * permette di verificare se tutto è stato compilato in maniera corretta
                 */
                for (PersonalExercise personalExercise : mViewModel.getPersonalExercise())
                    checkPersonalExercise(personalExercise);
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
        return view;
    }

    private void setOnClickListClickListener() {
        mAddAdapter.setOnExpandClickListener(new AddAdapter.ExpandOnClickListener() {
            @Override
            public void onExpandListener(int finalHeight, int startHeight, View itemView, boolean expanded) {
                expandCardLayout(itemView, finalHeight, startHeight, expanded);
            }
        });
        mAddAdapter.setOnSelectedItemListener(new AddAdapter.OnSelectItemListener() {
            /*
             * Questo metodo verifica se era già stato effettuato l'inserimento di quell'
             * esercizio se non era stato già inserito viene fatto, altrimenti viene
             * eliminato dalla lista
             */
            @Override
            public void onSelectItemListener(View view, final int position) {
                addPersonalExercise(view, position);
            }
        });
        /*
         * Questo listener apre il dialog per settare il timer, il quale viene restutuito
         * in secondi in onActivityResult
         */
        mAddAdapter.setOnOpenTimerListener(new AddAdapter.OnOpenTimerListener() {
            @Override
            public void onOpenTimerListener(int position, View view) {
                AppCompatDialogFragment appCompatDialogFragment = new TimerPickerFragment(position, view);
                appCompatDialogFragment.setTargetFragment(AddExerciseToList.this, TIMER_PICKER_RESULT);
                appCompatDialogFragment.show(getActivity().getSupportFragmentManager(), DATE_PICKER);
            }
        });
    }

    private void addPersonalExercise(View view, int position) {
        int repetition = 0;
        int steps = 0;
        boolean selected = true;
        long coolDown = 0L;
        TextInputLayout repetitionText = view.findViewById(R.id.exercise_repetition);
        TextInputLayout seriesText = view.findViewById(R.id.exercise_series);
        MaterialCheckBox checkBox = view.findViewById(R.id.select_exercise_check);
        MaterialButton pickTime = view.findViewById(R.id.add_exercise_pick_time_button);
        try {
            repetition = Integer.parseInt(repetitionText.getEditText().getText().toString().trim());
            steps = Integer.parseInt(seriesText.getEditText().getText().toString().trim());
            selected = checkBox.isChecked();
            coolDown = Long.parseLong(pickTime.getText().toString().trim().replace("''", ""));
            if (repetition > 0 && steps > 0 && selected && coolDown > 0L) {
                mViewModel.addPersonalExercise(new PersonalExercise(
                        mAddAdapter.getItemAt(position).getExerciseId(),
                        steps, repetition, coolDown
                ));
            } else if (!selected) {
                mViewModel.removePersonalExercise(new PersonalExercise(mAddAdapter.getItemAt(position).getExerciseId()));
            } else {
                if (repetition == 0)
                    repetitionText.setError(getString(R.string.complete_correctly_field));
                if (steps == 0)
                    seriesText.setError(getString(R.string.complete_correctly_field));
            }
        } catch (NumberFormatException ex) {
            checkBox.setSelected(false);
            Toast.makeText(getContext(), "Play attention at field", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Questo metodo verifica che siano stati compilati i campi dell'esercizio prima di salvarlo
     * nel ViewModel di comunicazione, CreationViewModel
     *
     * @param personalExercise è l'esercizio prelevato da controllare
     */
    private void checkPersonalExercise(PersonalExercise personalExercise) {
        //Error tiene conto di eventuali campi non compilati
        if (mCallerFragment.equals(CREATE_TRAINING_LIST)) {
            CreationViewModel creationViewModel = ViewModelProviders.of(getActivity()).get(CreationViewModel.class);
            creationViewModel.setPersonalExerciseList(mViewModel.getPersonalExercise());
        } else if (mCallerFragment.equals(EDIT_LIST_FRAGMENT)) {
            EditListViewModel editListViewModel = ViewModelProviders.of(getActivity()).get(EditListViewModel.class);
            editListViewModel.setPersonalExerciseList(mViewModel.getPersonalExercise());
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        //Distruggo tutte le variabili del fragment
        super.onDetach();
        mContext = null;
    }

    /**
     * La sovrasctittura di onActivityResult permette di gestire il valore di ritorno da TimePickerFragmet
     *
     * @param requestCode contiene il codice di richiesta passato all'nuovo Intent: TIMER_PICKER_RESULT
     * @param resultCode  contiene il risultato dell'elaborzione nell'Intent
     * @param data        contiene i dati elaborati dall'intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == TIMER_PICKER_RESULT && resultCode == RESULT_OK) {
                Log.d(TAG, "Tempo iserito");
            }
        } catch (NullPointerException ex) {
            Toast.makeText(mContext, mContext.getString(R.string.shit_error), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    /**
     * Questo metodo gestisce l'animazione di espansione restringimento della card contente l'esercizo
     *
     * @param view        contiente la view da espandere/collassare
     * @param finalHeight contiene l'altezza finale della card dopo l'espansione
     * @param startHeight contiene l'altezza iniziale della card prima dell'espansione
     * @param expanded    permette di verificare se la card è espansa o meno
     */
    private void expandCardLayout(final View view, final int finalHeight, final int startHeight, final boolean expanded) {
        final View layoutView = view.findViewById(R.id.exercise_item);
        int duration = 300;
        final TextView description = view.findViewById(R.id.add_exercise_description);
        final ImageButton expandButton = view.findViewById(R.id.card_expander_collapse_arrow);
        ValueAnimator animator;
        //Value animator prende due int uno iniziale e uno finale, poi li avvicina incrementalmente
        if (expanded) {
            //Questa animazione va dall'altezza iniziale a quella finale per la view
            animator = ValueAnimator.ofInt(startHeight, finalHeight);
            expandButton.setImageResource(R.drawable.ic_keyboard_arrow_up);
            expandButton.setContentDescription("EXPANDED");
        } else {
            //Questa al contrario va dall'altezza finale a quella inizale della view
            animator = ValueAnimator.ofInt(finalHeight, startHeight);
            description.setVisibility(View.GONE);
            expandButton.setContentDescription("COLLAPSED");
            expandButton.setImageResource(R.drawable.ic_keyboard_arrow_down);
        }
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        //Questo Listener permette di aggiornare la view
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //Qui viene preso il valore dell'animazione
                int animationValue = (Integer) animation.getAnimatedValue();
                //Qui viene settato il nuovo valore sulla View
                ViewGroup.LayoutParams layoutParams = layoutView.getLayoutParams();
                layoutParams.height = animationValue;
                layoutView.setLayoutParams(layoutParams);
            }
        });
        /*
         * Alla fine dell'animazione viene richiamato questo metodo di call back che imposta la
         * visibilità del box di descrizione
         */
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (expanded)
                    description.setVisibility(View.VISIBLE);
            }
        });
        animator.start();
    }
}
