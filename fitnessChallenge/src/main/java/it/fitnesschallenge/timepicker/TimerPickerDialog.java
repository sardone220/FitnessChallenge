package it.fitnesschallenge.timepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import it.fitnesschallenge.R;

public class TimerPickerDialog extends AlertDialog {

    private static final String TAG = "TimerPickerDialog";

    private OnTimerSetListener mOnTimerSetListener;
    private int hours;
    private int minutes;
    private int seconds;

    public TimerPickerDialog(Context context, OnTimerSetListener onTimerSetListener) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.timer_picker_dialog, null);
        setView(view);
        MaterialButton mCancelButton = view.findViewById(R.id.time_picker_negative_button);
        MaterialButton mOkButton = view.findViewById(R.id.timer_picker_positive_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnTimerSetListener != null) {
                    Log.d(TAG, "Setto i valori corretti");
                    TextInputLayout hoursLayout = view.findViewById(R.id.timer_picker_hours);
                    TextInputLayout minutesLayout = view.findViewById(R.id.timer_picker_minutes);
                    TextInputLayout secondsLayout = view.findViewById(R.id.timer_picker_seconds);
                    hours = Integer.parseInt(hoursLayout.getEditText().getText().toString());
                    minutes = Integer.parseInt(minutesLayout.getEditText().getText().toString());
                    seconds = Integer.parseInt(secondsLayout.getEditText().getText().toString());
                    mOnTimerSetListener.onTimerSetListener(view, hours, minutes, seconds);
                }
                cancel();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        this.mOnTimerSetListener = onTimerSetListener;
    }
}
