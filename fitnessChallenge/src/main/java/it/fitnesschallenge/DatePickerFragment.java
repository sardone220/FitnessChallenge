package it.fitnesschallenge;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static it.fitnesschallenge.model.SharedConstance.SELECTED_DATE;


public class DatePickerFragment extends AppCompatDialogFragment {

    private static final String TAG = "DatePickerFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Log.d(TAG, "Day: " + dayOfMonth);
                Date date = calendar.getTime();
                Log.d(TAG, "Date picked: " +
                        new SimpleDateFormat("YYYY-MM-dd", Locale.US)
                                .format(date));
                getTargetFragment().onActivityResult(getTargetRequestCode(),
                        RESULT_OK,
                        new Intent().putExtra(SELECTED_DATE, date));
            }
        }, year, month, day);
    }
}
