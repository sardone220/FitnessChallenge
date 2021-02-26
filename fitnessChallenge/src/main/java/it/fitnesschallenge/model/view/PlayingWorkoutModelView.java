/**
 * Questo ViewModel gestisce l'esecuzione del workout creando una lista contenente tutti gli esercizi
 * di un allenamento, e collegandoci un ListIterator così da poter scorrere la lista in tutte le
 * direzioni, inoltre gestisce anche l' interazione con il DB locale.
 */
package it.fitnesschallenge.model.view;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import it.fitnesschallenge.model.User;
import it.fitnesschallenge.model.room.FitnessChallengeRepository;
import it.fitnesschallenge.model.room.entity.Exercise;
import it.fitnesschallenge.model.room.entity.ExerciseExecution;
import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.room.entity.PersonalExerciseWorkoutCrossReference;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;

public class PlayingWorkoutModelView extends AndroidViewModel {

    private static final String TAG = "PlayingWorkoutModelView";
    private static final int NEXT = 1;
    private static final int PREVIOUS = 2;

    /*
     Questo array conterrà tutti gli esercizi del DB per ottenere info su di essi durante l'esecuzione
     dell'allenamento.
    */
    private List<Exercise> mExerciseList;
    // Questo è l'iteratore legato a PersonalExercise che permetterà di scorrere la lista degli esercizi
    private ListIterator<PersonalExercise> mPersonalExerciseListIterator;
    // Questo indicatore tiene conto della serie in esecuzione
    private int mCurrentSeries;
    /*
     * Questa variablie indica all'iteratore come si deve comportare se avviene un cambio di diriezione
     * nello scorrimento della lista, in pratica gli chiamado next() l'iterarore restituisce l'elemento
     * successivo:  A  B  C
     *            ^         (posizione inizale iteratore)
     *                ^     (dopo il primo next() restituisce A e si posiziona tra A e B)
     *            ^         (chiamado previous() restituisce dinuovo A non l'inizi della lista come
     *                       desideriamo al momento)
     * Quindi la variabile indica se il verso di scorrimento è cambiato, se lo è allora deve chiamare
     * rispettivamente previous() o next() 2 volte.
     */
    int direction;
    // Questa variabile contiene l'esercizio attualmente in esecuzione
    private PersonalExercise mCurrentExercise;
    // Questo array contiene tutti gi esercizi legati al workout
    private ArrayList<PersonalExercise> mPersonalExerciseList;
    /*
     * Questo LiveData permette di notificare all'activity chiamante che il workout e gli esercizi sono
     * stati prelevati
     */
    private MutableLiveData<WorkoutWithExercise> mWorkoutWithExerciseLiveData;

    // Questo è il collegamento al repository locale
    private FitnessChallengeRepository mRepository;
    // Questo contiene l'id dell'allenamento attuale
    private MutableLiveData<Long> mWorkoutId;
    // Questa variablile contiene il workout con la lista degli esercizi da eseguire.
    private WorkoutWithExercise mWorkoutWithExercise;
    // Queste variabili permettono di mantenere attivo l'utente mentre esegue il workout
    private User mUser;
    private FirebaseUser mFireStoreUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    public PlayingWorkoutModelView(@NonNull Application application) {
        super(application);
        mRepository = new FitnessChallengeRepository(application);
        mWorkoutId = new MutableLiveData<>(-1L);
        // Questo array conterrà tutte le esecuzioni degli esercizi dell'allenamento
        mWorkoutWithExerciseLiveData = new MutableLiveData<>();
    }

    /**
     * Qesto metodo crea l'iterator impostandolo all'inizio della lista, imposta la prima direzione di
     * scorrimento NEXT e la serie corrente dell'esercizio a 1.
     */
    public void resetListIterator() {
        mPersonalExerciseListIterator = mPersonalExerciseList.listIterator();
        mCurrentExercise = mPersonalExerciseListIterator.next();
        Log.d(TAG, "Primo esercizio prelevato: " + mCurrentExercise.getExerciseId());
        mCurrentSeries = 1;
        direction = NEXT;
    }

