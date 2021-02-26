package it.fitnesschallenge;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

import it.fitnesschallenge.timepicker.OnTimerSetListener;
import it.fitnesschallenge.timepicker.TimerPickerDialog;

import static android.app.Activity.RESULT_OK;
import static it.fitnesschallenge.model.SharedConstance.POSITION_IN_ADAPTER;
import static it.fitnesschallenge.model.SharedConstance.SELECTED_TIMER;

public class TimerPickerFragment extends AppCompatDialogFragment {

    private static final String TAG = "TimerPickerFragment";

    private int mPosition;
    private View mView;

    public TimerPickerFragment(int position, View mView) {
        this.mPosition = position;
        this.mView = mView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new TimerPickerDialog(getActivity(), new OnTimerSetListener() {
            @Override
            public void onTimerSetListener(View view, int hours, int minutes, int seconds) {
                MaterialButton addTime = (MaterialButton) mView;
                addTime.setText(NumberFormat.getInstance(Locale.getDefault()).format(getTimeInSeconds(hours, minutes, seconds)));
                Intent intent = new Intent();
                intent.putExtra(SELECTED_TIMER, getTimeInSeconds(hours, minutes, seconds));
                intent.putExtra(POSITION_IN_ADAPTER, mPosition);
                Log.d(TAG, "Postizione nell'adapter: " + mPosition);
                getTargetFragment().onActivityResult(getTargetRequestCode(),
                        RESULT_OK,
                        intent);
            }
        });
    }

    private Long getTimeInSeconds(int hours, int minutes, int seconds) {
        return (long) (hours * 3600 + minutes * 60 + seconds);
    }
}
