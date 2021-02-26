/**
 * Questo fragment permette di prelevare l'ultimo workout attivo disponibile, verificando se su
 * FireStore è presente un nuovo workout, e permette di visualizzare la sequenza di esecuzione prima
 * di avviare l'allenamento tramite il FAB.
 */
package it.fitnesschallenge;


import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import it.fitnesschallenge.adapter.ShowAdapter;
import it.fitnesschallenge.model.User;
import it.fitnesschallenge.model.room.WorkoutType;
import it.fitnesschallenge.model.room.entity.Exercise;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;
import it.fitnesschallenge.model.view.PlayingWorkoutModelView;

import static it.fitnesschallenge.model.SharedConstance.PLAYING_WORKOUT;
import static it.fitnesschallenge.model.SharedConstance.SIGN_UP_FRAGMENT;

public class WorkoutIndoorList extends Fragment {

    private static final String TAG = "WorkoutList";
    private static final String FIREBASE_USER = "firebaseUser";

    private PlayingWorkoutModelView mViewModel;
    private User mUser;
    private FirebaseUser mFireStoreUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private RecyclerView mRecyclerView;
    private ShowAdapter mShowAdapter;
    private Context mContext;
    private boolean mFirebaseCheck;

    public WorkoutIndoorList() {
        // Required empty public constructor
    }