    /**
     * Questo metodo verifica se l'iteratore è stato impostato.
     *
     * @return true se l'iteratore non è impostato, false altrimenti
     */
    public boolean isIteratorNull() {
        return mPersonalExerciseListIterator == null;
    }

    /**
     * Questo metodo restituisce l'indice successivo.
     * @return indice successivo
     */
    public int getNextPosition() {
        return mPersonalExerciseListIterator.nextIndex();
    }

    /**
     * Questo metodo restituisce l'indice precedente.
     * @return indice precedente.
     */
    public int getPreviousPosition() {
        return mPersonalExerciseListIterator.previousIndex();
    }

    /**
     * Questo metodo verifica se l'iteratore può ancora scorrere in avanti
     * @return true se può scorrere, false altrimenti.
     */
    public boolean hasNext() {
        if (mPersonalExerciseListIterator != null)
            return mPersonalExerciseListIterator.hasNext();
        else
            return false;
    }

    /**
     * Questo metodo verifica se l'iteratore può andare verso dietro.
     * @return true se può andare, false altrimenti.
     */
    public boolean hasPrevious() {
        if (mPersonalExerciseListIterator != null)
            return mPersonalExerciseListIterator.hasPrevious();
        else
            return false;
    }

    /**
     * Questo metodo restituisce l'esercizio successivo, verificando anche eventuali cambi di
     * direzione nello scorrimento, se viene individuato un cambio di direzione il salto viene fatto
     * due volte, altrimenti verrà restituito di nuovo l'esercizio corrente, verifica inlotre se
     * la serie in esecuzione è l'ultima o meno.
     * @return l'esercizio successivo
     */
    public PersonalExercise getNextExercise() {
        if (mCurrentSeries >= mCurrentExercise.getSteps()) {
            mCurrentSeries = 1;
            if (this.direction == PREVIOUS && mPersonalExerciseListIterator.hasNext()) {
                Log.d(TAG, "Cambio direzione di scorrimento NEXT");
                mPersonalExerciseListIterator.next();
                this.direction = NEXT;
            }
            if (mPersonalExerciseListIterator.hasNext())
                mCurrentExercise = mPersonalExerciseListIterator.next();
        } else
            mCurrentSeries++;
        return mCurrentExercise;
    }

    /**
     * Questo metodo funziona come il precedente ma per lo scorrimento verso dietro nella lista.
     * @return l'esercizio precedente
     */
    public PersonalExercise getPreviousExercise() {
        if (mCurrentSeries <= 1) {
            if (this.direction == NEXT && mPersonalExerciseListIterator.hasPrevious()) {
                Log.d(TAG, "Cambio direzione di scorrimento PREVIOUS");
                mPersonalExerciseListIterator.previous();
                this.direction = PREVIOUS;
            }
            if (mPersonalExerciseListIterator.hasPrevious()) {
                mCurrentExercise = mPersonalExerciseListIterator.previous();
                mCurrentSeries = mCurrentExercise.getSteps();
            }
        } else
            mCurrentSeries--;
        return mCurrentExercise;
    }

    /**
     * Current exercise mi permette di mantenere in memoria quale è l'esercizio selezionato, nel caso
     * in cui il Fragment venga distrutto.
     *
     * @return ritorna l'esercizio attuale.
     */
    public PersonalExercise getCurrentExercise() {
        return mCurrentExercise;
    }

    public int getCurrentSeries() {
        return mCurrentSeries;
    }

    public LiveData<List<Exercise>> getExerciseListLiveData() {
        return mRepository.getListExerciseLiveData();
    }

    public void setExerciseList(List<Exercise> exercises) {
        this.mExerciseList = exercises;
    }

