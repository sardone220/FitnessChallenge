/**
 * Questo Fragment contiente l'intelligenza e le informazioni necessarie all'utente per eseguire il
 * workout step-by-step, una volta terminato il workout questo fragment eseguirà le istruzioni necessarie
 * ad effettuare la sincronizzazione con il FireBase.
 */
package it.fitnesschallenge;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.Locale;

import it.fitnesschallenge.adapter.LastExecutionDialog;
import it.fitnesschallenge.model.room.entity.Exercise;
import it.fitnesschallenge.model.room.entity.ExerciseExecution;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.view.PlayingWorkoutModelView;

import static it.fitnesschallenge.model.SharedConstance.ADD_WEIGHT;
import static it.fitnesschallenge.model.SharedConstance.CONVERSION_SEC_IN_MILLIS;
import static it.fitnesschallenge.model.SharedConstance.TIMER_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.UPLOAD_WORKOUT_ON_FIREBASE;


public class PlayingWorkout extends Fragment {

    private static final String TAG = "PlayingWorkout";
    private static final int NANOSECOND_IN_MILLISECONDS = 1000000;
    private static final short PREVIOUSLY = 1;
    private static final short NEXT = 2;
    private static final short INIT = 3;

    private ImageButton mNext;
    private ImageButton mPrev;
    private TextView mNextText;
    private TextView mPrevText;
    private TextView mExerciseTitle;
    private ImageView mExerciseImage;
    private TextView mTimeTimer;
    private TextView mCurrentRepetition;
    private TextView mMaxRepetition;
    private PlayingWorkoutModelView mViewModel;
    private ProgressBar mProgressBar;
    private TextView mProgressValue;
    private Timer mTimer;
    private ExerciseExecution mExerciseExecution;
    private MaterialButton mStopButton;
    private SensorManager mSensorManager;
    private float mAcceleration;
    private float mCurrentAcceleration;
    private long mLastShake;
    private long mCurrentShake;
    private long mTimeDelta;