    static WorkoutIndoorList newInstance(User user) {
        WorkoutIndoorList fragment = new WorkoutIndoorList();
        Bundle args = new Bundle();
        args.putParcelable(FIREBASE_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mUser = getArguments().getParcelable(FIREBASE_USER);
        mAuth = FirebaseAuth.getInstance();
        mFireStoreUser = mAuth.getCurrentUser();
        if (mFireStoreUser != null)
            mDatabase = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_indoor_list, container, false);
        mRecyclerView = view.findViewById(R.id.workout_list_recycler_view);
        mViewModel = ViewModelProviders.of(getActivity()).get(PlayingWorkoutModelView.class);
        mFirebaseCheck = false;

        mViewModel.getWorkoutId().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long workoutId) {
                setCurrentWorkout(workoutId);
            }
        });

        Log.d(TAG, "RecyclerView: " + mRecyclerView.toString());

        /*
         * Questo controllo permette di segliere quale Fab utilizzare nel particolare frangente,
         * se non ci sono instanze dell'utente avremo un wokout outdoor, che richiede un fab che
         * permette le modifiche e l'esecuzione dell'allenamento.
         */
            FloatingActionButton floatingActionButton = view.findViewById(R.id.start_workout_FAB);
            floatingActionButton.setVisibility(View.VISIBLE);
            floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow));
            floatingActionButton.setContentDescription(getString(R.string.start_workout_fab));
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayingWorkout playingWorkout = new PlayingWorkout();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                    transaction.replace(R.id.fragmentContainer, playingWorkout, PLAYING_WORKOUT)
                            .addToBackStack(PLAYING_WORKOUT)
                            .commit();
                }
            });

        /*
         * Da questo punto parte l'algoritmo di selezione del workout e dei relativi esercizi
         * Qui vengono selezionati tutti i workout presenti in locale, se non ce ne sono viene avviata
         * la ricerca in cloud, se ovviamente è possibile reperire un'istanza di utente.
         */
        mViewModel.getWorkout().observe(getViewLifecycleOwner(), new Observer<List<Workout>>() {
            @Override
            public void onChanged(List<Workout> workoutList) {
                if (!mFirebaseCheck) {
                    Log.d(TAG, "Avvio verifica su DB, size: " + workoutList.size());
                    if (workoutList.size() > 0)
                        checkWorkoutInLocalDB(workoutList);
                    else if (checkConnection())
                        getLastWorkoutOnFireBase();
                    else
                        errorDialog(R.string.connection_error_message);
                }
            }
        });

        return view;
    }

    /**
     * Questo metodo permette di settare il workout nella recyclerview, questo metodo viene richiamato
     * quando il viene notificato il live data contenente il workoutid.
     *
     * @param workoutId contiene l'id del workout.
     */
    private void setCurrentWorkout(Long workoutId) {
        if (workoutId != -1) {
            Log.d(TAG, "Workout settatto.");
            mViewModel.getExerciseListLiveData().observe(getViewLifecycleOwner(), new Observer<List<Exercise>>() {
                @Override
                public void onChanged(List<Exercise> exercises) {
                    Log.d(TAG, "Settata lista esercizi.");
                    mViewModel.setExerciseList(exercises);
                }
            });
            mViewModel.getWorkoutWithExercise().observe(getViewLifecycleOwner(), new Observer<WorkoutWithExercise>() {
                @Override
                public void onChanged(WorkoutWithExercise workoutWithExercise) {
                    setUI(workoutWithExercise);
                }
            });
        }
    }

    /**
     * Questo metodo permette di settare il layout sulla recycler view, e il relativo contenuto.
     * @param workoutWithExercise contiene il workout e la lista degli esercizi da eseguire.
     */
    private void setUI(WorkoutWithExercise workoutWithExercise) {
        Log.d(TAG, "Observer di WorkoutWithExercise");
        mViewModel.setWorkoutWithExercise(workoutWithExercise);
        Log.d(TAG, "WorkoutWithExercise: " + workoutWithExercise.getPersonalExerciseList().toString());
        mViewModel.setPersonalExerciseList((ArrayList<PersonalExercise>) workoutWithExercise.getPersonalExerciseList());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mShowAdapter = new ShowAdapter(mViewModel.getPersonalExerciseList(), getActivity().getApplication(), getViewLifecycleOwner());
        mRecyclerView.setAdapter(mShowAdapter);
    }

    /**
     * Questo metodo scorre tutti i workout prelevati dal DB locale e setta il workout attivo o meno
     * a seconda della sua endDate, se non vengono trovati wokrout in locale attivi, viene richiamato
     * il cloud per prelevare workout da esso.
     * @param workoutList contiene la lista dei workout locali
     */
    private void checkWorkoutInLocalDB(List<Workout> workoutList) {
        boolean found = false;
        for (int i = 0; i < workoutList.size() && !found; i++) {
            Workout workout = workoutList.get(i);
            Log.d(TAG, "workout[" + i + "]: " + workout.getStartDate());
            if (workout.getEndDate().before(Calendar.getInstance().getTime())
                    && workout.getWorkoutType().equals(WorkoutType.INDOOR)) {
                workout.setActive(false);
                mViewModel.updateWorkout(workout);
                Log.d(TAG, "Trovato workout da disattivare");
            } else {
                Log.d(TAG, "Trovato workout attivo");
                mViewModel.setWorkoutId(workout.getWorkOutId());
                found = true;
            }
        }
        if (!found) {
            if (checkConnection()) {
                if (!mFirebaseCheck) {
                    Log.d(TAG, "Apro connessione con firebase, poichè non ho trovato workout attivi");
                    getLastWorkoutOnFireBase();
                    mFirebaseCheck = true;
                } else
                    errorDialog(R.string.no_active_workout);
            } else {
                errorDialog(R.string.connection_error_message);
            }
        }
    }

    /**
     * Questo metodo mostra dei dialog di errore.
     * @param message contiene il messaggio da visualizzare.
     */
    private void errorDialog(int message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.ops)
                .setMessage(message);
        final FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
        if (message == R.string.registration_error) {
            builder.setNegativeButton(R.string.sign_in, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SignUp signUp = new SignUp();
                    transaction.replace(R.id.fragmentContainer, signUp, SIGN_UP_FRAGMENT)
                            .addToBackStack(SIGN_UP_FRAGMENT)
                            .commit();
                }
            });
        }
        builder.show();
    }

    /**
     * Questo metodo preleva l'ultimo workout inserito su firebase, verifica se esso è attivo, se lo
     * è avvia la scrittura sul DB locale.
     */
    private void getLastWorkoutOnFireBase() {
        mDatabase.collection("user").document(mUser.getUsername())
                .collection("workout").orderBy("workout", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        Log.d(TAG, "Dimensione result set firebase: " + queryDocumentSnapshots.getDocuments().size());
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Log.d(TAG, "Ultimo workout inserito prelevato");
                            final WorkoutWithExercise workoutWithExercise = documentSnapshot.toObject(WorkoutWithExercise.class);
                            Log.d(TAG, "Workout: " + workoutWithExercise.getWorkout().getEndDate());
                            if (workoutWithExercise.getWorkout().getEndDate().before(Calendar.getInstance().getTime())
                                    && workoutWithExercise.getWorkout().getWorkoutType().equals(WorkoutType.INDOOR)) {
                                Log.d(TAG, "Il workout su fire base è scaduto");
                                errorDialog(R.string.no_active_workout);
                            } else {
                                Log.d(TAG, "Avvio la scrittura sul database locale");
                                writeNewWorkoutInLocalDB(workoutWithExercise);
                            }
                        }
                    }
                });
    }

    /**
     * Questo metodo avvia la strittura del workout prelevato da fire base in locale e setta l'observer
     * che notifica l'inserimento del workout, per poi richiamare il metodo che scrive gli esercizi
     * relativi al workout nel DB.
     * @param workoutWithExercise contiene il workout e i suoi relativi esercizi.
     */
    private void writeNewWorkoutInLocalDB(final WorkoutWithExercise workoutWithExercise) {
        mViewModel.writeWorkout(workoutWithExercise.getWorkout()).observe(
                getViewLifecycleOwner(), new Observer<Long>() {
                    @Override
                    public void onChanged(final Long workoutId) {
                        Log.d(TAG, "Ho scritto il workout e ricevuto il suo id locale: " + workoutId);
                        writePersonaExercise(workoutId, workoutWithExercise);
                    }
                }
        );
    }

    /**
     * Questo metodo permette di scrivere gli esercizi del workout nel DB locale, imposta un observer
     * che notifica l'inserimento di tutti gli esercizi e avvia il metodo che permette di collegare
     * gli esercizi al workout.
     * @param workoutId contiene l'id del nuovo workout.
     * @param workoutWithExercise contiene la lista degli esercizi.
     */
    private void writePersonaExercise(final Long workoutId, WorkoutWithExercise workoutWithExercise) {
        mViewModel.writePersonaExercise(workoutWithExercise.getPersonalExerciseList()).observe(
                getViewLifecycleOwner(), new Observer<long[]>() {
                    @Override
                    public void onChanged(long[] longs) {
                        Log.d(TAG, "Ho scritto la lista degli esercizi nel DB locale: " + Arrays.toString(longs));
                        writePersonalExerciseWorkoutReference(longs, workoutId);
                    }
                }
        );
    }

    /**
     * Questo metodo permette di mettere in relazione gli esercizi del workout con il workout stesso.
     * @param longs contiene gli id degli esercizi inseriti.
     * @param workoutId contiene l'id del workout.
     */
    private void writePersonalExerciseWorkoutReference(long[] longs, final Long workoutId) {
        mViewModel.insertWorkoutExerciseReference(workoutId, longs).observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean == true) {
                    Log.d(TAG, "Ho messo in relazione il workout con gli esercizi, adesso notifico l'id del workout");
                    mViewModel.setWorkoutId(workoutId);
                }
            }
        });
    }

    /**
     * Questo metodo controlla se il dispositivo è connesso prima di richiamare il metodo setObserver()
     */
    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
    }
}
