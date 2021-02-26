package it.fitnesschallenge.adapter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import it.fitnesschallenge.R;
import it.fitnesschallenge.model.ExecutionList;
import it.fitnesschallenge.model.room.entity.ExerciseExecution;

public class SharingBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "SharingBottomSheet";

    private TextView mSharingPoint;
    private TextView mSharingLabel;
    private ProgressBar mProgressBar;
    private FirebaseFirestore mDatabase;
    private FirebaseUser mUser;
    private ImageView mSuccessImage;
    private MutableLiveData<Float> mExecutionAvg;
    private MutableLiveData<Float> mWorkoutAvg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        mDatabase = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mSharingLabel = view.findViewById(R.id.registration_label);
        mSharingPoint = view.findViewById(R.id.shared_point);
        mProgressBar = view.findViewById(R.id.registration_progress_bar);
        mSuccessImage = view.findViewById(R.id.registration_image_success);
        ImageView mCloseSheet = view.findViewById(R.id.registration_close_sheet);

        mExecutionAvg = new MutableLiveData<>(0.00F);
        mWorkoutAvg = new MutableLiveData<>(0.00F);

        mCloseSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mDatabase.collection("user/").document(mUser.getEmail()).collection("workout")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                for (final DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    getWorkoutAvg(documentSnapshot);
                }
                mWorkoutAvg.observe(getViewLifecycleOwner(), new Observer<Float>() {
                    @Override
                    public void onChanged(Float aFloat) {
                        Log.d(TAG, "Media tra tutti i workout: " + aFloat);
                        mSharingPoint.setText(NumberFormat.getInstance(Locale.getDefault())
                                .format(aFloat));
                        saveSharingPointOnFireBase(aFloat / queryDocumentSnapshots.getDocuments().size());
                    }
                });
            }
        });

        return view;
    }

    private void getWorkoutAvg(DocumentSnapshot workoutSnapshot) {
        mDatabase.collection("user").document(mUser.getEmail()).collection("workout")
                .document(workoutSnapshot.getId()).collection("execution").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                        for (final DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Log.d(TAG, "Workout: " + documentSnapshot.getId());
                            getExecutionAvg(documentSnapshot);
                        }

                        mExecutionAvg.observe(getViewLifecycleOwner(), new Observer<Float>() {
                            @Override
                            public void onChanged(Float aFloat) {
                                Log.d(TAG, "Media di esecuzione: " + aFloat / queryDocumentSnapshots.getDocuments().size());
                                mWorkoutAvg.setValue(aFloat / queryDocumentSnapshots.getDocuments().size());
                            }
                        });
                    }
                });
    }

    private void getExecutionAvg(DocumentSnapshot executionSnapshot) {
        float tempExecution = 0.00F;
        ExecutionList executionList = executionSnapshot.toObject(ExecutionList.class);
        for (ExerciseExecution execution : executionList.getExerciseList()) {
            tempExecution += getExerciseAvg(execution);
        }
        mExecutionAvg.setValue(mExecutionAvg.getValue() + (tempExecution / executionList.getExerciseList().size()));
    }

    private float getExerciseAvg(ExerciseExecution execution) {
        float exerciseAVG = 0.00F;
        List<Float> usedKilograms = execution.getUsedKilograms();
        for (Float value : usedKilograms) {
            exerciseAVG += value;
        }
        return exerciseAVG / usedKilograms.size();
    }

    private void saveSharingPointOnFireBase(float avg) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("SharedPoint", avg);
        mDatabase.collection("user").document(mUser.getEmail())
                .collection("sharedValue").document("value").set(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mSharingLabel.setText(R.string.progress_saved);
                        mSuccessImage.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}