    public PlayingWorkout() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_playing_workout, container, false);

        mStopButton = view.findViewById(R.id.playing_workout_stop_workout);
        mExerciseTitle = view.findViewById(R.id.playing_workout_title);
        mExerciseImage = view.findViewById(R.id.playing_workout_image);
        mTimeTimer = view.findViewById(R.id.playing_workout_timer);
        ImageButton startTimer = view.findViewById(R.id.playing_workout_start_timer);
        mCurrentRepetition = view.findViewById(R.id.playing_workout_repetition);
        mMaxRepetition = view.findViewById(R.id.playing_workout_repetition_max);
        ImageButton infoButton = view.findViewById(R.id.playing_workout_info);
        Button addWeight = view.findViewById(R.id.playing_exercise_add_weigth);
        mNext = view.findViewById(R.id.next_exercise);
        mNextText = view.findViewById(R.id.next_exercise_label);
        mPrev = view.findViewById(R.id.prev_exercise);
        mPrevText = view.findViewById(R.id.prev_exercise_label);
        mProgressBar = view.findViewById(R.id.playing_workout_progress_bar);
        mProgressValue = view.findViewById(R.id.progress_value);
        mAcceleration = 10f;
        mCurrentAcceleration = SensorManager.GRAVITY_EARTH;
        mCurrentShake = 0L;
        mLastShake = 0L;
        mTimeDelta = 2000;

        mViewModel = ViewModelProviders.of(getActivity()).get(PlayingWorkoutModelView.class);

        // Prelevo il primo esercizio da eseguire nel ViewModel
        getCurrentExercise(INIT);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentExercise(NEXT);
            }
        });
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentExercise(PREVIOUSLY);
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStopButton.getContentDescription().equals(getContext().getString(R.string.finish_workout))) {
                    uploadOnFireBase();
                } else
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

        startTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimer = new Timer();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                        .replace(R.id.fragmentContainer, mTimer, TIMER_FRAGMENT)
                        .addToBackStack(TIMER_FRAGMENT)
                        .commit();
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.getExerciseExecution().observe(getViewLifecycleOwner(), new Observer<ExerciseExecution>() {
                    @Override
                    public void onChanged(ExerciseExecution personalExerciseExecution) {
                        Log.d(TAG, "Prelevata ultima esecuzione");
                        try {
                            mExerciseExecution = personalExerciseExecution;
                            LastExecutionDialog lastExecutionDialog = new LastExecutionDialog(getContext(), mExerciseExecution);
                            lastExecutionDialog.show();
                        } catch (NullPointerException ex) {
                            Log.d(TAG, "Non ci sono esecuzioni precedenti");
                            mExerciseExecution = null;
                            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext())
                                    .setTitle(R.string.ops)
                                    .setMessage(R.string.there_is_not_previously_execution);
                            materialAlertDialogBuilder.show();
                        }
                    }
                });
            }
        });

        addWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddWeight addWeight = new AddWeight();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                        .replace(R.id.fragmentContainer, addWeight, ADD_WEIGHT)
                        .addToBackStack(ADD_WEIGHT)
                        .commit();
            }
        });

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        return view;
    }

    /**
     * Questo metodo apre il fragment per fare l'upload del workout su firebase.
     */
    private void uploadOnFireBase() {
        Log.d(TAG, "Apro uploadOnFireBase.");
        UploadWorkoutOnFireBase uploadWorkoutOnFireBase = new UploadWorkoutOnFireBase();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                .replace(R.id.fragmentContainer, uploadWorkoutOnFireBase, UPLOAD_WORKOUT_ON_FIREBASE)
                .addToBackStack(UPLOAD_WORKOUT_ON_FIREBASE)
                .commit();
    }

    /**
     * La variabile sensorEventListener crea il listener ai cambiamenti dei dati inviati dai sensori
     * in questo caso prende i tre assi x, y, z, o meglio le accellerazioni sui tre assi e ne fa una
     * somma vettoriale, ottenendo l'accelerazione di tutto il device.
     */
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float accelerationOnX = event.values[0];
            float accelerationOnY = event.values[1];
            float accelerationOnZ = event.values[2];

            float mLastAcceleration = mCurrentAcceleration;
            /*
             * Il time stamp di event restituisce il valore in nano secondi, quindi per comodità lo
             * convertiamo in millisecondi.
             */
            mCurrentShake = event.timestamp / NANOSECOND_IN_MILLISECONDS;
            /*
             * Se sto eseguendo per la prima volta questo pezzo di codice, serve settare un riferimento
             * anche per last shake
             */
            if (mLastShake == 0) {
                Log.d(TAG, "Setto per la prima volta last shake");
                mLastShake = mCurrentShake;
            }

            /*
             * Questa formula ricava l'accellerazione lienare di tutto il dispositivo, sfruttando la
             * proprietà fisica dell'accellerazione vista sotto forma di vettore con le sue componenti
             * x, y, z.
             */
            mCurrentAcceleration = (float) Math.sqrt(accelerationOnX * accelerationOnX +
                    accelerationOnY * accelerationOnY +
                    accelerationOnZ * accelerationOnZ);

            // Questo delta di accelerazione mi dice se il dispositivo sta accellerando, in questo istante.
            float accelerationDelta = mCurrentAcceleration - mLastAcceleration;

            /*
             * In questa formula sottraggo l'accellerazione di gravità, se il telefono sta solo cadendo,
             * non riuscirà a produrre l'accelerazione necessaria per attivare lo shake event.
             */
            mAcceleration = mAcceleration * 0.9f + accelerationDelta;
            /*
             * Il delta temporale serve per valutare se lo shake che si è rilevato è dovuto ancora allo
             * shake iniziale, poichè il sensore è così veloce da inviare almeno 3 dati diversi per una
             * stessa shekerata.
             */
            mTimeDelta = mCurrentShake - mLastShake;

            /*
             * Se l'accelerazione totale del dispositivo è maggiore di 18(?, sperimentalmente provato
             * come valore targhet, per il tipo di shake che cerchiamo di rilevare) e il tempo tra un
             * dato e l'altro è maggiore di 2 sec.
             */
            if (mAcceleration > 18 & mTimeDelta >= 2000) {
                mLastShake = mCurrentShake;
                Log.d(TAG, "Sensor event, prossimo esercizio");
                if (mNext.getVisibility() == View.VISIBLE) {
                    getCurrentExercise(NEXT);
                } else
                    uploadOnFireBase();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Necessario sovrascivere ma lascio vuoto.
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Questo metodo permette di ottenere l'esercizio corrente, in base al tipo di operazione richiesta
     *
     * @param witch contiene il tipo di operazione da esegure:
     *              PREVIOUSLY: indica che si sta selezionando l'esercio precedente
     *              NEXT: indica che si sta selezionando l'esercizio successivo
     *              INIT: indica che l'operazione è quella di inizializzazione iniziale
     */
    private void getCurrentExercise(short witch) {
        if (witch == PREVIOUSLY) {
            Log.d(TAG, "Prelievo precedente");
            mViewModel.getPreviousExercise();
        } else if (witch == NEXT) {
            Log.d(TAG, "Prelievo successivo");
            mViewModel.getNextExercise();
        } else if (witch == INIT) {
            if (mViewModel.isIteratorNull()) {
                Log.d(TAG, "Prelievo il primo esercizio");
                mViewModel.resetListIterator();
            }
        }
        setUI(witch);
        setNavigationButton(witch);

    }

    /**
     * SetUi contiene tutte le istruzioni necessarie a mostrare l'esercizio corrente all'utente
     *
     * @param witch indica che tipo di operazione eseguire
     */
    private void setUI(final short witch) {
        final int progress = 100 / mViewModel.getPersonalExerciseList().size();
        mMaxRepetition.setText(NumberFormat.getInstance(Locale.getDefault())
                .format(mViewModel.getCurrentExercise().getSteps())
        );
        mViewModel.getExerciseInformation(mViewModel.getCurrentExercise()).observe(getViewLifecycleOwner(), new Observer<Exercise>() {
            @Override
            public void onChanged(Exercise exercise) {
                mExerciseTitle.setText(exercise.getExerciseName());
                mExerciseImage.setImageResource(exercise.getImageReference());
                mTimeTimer.setText(PersonalExercise.getCoolDownString(mViewModel.getCurrentExercise().getCoolDown() * CONVERSION_SEC_IN_MILLIS));
                if (witch == NEXT || witch == INIT) {
                    mProgressValue.setText(NumberFormat.getInstance(Locale.getDefault()).format(progress * mViewModel.getNextPosition()));
                    mProgressBar.setProgress(progress * mViewModel.getNextPosition());
                } else if (witch == PREVIOUSLY) {
                    Log.d(TAG, "ProgressBar: " + mProgressBar.getProgress());
                    Log.d(TAG, "Progress: " + progress);
                    mProgressValue.setText(NumberFormat.getInstance(Locale.getDefault()).format(progress * (mViewModel.getNextPosition() + 1)));
                    mProgressBar.setProgress(progress * (mViewModel.getNextPosition() + 1));
                }
            }
        });
        mCurrentRepetition.setText(NumberFormat
                .getInstance(Locale.getDefault())
                .format(mViewModel.getCurrentSeries()));
    }

    /**
     * Questo metodo consente di verificare quali tasti di navigazione mostrare all'utente
     * se l'esercizio è il primo allora mostrerà solo il pusante di successivo, e così via.
     */
    private void setNavigationButton(int witch) {
        Log.d(TAG, "Ha precedente: " + mViewModel.hasPrevious());
        Log.d(TAG, "getPreviousPosition(): " + mViewModel.getPreviousPosition());
        Log.d(TAG, "Ha successivo: " + mViewModel.hasNext());
        Log.d(TAG, "getNextPosition(): " + mViewModel.getNextPosition());
        Log.d(TAG, "Serie corrente: " + mViewModel.getCurrentSeries());
        if ((witch == INIT ||
                (mViewModel.getPreviousPosition() == -1))
                && (mViewModel.getCurrentSeries() <= 1)) {
            mPrev.setVisibility(View.GONE);
            mPrevText.setVisibility(View.GONE);
        } else {
            if (mPrev.getVisibility() == View.GONE) {
                mPrev.setVisibility(View.VISIBLE);
                mPrevText.setVisibility(View.VISIBLE);
            }
        }
        if (((mViewModel.getNextPosition() == 3) && mViewModel.getCurrentSeries() >= mViewModel.getCurrentExercise().getSteps())) {
            if (mNext.getVisibility() == View.VISIBLE) {
                mNext.setVisibility(View.GONE);
                mNextText.setVisibility(View.GONE);
                mStopButton.setIconResource(R.drawable.ic_backup_24dp);
                mStopButton.setText(R.string.finish);
                mStopButton.setContentDescription(getString(R.string.finish_workout));
            }
        } else {
            if (mNext.getVisibility() == View.GONE) {
                mNext.setVisibility(View.VISIBLE);
                mNextText.setVisibility(View.VISIBLE);
            }
            if (mStopButton.getContentDescription().equals(getString(R.string.finish_workout))) {
                mStopButton.setIconResource(R.drawable.ic_stop_24dp);
                mStopButton.setText(getString(R.string.stop));
                mStopButton.setContentDescription(getString(R.string.stop_workout));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorListener);
    }
}
