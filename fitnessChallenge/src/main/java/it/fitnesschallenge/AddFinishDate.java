package it.fitnesschallenge;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.fitnesschallenge.model.view.CreationViewModel;

import static android.app.Activity.RESULT_OK;
import static it.fitnesschallenge.model.SharedConstance.DATE_PICKER;
import static it.fitnesschallenge.model.SharedConstance.SELECTED_DATE;

public class AddFinishDate extends Fragment {

    private static final int DATE_PICKER_RESULT = 1;

    private TextView mDateText;
    private CreationViewModel mCreationViewModel;

    public AddFinishDate() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_finish_date, container, false);

        mCreationViewModel = ViewModelProviders.of(getActivity()).get(CreationViewModel.class);

        MaterialButton pickDateButton = view.findViewById(R.id.add_finish_date_pick_date);
        mDateText = view.findViewById(R.id.add_finish_date_date);

        mCreationViewModel.getFinishDate().observe(getViewLifecycleOwner(), new Observer<Date>() {
            @Override
            public void onChanged(Date date) {
                if (date != null)
                    mDateText.setText(new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(date));
            }
        });

        mCreationViewModel.getIsError().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    mDateText.setTextColor(Color.RED);
                    mDateText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error, 0);
                    mDateText.setText(R.string.complete_correctly_field);
                }
            }
        });

        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatDialogFragment appCompatDialogFragment = new DatePickerFragment();
                appCompatDialogFragment.setTargetFragment(AddFinishDate.this, DATE_PICKER_RESULT);
                appCompatDialogFragment.show(getActivity().getSupportFragmentManager(), DATE_PICKER);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DATE_PICKER_RESULT && resultCode == RESULT_OK) {
            Date date = (Date) data.getSerializableExtra(SELECTED_DATE);
            mCreationViewModel.setFinishDate(date);
            String dateFormat = new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault()).format(date);
            mDateText.setText(dateFormat);
        }
    }
}