    public void setWorkoutWithExercise(WorkoutWithExercise workoutWithExercise) {
        this.mWorkoutWithExercise = workoutWithExercise;
    }

    public void setPersonalExerciseList(ArrayList<PersonalExercise> personalExerciseList) {
        this.mPersonalExerciseList = personalExerciseList;
    }

    /**
     * Restituisce il LiveData contente l'ultima esecuzione per quell'esercizio se non ci sono
     * esecuzioni precedenti torna null.
     *
     * @return LiveDate contentente l'ultima esecuzione, altrimenti, se non ci sono esecuzioni precedenti
     * torna null.
     */
    public LiveData<ExerciseExecution> getExerciseExecution() {
        return mRepository.getLastExecutionExecution(mCurrentExercise.getPersonalExerciseId());
    }

    /**
     * Permette di salvare nel DB l'esecuzione di un esercizio, assieme alla data di esecuzuione
     *
     * @param exerciseExecution contitene i dati da inserire nel DB riguardo all'ultima esecuzione.
     */
    public void writeExerciseExecutionRoom(ExerciseExecution exerciseExecution) {
        InsertExerciseExecution insertExerciseExecution = new InsertExerciseExecution(mRepository);
        insertExerciseExecution.execute(exerciseExecution);
    }

    public LiveData<List<ExerciseExecution>> getLastExecution(Date currentDate) {
        return mRepository.selectLastWorkoutExecution(currentDate);
    }

    /**
     * Questo metodo ritorna semplicemente la lista degli esercizi necessari a creare la RecyclerView
     *
     * @return un ArrayList contenente gli esercizi selezionati dal DB che dovranno verrannno eseguiti
     */
    public ArrayList<PersonalExercise> getPersonalExerciseList() {
        return mPersonalExerciseList;
    }

    /**
     * Questo metodo preleva le informazioni dell'eserecizio che si sta attualmente eseguendo, facendo
     * @param personalExercise indica l'esercizio in esecuzione e viene usato per individuare
     *                         quale è l'esercizio in esecuzione (VEDERE METODO EQUALS DI EXERCISE)
     * @return il valore di ritorno è un live data contente le informazioni riguardanti un esercizo
     */
    public MutableLiveData<Exercise> getExerciseInformation(PersonalExercise personalExercise) {
        Log.d(TAG, "Prelevo le informazioni sull esercizio: " + personalExercise);
        MutableLiveData<Exercise> mutableLiveData = new MutableLiveData<>();
        mutableLiveData.setValue(mExerciseList.get(mPersonalExerciseList.indexOf(personalExercise)));
        return mutableLiveData;
    }

    public LiveData<List<Workout>> getWorkout() {
        return mRepository.getWorkoutList();
    }

    public void updateWorkout(Workout workout) {
        UpdateWorkout updateWorkout = new UpdateWorkout(mRepository);
        updateWorkout.execute(workout);
    }

    /**
     * Questo metodo consente di prelevare i workout con i relativi esercizi
     *
     * @return ritorna il workout con la lista degli esercizi individuati
     */
    public LiveData<WorkoutWithExercise> getWorkoutWithExercise() {
        Log.d(TAG, "WorkoutId: " + mWorkoutId.getValue());
        return mRepository.getWorkoutWithExerciseList(mWorkoutId.getValue());
    }

    /**
     * Questo metodo verrà richiamato quando il workout verrà prelevato dal DB Firebase
     *
     * @param workoutId contiene l'id del workout prelevato.
     */
    public void setWorkoutId(long workoutId) {
        Log.d(TAG, "Setto l'id del workout precedente " + mWorkoutId.getValue() + " successivo: " + workoutId);
        this.mWorkoutId.setValue(workoutId);
    }

