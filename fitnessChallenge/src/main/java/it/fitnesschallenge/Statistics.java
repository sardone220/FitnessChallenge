/**
 * Questo fragment permette di visualizzare un grafico a linee composto dal giorno di esecuzione sull'
 * asse delle X e la media dei pesi utilizzti per il workout sull'asse delle Y, inoltre se si clicca
 * su uno dei punti del grafico compare una "nuvoletta" che riporta i dati con esattezza.
 */
package it.fitnesschallenge;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.fitnesschallenge.adapter.LineChartMakerView;
import it.fitnesschallenge.model.ExecutionList;
import it.fitnesschallenge.model.room.entity.ExerciseExecution;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.view.StatisticsRoomsViewModel;

public class Statistics extends Fragment {

    private static final String TAG = "Statistics";
    private static final String USER = "user";

    // LineChart è l'istanza del grafico a linee
    private LineChart mLineChart;
    private TextView mWorkoutsChart;
    /*
     * Questa lista contiene degli oggetti denominati Entry, che fondamentalmente, racchiude due valori
     * di tipo float: x, y, che sono rispettivamente i valori che verranno utilizzati per la rappresentazione
     * dei punti sul grafico.
     */
    private ArrayList<Entry> mEntryList;
    private StatisticsRoomsViewModel mViewModel;
    private FirebaseFirestore mDatabase;
    private FirebaseUser mUser;

    public Statistics() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        mViewModel = ViewModelProviders.of(getActivity()).get(StatisticsRoomsViewModel.class);
        mLineChart = view.findViewById(R.id.execution_chart);
        mWorkoutsChart = view.findViewById(R.id.workout_statistics_times);

        mEntryList = new ArrayList<>();

