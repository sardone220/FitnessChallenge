package it.fitnesschallenge;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.fitnesschallenge.adapter.LineChartMakerView;
import it.fitnesschallenge.model.ExecutionList;
import it.fitnesschallenge.model.room.entity.ExerciseExecution;
import it.fitnesschallenge.model.room.entity.Workout;
import it.fitnesschallenge.model.room.entity.reference.WorkoutWithExercise;


/**
 * A fragment representing a single User detail screen.
 * This fragment is either contained in a {@link UserListActivity}
 * in two-pane mode (on tablets) or a {@link UserDetailActivity}
 * on handsets.
 */
public class UserDetailFragment extends Fragment {

    private static final String TAG = "UserDetailFragment";

    private List<Entry> mEntryList = new ArrayList<>();
    private LineChart mLineChart;
    private FirebaseFirestore mDatabase;
    List<Float> media; //ogni elemento di questa lista contiene la media di un esecuzione di workout
    List<ExerciseExecution> exerciseExecutions; //ogni elemento contiene un ExerciseExecution
    List<List<Float>> pesi; //array che conterra un array di pesi per ogni execution list

    private static final String ARG_ITEM_ID = "itemId";

    private String mItem;

    public UserDetailFragment() {
        //Need empty constructor
    }

    public static UserDetailFragment newInstance(String userItem) {
        UserDetailFragment fragment = new UserDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ITEM_ID, userItem);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = getArguments().getString(ARG_ITEM_ID);
        }
        mDatabase = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_detail, container, false);
        mLineChart = rootView.findViewById(R.id.progress_chart);

        /*
         * In questo richiamo firebase ottengo l'utlimo workout, che a sua volta richiama getExecutionList
         */
        mDatabase.collection("user").document(mItem).collection("workout")
                .orderBy("workout", Query.Direction.DESCENDING).limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Log.d(TAG, "Ottenuto workout con esercizi");
                    getExecutionList(documentSnapshot.toObject(WorkoutWithExercise.class).getWorkout());

                }
            }
        });

        //impostiamo il grafico
        mLineChart = rootView.findViewById(R.id.progress_chart);
        return rootView;
    }

    /**
     * In questo metodo viene richiamato Firebase fino ad ottenere la lista di tutte le esecuzioni
     * per quel workout, ora per ogni esecuzione va a richiamare getExecutionData.
     */
    private void getExecutionList(Workout workout) {
        mDatabase.collection("user").document(mItem).collection("workout")
                .document(new SimpleDateFormat(getString(R.string.date_pattern), Locale.getDefault()).format(workout.getStartDate()))
                .collection("execution").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Log.d(TAG, "Ottenuto lista esecuzione");
                    getExecutionData(documentSnapshot.toObject(ExecutionList.class));
                }
            }
        });
    }

    /**
     * Ogni lista di esecuzione contiene una lista di ExerciseExecution dove bisogna calcolare per
     * ognuno il peso medio usato in quanto la classe contiene un array contentente i pesi usati,
     * per ogni ExecutionList la media deve essere unica.
     *
     * @param executionList
     */
    private void getExecutionData(ExecutionList executionList) {
        pesi = new ArrayList<>();
        exerciseExecutions = executionList.getExerciseList(); //questo metodo ci ritorna una lista di tipo exercise exection
        for(ExerciseExecution execution : exerciseExecutions) {
            pesi.add(execution.getUsedKilograms());
        }
        //quindi adesso ogni elemento della lista pesi, conntiene la lista dei pesi usati
        media = new ArrayList<>(); //ogni elemento contiene la media
        for(int i = 0; i < pesi.size(); i++) {
           media.add(getKilogramsAVG(pesi.get(i)));
        }
        setEntryList(exerciseExecutions);
        setBarChart();
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
}
