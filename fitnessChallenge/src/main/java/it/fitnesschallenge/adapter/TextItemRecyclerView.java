/**
 * Questo adapter crea la lista di visualizzazione dei pesi utilizzati nell'utlima esecuzione, è un
 * estenzione di Adapter standard senza particolari accorgimenti.
 */
package it.fitnesschallenge.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import it.fitnesschallenge.R;

public class TextItemRecyclerView extends RecyclerView.Adapter<TextItemRecyclerView.ViewHolder> {

    private ArrayList<Float> mArrayList;

    TextItemRecyclerView(ArrayList<Float> arrayList) {
        this.mArrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.standard_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StringBuilder builder = new StringBuilder(NumberFormat.getInstance(Locale.getDefault()).format(mArrayList.get(position)));
        builder.append(" Kg");
        holder.mTextView.setText(builder.toString());
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text_repetition);
        }
    }
}