    /**
     * Questo metodo permette di scrivere un nuovo workout nel DB locale, e restituisce un live data
     * contenente il nuovo id.
     * @param workout il workout da scrivere
     * @return live data contenente l'id
     */
    public LiveData<Long> writeWorkout(Workout workout) {
        InsertWorkout insertWorkout = new InsertWorkout(mRepository);
        insertWorkout.execute(workout);
        return insertWorkout.getLiveData();
    }

    /**
     * Questo metodo permette di scrivere la lista degli esercizi personali nel database locale,
     * restituendo un vettore contenente gli id degli esercizi scritti
     *
     * @param personalExerciseList lista degli esercizi
     * @return live data contenente gli id degli esercizi inseriti
     */
    public LiveData<long[]> writePersonaExercise(List<PersonalExercise> personalExerciseList) {
        InsertPersonalExercise insertPersonalExercise = new InsertPersonalExercise(mRepository);
        insertPersonalExercise.execute(personalExerciseList);
        return insertPersonalExercise.getLiveData();
    }

    /**
     * Questo metodo permette di mettere in relazione il workout con i rispettivi esercizi
     *
     * @param workoutId contiene l'id del workout
     * @param exercises contiene gli id deli esercizi
     * @return live data che notifica la fine dell'inserimento nella UI.
     */
    public LiveData<Boolean> insertWorkoutExerciseReference(long workoutId, long[] exercises) {
        ArrayList<PersonalExerciseWorkoutCrossReference> personalExerciseWorkoutCrossReferences = new ArrayList<>();
        for (long exerciseId : exercises) {
            personalExerciseWorkoutCrossReferences.add(new PersonalExerciseWorkoutCrossReference(workoutId, exerciseId));
        }
        InsertWorkoutExerciseReference insertWorkoutExerciseReference = new InsertWorkoutExerciseReference(mRepository);
        insertWorkoutExerciseReference.execute(personalExerciseWorkoutCrossReferences);
        return insertWorkoutExerciseReference.getLiveData();
    }

    /**
     * Questo metodo permette di ottenere il riferimento all'id del workout selezionato
     * @return LiveData contente l'id del workout attutale.
     */
    public MutableLiveData<Long> getWorkoutId() {
        return mWorkoutId;
    }

    /**
     * Questo metodo permette di ottenere le informazioni sull'utente loggato in FireBase
     * @return la classe che descrive l'utente
     */
    public User getUser() {
        return mUser;
    }

    /**
     * Questo metodo permette di memorizzare le informazioni che l'utente ha inserito alla registrazione
     * @param mUser viene passata esattamente un istanza della Classe User che è stata prelevata da FireBase
     */
    public void setUser(User mUser) {
        this.mUser = mUser;
    }

    /**
     * Questo metodo permette di ottenere le informazioni sull'utente loggato in FireBase, diverso dal
     * precedente utente perchè questa classe permette di effettuare operazioni sul DB firebase
     * @return ritorna l'utente collegato in FireBase.
     */
    public FirebaseUser getFireStoreUser() {
        return mFireStoreUser;
    }

