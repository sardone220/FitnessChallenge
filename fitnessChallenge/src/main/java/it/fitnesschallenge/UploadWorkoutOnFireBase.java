/**
 * Questa classe sincronizza i dati dell'esecuzione del workout con il database Firebase.
 * Per far ciò prima preleva l'ultima esecuzione inserita nel DB locale, dopo di che crea il percorso
 * di collezioni e documenti nesessario a salvare i dati nel DB in firestore:
 * user > workout > DataInizioWorkout > execution > DataEsecuzioneWorkout > ExerciseList > POJO.
 * Inoltre la classe utilizza un Handler che mostra il progresso dell'upload del workout.
 */
package it.fitnesschallenge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.fitnesschallenge.model.ExecutionList;
import it.fitnesschallenge.model.room.entity.ExerciseExecution;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;
import it.fitnesschallenge.model.view.PlayingWorkoutModelView;

public class UploadWorkoutOnFireBase extends Fragment {

    private static final String TAG = "UploadWorkoutOnFireBase";
    private static final int MSG_UPDATE_PROGRESS = 0;
    private static final int MSG_FINISH_PROGRESS = 1;
    private static final int MSG_CLOSE_FRAGMENT = 2;

    private PlayingWorkoutModelView mPlayWorkoutViewModel;
    private Workout mWorkout;
    private ProgressBar mProgressBar;
    private TextView mPercent;
    private List<ExerciseExecution> mLastWorkout;
    private TextView mUploadMessage;
    private Handler handler = new UIUpdateHandler(this);

    public UploadWorkoutOnFireBase() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_upload_workout_on_fire_base, container, false);
        mProgressBar = view.findViewById(R.id.upload_workout_progress_bar);
        mPercent = view.findViewById(R.id.upload_workout_percent);
        mUploadMessage = view.findViewById(R.id.uploading_workout_please_label);

        mPlayWorkoutViewModel = ViewModelProviders.of(getActivity()).get(PlayingWorkoutModelView.class);
        /*
         * Qui viene prelevato il workout che l'utente sta eseguendo, dopo di che viene richiamanta getLastExecutionFormDB().
         */
        mPlayWorkoutViewModel.getWorkoutWithExercise().observe(getViewLifecycleOwner(), new Observer<WorkoutWithExercise>() {
            @Override
            public void onChanged(WorkoutWithExercise workoutWithExercise) {
                mWorkout = workoutWithExercise.getWorkout();
                Log.d(TAG, "Prelevato workout dal DB: " + mWorkout.getStartDate().toString());
                getLastExecutionFromDB();
            }
        });

        return view;
    }

    /**
     * Questo metodo preleva l'ultima esecuzione per il workout dal DB locale, se l'esecuzione contiene
     * dati allora richiamo prima di tutto l'handler per mostrare l'aggiornamento, e poi richiamo uploadOnFireBase.
     */
    private void getLastExecutionFromDB() {
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        mPlayWorkoutViewModel.getLastExecution(calendar.getTime()).observe(getViewLifecycleOwner(),
                new Observer<List<ExerciseExecution>>() {
                    @Override
                    public void onChanged(List<ExerciseExecution> exerciseExecutions) {
                        Log.d(TAG, "Prelevata ultima esecuzione");
                        mLastWorkout = exerciseExecutions;
                        if (exerciseExecutions.size() > 0) {
                            handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
                            uploadOnFireBase(calendar.getTime());
                        } else {
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                                    .setTitle(getString(R.string.ops))
                                    .setMessage("It seems that you have not entered the training data, go back in the execution to insert them.");
                            builder.show();
                        }
                    }
                });
    }

    /**
     * Questo metodo inivia i dati al DB Firebase cosi che possano essere salvati nella giusta raccolta.
     *
     * @param execution contiene la data prelevata dal sistema, quindi l'esecuzione.
     */
    private void uploadOnFireBase(Date execution) {
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        Log.d(TAG, "Workout date: " + mWorkout.getStartDate().toString());
        Log.d(TAG, "Execution date: " + execution.toString());
        mDatabase.collection("user").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .collection("workout").document(
                new SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault())
                        .format(mWorkout.getStartDate())
        ).collection("execution").document(new SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault())
                .format(execution)).set(new ExecutionList(mLastWorkout))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        handler.sendEmptyMessage(MSG_FINISH_PROGRESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handler.sendEmptyMessage(MSG_FINISH_PROGRESS);
                    }
                });
    }

    /**
     * Questo metodo viene richiamato dall'handler e setta il progress sia sulla progress bar che nella
     * TextView che contiene la percentuale di aggiornamento.
     *
     * @param update contiene il valore dell'aggiornameto.
     */
    private void onProgressUpdate(int update) {
        Log.d(TAG, "Valore da aggiungere: " + update);
        Log.d(TAG, "Valore progress bar: " + mProgressBar.getProgress());
        ValueAnimator animator = ValueAnimator.ofInt(mProgressBar.getProgress(), update);
        animator.setDuration(90)
                .setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                mProgressBar.setProgress(progress);
                mPercent.setText(NumberFormat.getInstance().format(progress));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.d(TAG, "Animazione finita, varlore progressbar: " + mProgressBar.getProgress());
            }
        });
        animator.start();
    }

    /**
     * Questo metodo chiude contemporaneamente due Fragment, ovvero questo per l'upload e quello dell'
     * esecuzione del workout.
     */
    private void closeFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 2).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /**
     * Questa classe estende Handler e crea il riferimanto con il Fragment UploadWorkoutOnFireBase
     * permette di mostrare un progresso "fittizio", che incrementa finchè l'upload non viene terminato,
     * quando l'upload termina l'aggiornamento della progress bar carica completamente.
     * Dopo di che l'handler attente 2 sec e chiude i due fragment.
     */
    public static class UIUpdateHandler extends Handler {

        private static final int UPDATE_RATE_MS = 100;
        private static int mProgress = 0;
        private static final int MAX_PROGRESS = 100;
        private static boolean mIsUpdating = true;

        private final WeakReference<UploadWorkoutOnFireBase> reference;

        UIUpdateHandler(UploadWorkoutOnFireBase reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MSG_UPDATE_PROGRESS:
                    /*
                     * Qui viene richiamto il metodo per aggiornare la progress bar
                     */
                    if (mIsUpdating) {
                        Log.d(TAG, "Aggiorno il progress");
                        mProgress += 5;
                        reference.get().onProgressUpdate(mProgress);
                        sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, UPDATE_RATE_MS);
                    }
                    break;
                case MSG_FINISH_PROGRESS:
                    /*
                     * Qui viene richiamato il metodo per completare l'aggiornamento e poi attende 2
                     * secondi per chiudere i due Fragment sopra citati.
                     */
                    Log.d(TAG, "Concludo aggiornamento progress");
                    reference.get().mUploadMessage.setText(R.string.upload_complete);
                    reference.get().onProgressUpdate(MAX_PROGRESS);
                    mIsUpdating = false;
                    sendEmptyMessageDelayed(MSG_CLOSE_FRAGMENT, (UPDATE_RATE_MS * 20));
                    break;
                case MSG_CLOSE_FRAGMENT:
                    /*
                     * Qui viene chiamato il metodo di chiusura del fragment e viene inteterrotto il
                     * collegamento tra handler e fragment.
                     */
                    Log.d(TAG, "Chiudo il fragment");
                    reference.get().closeFragment();
                    reference.clear();
                    break;
            }
        }
    }
}
