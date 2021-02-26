package it.fitnesschallenge.adapter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import it.fitnesschallenge.R;

public class RegistrationCompleteBottomSheet extends BottomSheetDialogFragment {

    private MutableLiveData<Boolean> mSuccess;
    private ProgressBar mProgressBar;
    private ImageView mImageView;
    private TextView mMessage;

    public RegistrationCompleteBottomSheet() {
        mSuccess = new MutableLiveData<>();
    }

    public void setSuccess(boolean aBoolean) {
        mSuccess.setValue(aBoolean);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.registration_complete_bottom_sheet, container, false);
        ImageButton closeButton = view.findViewById(R.id.registration_close_sheet);
        mProgressBar = view.findViewById(R.id.registration_progress_bar);
        mImageView = view.findViewById(R.id.registration_image_success);
        mMessage = view.findViewById(R.id.registration_label);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mSuccess.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    mProgressBar.setVisibility(View.GONE);
                    mImageView.setVisibility(View.VISIBLE);
                    mMessage.setText(R.string.subscribing_complete);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                    mMessage.setText(R.string.subscribing_error);
                    mMessage.setTextColor(Color.RED);
                }
            }
        });

        return view;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}