    /**
     * Questo metodo permette di settare le informazioni necessarie per collegarsi con FireBase.
     * @param mFireStoreUser prende un istanza dell'Utente memorizzata sul dispositivo e permette di
     *                       accedere a FireBase.
     */
    public void setFireStoreUser(FirebaseUser mFireStoreUser) {
        this.mFireStoreUser = mFireStoreUser;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public void setAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public FirebaseFirestore getDatabase() {
        return mDatabase;
    }

    public void setDatabase(FirebaseFirestore mDatabase) {
        this.mDatabase = mDatabase;
    }


    /**
     * Questa classe crea in Thread che si prenderà carico di inserire l'esecuzione dell'esercizio nel
     * DB, in quanto ogni accesso al DB non può essere eseguita sull UI thread.
     */
    static class InsertExerciseExecution extends AsyncTask<ExerciseExecution, Void, Void> {

        private FitnessChallengeRepository mRepository;

        InsertExerciseExecution(FitnessChallengeRepository repository) {
            this.mRepository = repository;
        }

        @Override
        protected Void doInBackground(ExerciseExecution... exerciseExecutions) {
            mRepository.insertExecution(exerciseExecutions[0]);
            return null;
        }
    }

    /**
     * Questa classe crea in Thread che si prenderà carico di aggiornare il workout nel
     * DB, in quanto ogni accesso al DB non può essere eseguita sull UI thread.
     */
    static class UpdateWorkout extends AsyncTask<Workout, Void, Void> {

        private FitnessChallengeRepository mRepository;

        UpdateWorkout(FitnessChallengeRepository repository) {
            this.mRepository = repository;
        }

        @Override
        protected Void doInBackground(Workout... workouts) {
            mRepository.updateWorkout(workouts[0]);
            return null;
        }
    }

    /**
     * Questa classe crea in Thread che si prenderà carico di inserire il riferimanto tra il workout
     * e gli esercizi all'interno del
     * DB, in quanto ogni accesso al DB non può essere eseguita sull UI thread.
     */
    static class InsertWorkoutExerciseReference extends AsyncTask<List<PersonalExerciseWorkoutCrossReference>, Void, Boolean> {

        private FitnessChallengeRepository mRepository;
        private MutableLiveData<Boolean> mLiveData;

        InsertWorkoutExerciseReference(FitnessChallengeRepository mRepository) {
            this.mRepository = mRepository;
            this.mLiveData = new MutableLiveData<>(false);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mLiveData.setValue(aBoolean);
        }

        LiveData<Boolean> getLiveData() {
            return mLiveData;
        }

        @Override
        protected Boolean doInBackground(List<PersonalExerciseWorkoutCrossReference>... lists) {
            List<PersonalExerciseWorkoutCrossReference> personalExerciseWorkoutCrossReferences = lists[0];
            for (PersonalExerciseWorkoutCrossReference reference : personalExerciseWorkoutCrossReferences) {
                mRepository.insertPersonalExerciseWorkoutReference(reference);
            }
            return true;
        }
    }

    /**
     * Questa classe crea in Thread che si prenderà carico di inserire il workout nel
     * DB, in quanto ogni accesso al DB non può essere eseguita sull UI thread.
     */
    static class InsertWorkout extends AsyncTask<Workout, Void, Long> {

        private FitnessChallengeRepository mRepository;
        private MutableLiveData<Long> mLiveData;

        InsertWorkout(FitnessChallengeRepository mRepository) {
            this.mRepository = mRepository;
            this.mLiveData = new MutableLiveData<>();
        }

        MutableLiveData<Long> getLiveData() {
            return mLiveData;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            mLiveData.setValue(aLong);
        }

        @Override
        protected Long doInBackground(Workout... workouts) {
            Log.d(TAG, "Workout type: " + workouts[0].getWorkoutType());
            return mRepository.insertWorkout(workouts[0]);
        }
    }

    /**
     * Questa classe crea in Thread che si prenderà carico di inserire gli esercizi relativi al nuovo
     * workout nel
     * DB, in quanto ogni accesso al DB non può essere eseguita sull UI thread.
     */
    static class InsertPersonalExercise extends AsyncTask<List<PersonalExercise>, Void, long[]> {

        private FitnessChallengeRepository mRepository;
        private MutableLiveData<long[]> mLiveData;

        InsertPersonalExercise(FitnessChallengeRepository mRepository) {
            this.mRepository = mRepository;
            this.mLiveData = new MutableLiveData<>();
        }

        MutableLiveData<long[]> getLiveData() {
            return mLiveData;
        }

        @Override
        protected void onPostExecute(long[] longs) {
            mLiveData.setValue(longs);
        }

        @Override
        protected long[] doInBackground(List<PersonalExercise>... lists) {
            return mRepository.insertPersonalExerciseList(lists[0]);
        }
    }
}
