package it.fitnesschallenge;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.fitnesschallenge.model.view.CreationViewModel;

import static android.app.Activity.RESULT_OK;
import static it.fitnesschallenge.model.SharedConstance.DATE_PICKER;
import static it.fitnesschallenge.model.SharedConstance.SELECTED_DATE;

public class AddUserNameAndStartDate extends Fragment {

    private static String[] goals;
    private static final String TAG = "AddUserNameAndStartDate";
    private static final int DATE_PICKER_RESULT = 1;
    private CreationViewModel mCreationViewModel;
    private TextView mDateText;
    private TextInputLayout mEmail;
    private TextInputLayout mGoal;

    public AddUserNameAndStartDate() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_username_and_start_date, container, false);

        mEmail = view.findViewById(R.id.first_step_creation_text_input);
        mGoal = view.findViewById(R.id.first_step_creation_training_goal);
        Button pickDate = view.findViewById(R.id.add_username_and_start_date_pick_date);
        mDateText = view.findViewById(R.id.first_step_creation_date);
        AutoCompleteTextView dropDownGoals = view.findViewById(R.id.dropdown_goal);
        goals = new String[]  {getContext().getString(R.string.gain_muscle),
                getContext().getString(R.string.get_fit)};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.drop_down_single_layout,
                goals);
        dropDownGoals.setAdapter(adapter);

        mCreationViewModel = ViewModelProviders.of(getActivity()).get(CreationViewModel.class);
        mCreationViewModel.getEmail().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null)
                    mEmail.getEditText().setText(s);
            }
        });
        mCreationViewModel.getGoal().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null) {
                    mGoal.getEditText().setText(s);
                }
            }
        });
        mCreationViewModel.getStartDate().observe(getViewLifecycleOwner(), new Observer<Date>() {
            @Override
            public void onChanged(Date date) {
                if (date != null)
                    mDateText.setText(new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(date));
            }
        });

        mCreationViewModel.getIsError().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.d(TAG, "Error status changed: " + aBoolean);
                if (aBoolean != null) {
                    if (aBoolean)
                        checkEmptyValue();
                }
            }
        });

        mEmail.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TextInputEditText view = (TextInputEditText) v;
                Log.d(TAG, "Email onFocusChange: " + view.getText().toString());
                mCreationViewModel.setEmail(view.getText().toString().trim());
                if (KeyboardVisibilityEvent.isKeyboardVisible(getActivity())) {
                    try {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    } catch (NullPointerException ex) {
                        Log.d(TAG, "Non riesco a nascondere la tastiera");
                    }
                }
            }
        });

        mGoal.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                AutoCompleteTextView view = (AutoCompleteTextView) v;
                Log.d(TAG, "Goal onFocusChange: " + view.getText().toString());
                mCreationViewModel.setGoal(view.getText().toString().trim());
            }
        });

        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatDialogFragment appCompatDialogFragment = new DatePickerFragment();
                appCompatDialogFragment.setTargetFragment(AddUserNameAndStartDate.this, DATE_PICKER_RESULT);
                appCompatDialogFragment.show(getActivity().getSupportFragmentManager(), DATE_PICKER);
            }
        });
        return view;
    }

    /**
     * Questo metodo di callback viene richiamto dal DatePicker dopo aver selezionato una data di
     * inizio per il workout
     *
     * @param requestCode contiene il codice di richiesta passato per il picker
     * @param resultCode  contiene il codice risultato, ovvero OK, ERROR, FAIL
     * @param data        contiene la data prelevata dal picker.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == DATE_PICKER_RESULT && resultCode == RESULT_OK){
            Date date = (Date) data.getSerializableExtra(SELECTED_DATE);
            mCreationViewModel.setStartDate(date);
            mDateText.setText(new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(date));
        }
    }

    /**
     * Questo metodo verifica che i campi siano stai compilati completamente.
     */
    private void checkEmptyValue() {
        if (mEmail.getEditText().getText().toString().isEmpty())
            mEmail.setError(getContext().getResources().getString(R.string.complete_correctly_field));
        if (mGoal.getEditText().getText().toString().isEmpty())
            mGoal.setError(getContext().getResources().getString(R.string.complete_correctly_field));
        if (mDateText.getText().equals(getContext().getResources().getString(R.string.date_pattern))) {
            mDateText.setTextColor(Color.RED);
            mDateText.setText(getContext().getResources().getString(R.string.complete_correctly_field));
            mDateText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
        }
    }
}
