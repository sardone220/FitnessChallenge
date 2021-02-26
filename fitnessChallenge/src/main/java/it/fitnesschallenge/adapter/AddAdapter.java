/**
 * Questa classe crea l'adapter per la recycler view che permette l'aggiunta degli esercizi
 * particolare attenzione va data alle interfaccie e alle loro implementazioni
 */
package it.fitnesschallenge.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import it.fitnesschallenge.R;
import it.fitnesschallenge.model.room.entity.Exercise;

public class AddAdapter extends RecyclerView.Adapter<AddAdapter.ViewHolder> {

    private static final String TAG = "AddAdapter";

    private ExpandOnClickListener mExpandOnClickListener;
    private OnSelectItemListener mOnSelectedItemListener;
    private OnOpenTimerListener mOnOpenTimerListener;
    private List<Exercise> mExerciseList;

    public AddAdapter(List<Exercise> mExerciseList) {
        this.mExerciseList = mExerciseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.add_exercise_item, parent, false);
        //qui avviene l'assegnazione dei listener al ViewHolder
        return new ViewHolder(view, mExpandOnClickListener, mOnSelectedItemListener, mOnOpenTimerListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.exerciseTitle.setText(mExerciseList.get(position).getExerciseName());
        holder.exerciseDescription.setText(mExerciseList.get(position).getExerciseDescription());
        holder.exerciseImage.setImageResource(mExerciseList.get(position).getImageReference());
        holder.cardView.setTag(mExerciseList.get(position).getExerciseId());
    }


    @Override
    public int getItemCount() {
        return mExerciseList.size();
    }

    public Exercise getItemAt(int position) {
        return mExerciseList.get(position);
    }

    /**
     * Questa interfaccia permette la gestione del click sul tasto di espansione della card
     */
    public interface ExpandOnClickListener {
        /**
         * Il metodo onClickListener è il metodo che verrà richiamato per gestire il click dal ViewHolder
         * @param finalHeight serve a individuare l'altezza finale che la card avrà dopo l'espasione
         * @param startHeight serve a individuare l'altezza iniziale della card
         * @param itemView permette di passare la view della card in questione
         * @param expanded permette di discernere se la card è già espansa
         */
        void onExpandListener(int finalHeight, int startHeight, View itemView, boolean expanded);
    }

    /**
     * Questa interfaccia gestisce il click sulla selezione dell'esercizio
     */
    public interface OnSelectItemListener {
        /**
         * Questo metodo verrà richiamato alla per gestire il click dal ViewHolder
         * @param view permette di passare la view della card in questione
         * @param position position permette di selezionare direttamente l'esercizio selezionato
         *                 dalla lista degli esercizi
         */
        void onSelectItemListener(View view, int position);
    }

    public interface OnOpenTimerListener {
        void onOpenTimerListener(int position, View view);
    }

    /**
     * Questi due metodi di set permettono di settare il listener dall'activity o fragment chiamante
     * @param expandOnClickListener permette di ottenere un riferimento a questa classe tramite il chiamante
     *                        poichè necessita della sovrasctittura del metodo onClickListener o onSelectedItemListener
     */
    public void setOnExpandClickListener(ExpandOnClickListener expandOnClickListener) {
        this.mExpandOnClickListener = expandOnClickListener;
    }

    public void setOnSelectedItemListener(OnSelectItemListener onSelectedItemListener) {
        this.mOnSelectedItemListener = onSelectedItemListener;
    }

    public void setOnOpenTimerListener(OnOpenTimerListener onOpenTimerListener) {
        this.mOnOpenTimerListener = onOpenTimerListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView exerciseTitle;
        private CardView cardView;
        private TextView exerciseDescription;
        private boolean modified;
        private boolean expanded;
        private ImageView exerciseImage;
        private int finalHeight;
        private int startHeight;
        private MaterialButton setCoolDown;
        private TextInputLayout exerciseSeries;
        private TextInputLayout exerciseRepetition;

        /**
         * @param itemView                layout dell'oggetto in lista
         * @param mExpandOnClickListener        listener del AddAdapter.OnExpandClickListener
         * @param mOnSelectedItemListener listener del AddAdapter.OnSelectedItemListener
         */
        ViewHolder(@NonNull final View itemView, final ExpandOnClickListener mExpandOnClickListener,
                   final OnSelectItemListener mOnSelectedItemListener, final OnOpenTimerListener mSetTimerListener) {
            super(itemView);

            modified = false;
            cardView = itemView.findViewById(R.id.item_card_view);
            /*
             * Questo call back rileva che la view sta per essere disegnata, da qui prendo l'altezza finale
             * che dovrebbe avere e la assegno a finalHeight per usarla dopo nell'animazione.
             */
            cardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!modified) {
                        modified = true;
                        finalHeight = cardView.getHeight() + 20;
                        exerciseDescription.setVisibility(View.GONE);
                    }
                }
            });
            expanded = false;
            exerciseTitle = itemView.findViewById(R.id.add_exercise_title);
            exerciseImage = itemView.findViewById(R.id.add_exercise_img);
            exerciseDescription = itemView.findViewById(R.id.add_exercise_description);
            setCoolDown = itemView.findViewById(R.id.add_exercise_pick_time_button);
            exerciseSeries = itemView.findViewById(R.id.exercise_series);
            exerciseRepetition = itemView.findViewById(R.id.exercise_repetition);
            ImageButton expandCollapseButton = itemView.findViewById(R.id.card_expander_collapse_arrow);
            MaterialCheckBox selectedCheckBox = itemView.findViewById(R.id.select_exercise_check);
            /*
             * La gestione dei click sui pulsanti avviene qiu infatti vengono richiamti i metodi sopra
             * descritti per gestire il click dall'esterno
             */
            expandCollapseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!expanded) {
                        startHeight = cardView.getHeight();
                        expanded = true;
                    }else
                        expanded = false;
                    if (mExpandOnClickListener != null && getAdapterPosition()
                            != RecyclerView.NO_POSITION)
                        //qui abbiamo la gestione dell'onClickListener
                        mExpandOnClickListener.onExpandListener(finalHeight, startHeight, itemView, expanded);
                }
            });
            selectedCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mExpandOnClickListener != null && getAdapterPosition()
                            != RecyclerView.NO_POSITION)
                        //qui abbiamo la gestione dell'onSelectedItemListener
                        mOnSelectedItemListener.onSelectItemListener(itemView, getAdapterPosition());
                }
            });
            setCoolDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSetTimerListener.onOpenTimerListener(getAdapterPosition(), setCoolDown);
                }
            });
        }
    }

    @NonNull
    @Override
    public String toString() {
        return mExerciseList.toString();
    }
}