        mViewModel.getNumberOfExecution().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                mWorkoutsChart.setText(NumberFormat.getInstance(Locale.getDefault())
                        .format(integer));
            }
        });

        /*
         * Qui viene interpellato il ViewModel per prelevare i dati delle esecuzioni dal DB locale
         * se non vengono trovate esecuzioni in locale si interpella FireBase per prelevare eventuali
         * esecuizioni in remoto.
         */
        mViewModel.getExerciseExecutionList().observe(getViewLifecycleOwner(), new Observer<List<ExerciseExecution>>() {
            @Override
            public void onChanged(List<ExerciseExecution> exerciseExecutions) {
                if (exerciseExecutions.size() > 0) {
                    Log.d(TAG, "Trovate esecuzioni");
                    setEntryList(exerciseExecutions);
                } else {
                    mViewModel.getWorkoutList().observe(getViewLifecycleOwner(), new Observer<List<Workout>>() {
                        @Override
                        public void onChanged(List<Workout> workoutList) {
                            if (workoutList.size() > 0) {
                                Log.d(TAG, "Trovati workout nel db locale.");
                                setActiveWorkout(workoutList);
                            } else {
                                Log.d(TAG, "Non ci sono workout nel db locale.");
                            }
                        }
                    });
                }
                setBarChart();
            }
        });

        setBarChart();

        return view;
    }

    /**
     * Questo metodo viene invocato se in locale non sono presenti esecuzioni, quindi verranno prelevati
     * da remoto solo le esecuzioni legate a workout attivi.
     *
     * @param workouts contiene una lista di tutti i workout individuati per l'utente in questione.
     */
    private void setActiveWorkout(List<Workout> workouts) {
        Calendar calendar = Calendar.getInstance();
        for (Workout workout : workouts) {
            if (workout.getEndDate().before(calendar.getTime())) {
                Log.d(TAG, "Workout id: " + workout.getWorkOutId() + " start date: " + workout.getStartDate());
                workout.setActive(false);
            } else {
                if (mUser != null)
                    getExecution(workout);
                else {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                            .setTitle(R.string.ops)
                            .setMessage(R.string.no_active_workout);
                    builder.show();
                }
            }
        }
    }

    /**
     * In questo metodo vengono prelevate le esecuzioni dei workout da FireStore e memorizzate in locale
     * così da notificare i LiveData che aggiorneranno il grafico.
     * @param workout contiene il workout di riferimento.
     */
    private void getExecution(Workout workout) {
        Log.d(TAG, "Prelevo dati da FireStore.");
        final ArrayList<ExecutionList> executionLists = new ArrayList<>();
        String documentPath = "user/" + mUser.getEmail() + "/workout/" + new SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault()).format(workout.getStartDate()) + "/execution";
        Log.d(TAG, "documentPath: " + documentPath);
        mDatabase.collection(documentPath).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Log.d(TAG, "Prelevate esecuzioni da Firestore");
                            executionLists.add(documentSnapshot.toObject(ExecutionList.class));
                        }
                        mViewModel.writeExecutionsInLocalDB(executionLists);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), getString(R.string.shit_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Questo metodo crea la lista dei nodi necessari per disegnare il grafico in questione.
     * @param exerciseExecutions contiene una lista delle esecuzioni.
     */
    private void setEntryList(List<ExerciseExecution> exerciseExecutions) {
        for (ExerciseExecution execution : exerciseExecutions) {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.setTime(execution.getExecutionDate());
            String floatDate = calendar.get(Calendar.DAY_OF_YEAR) + "." + calendar.get(Calendar.YEAR);
            float executionDate = Float.parseFloat(floatDate);
            Log.d(TAG, "Data di esecuzione: " + executionDate);
            float usedKilogramsAvg = getKilogramsAVG(execution.getUsedKilograms());
            Entry entry = new Entry(executionDate, usedKilogramsAvg);
            Log.d(TAG, "\tCreata nuova entry: " + executionDate + ", " + usedKilogramsAvg);
            mEntryList.add(entry);
        }
    }

    /**
     * Questo metodo contiene i comandi per settare i nodi che il grafico dovrà rappresentare.
     * Tramite la classe LineDataSet viene utilizzata la lista dei nodi da passare al grafico, impostando
     * le dipendenze, e gli apetti delle linee di collegamento e i punti da mostrare.
     * LineData racchiude queste informazioni e le invia al grafico per permetterne la reppresentazione.
     */
    private void setBarChart() {
        LineDataSet lineDataSet = new LineDataSet(mEntryList, "Execution");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setCircleColor(getContext().getColor(R.color.colorPrimaryDark));
        lineDataSet.setLineWidth(4f);
        lineDataSet.setCircleRadius(6f);

        LineData data = new LineData(lineDataSet);
        data.setValueTextSize(10f);

        mLineChart.setData(data);

        initLineChartChart();
    }

    /**
     * Questo metodo permette di impostare i valori necessari per disegnare il grafico, come le etichette,
     * la legenda, la granularità del grafico, e lo sfondo di tutta la rappresentazione.
     */
    private void initLineChartChart() {
        Legend legend = mLineChart.getLegend();
        legend.setEnabled(false);
        mLineChart.setBackgroundColor(Color.WHITE);
        mLineChart.getDescription().setEnabled(false);
        mLineChart.setTouchEnabled(true);
        mLineChart.setDragEnabled(true);
        mLineChart.setPinchZoom(true);
        mLineChart.animateX(0);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setGranularity(1F);
        xAxis.setXOffset(10F);


        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawTopYLabelEntry(false);
        rightAxis.setDrawLabels(false);

        LineChartMakerView makerView = new LineChartMakerView(getContext(), R.layout.custom_point_view);
        makerView.setChartView(mLineChart);
        mLineChart.setMarker(makerView);
    }

    /**
     * Questo metodo calcola la media di tutti i pesi utilizzati durante il workout
     * @param usedKilograms lista dei pesi utilizzati
     * @return la media di tutti i pesi.
     */
    private Float getKilogramsAVG(List<Float> usedKilograms) {
        float sum = 0.0F;
        for (Float kilograms : usedKilograms)
            sum += kilograms;
        return sum / usedKilograms.size();
    }
}
