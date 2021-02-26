/**
 * Questa classe crea il dialog che mostra la lista dei pesi utilizzati l'ultima volta, assieme alla
 * data di esecuzione dell'esercizio.
 */
package it.fitnesschallenge.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import it.fitnesschallenge.R;
import it.fitnesschallenge.model.room.entity.ExerciseExecution;

public class LastExecutionDialog extends AlertDialog {

    public LastExecutionDialog(Context context, ExerciseExecution exerciseExecution) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.last_execution_dialog, null);
        setView(view);
        TextView lastExecutionLabel = view.findViewById(R.id.last_execution_date);
        RecyclerView recyclerView = view.findViewById(R.id.last_execution_dialog_recyclerview);

        TextItemRecyclerView textItemRecyclerView = new TextItemRecyclerView((ArrayList<Float>) exerciseExecution.getUsedKilograms());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(textItemRecyclerView);

        lastExecutionLabel.setText(new SimpleDateFormat(context.getResources()
                .getString(R.string.date_pattern), Locale.getDefault()).format(
                exerciseExecution.getExecutionDate()
        ));
    }
}
