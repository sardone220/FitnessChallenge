package it.fitnesschallenge;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.fitnesschallenge.model.room.Converter;
import it.fitnesschallenge.model.room.WorkoutType;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;
import it.fitnesschallenge.model.view.CreationViewModel;

public class UploadNewWorkout extends Fragment {

    private static final String TAG = "UploadNewWorkout";

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private CreationViewModel mCreationViewModel;
    private FirebaseFirestore mDatabase;
    private Context mContext;
    private ProgressBar mProgressBar;

    public UploadNewWorkout() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_upload_new_workout, container, false);
        MaterialButton uploadButton = view.findViewById(R.id.upload_new_work_out_finish_button);
        mProgressBar = view.findViewById(R.id.upload_new_work_out_progressbar);

        mCreationViewModel = ViewModelProviders.of(getActivity()).get(CreationViewModel.class);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserExist();
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void checkUserExist() {
        mDatabase = FirebaseFirestore.getInstance();
        Log.d(TAG, "Email utente: " + mCreationViewModel.getEmail().getValue());
        try {
            DocumentReference document = mDatabase.collection("user").document(mCreationViewModel.getEmail().getValue());
            document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.d(TAG, "La ricerca ha avuto succsso");
                    uploadNewWorkout();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Ricerca utente fallita: " + e.getMessage());
                            Snackbar.make(getView(),
                                    mContext.getResources()
                                            .getString(R.string.user_does_not_exist_select_another_one),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
        } catch (NullPointerException ex) {
            Log.d(TAG, "La mail non è stata inserita");
        }
    }

    private void uploadNewWorkout() {
        //Questa conversione è necessaria per non inviare su firebase una data inquinata dall'orario di upload
        Converter converter = new Converter();
        Date startDate = converter.stringToDate(converter.dateToString(mCreationViewModel.getStartDate().getValue()));
        Date endDate = converter.stringToDate(converter.dateToString(mCreationViewModel.getFinishDate().getValue()));
        mProgressBar.setVisibility(View.VISIBLE);
        WorkoutWithExercise workoutWithExercise = new WorkoutWithExercise();
        workoutWithExercise.setWorkout(new Workout(true,
                startDate,
                endDate,
                WorkoutType.INDOOR));
        workoutWithExercise.setPersonalExerciseList(mCreationViewModel.getPersonalExerciseList().getValue());
        mDatabase.collection("user")
                .document(mCreationViewModel.getEmail().getValue())
                .collection("workout")
                .document(new SimpleDateFormat("YYYY-MM-dd", Locale.getDefault())
                        .format(mCreationViewModel.getStartDate().getValue()))
                .set(workoutWithExercise)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar.setVisibility(View.GONE);
                        Snackbar.make(getView(), "Workout uploaded correctly", Snackbar.LENGTH_LONG).show();
                        SystemClock.sleep(2000);
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Errore upload: " + e.getMessage());
                    }
                });
    }
}
