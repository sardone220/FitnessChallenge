package it.fitnesschallenge.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import it.fitnesschallenge.R;

public class WeightInputAdapter extends RecyclerView.Adapter<WeightInputAdapter.ViewHoled> {

    private static final String TAG = "WeightInputAdapter";

    private List<Float> mUsedKilograms;
    private Context mContext;
    private ArrayList<ViewHoled> mViewHolderList;

    public WeightInputAdapter(List<Float> usedKilograms, Context context) {
        this.mUsedKilograms = usedKilograms;
        this.mContext = context;
        mViewHolderList = new ArrayList<>();
        Log.d(TAG, "Dimensione lista: " + mUsedKilograms.size());
    }

    @NonNull
    @Override
    public ViewHoled onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.input_weight_recycler_item, parent, false);
        Log.d(TAG, "Setto il layout della RecyclerView");
        return new ViewHoled(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoled holder, int position) {
        holder.mTextInputLayout.setHint(mContext.getResources().getQuantityString(R.plurals.repetitionNumber, (position + 1), (position + 1)));
        mViewHolderList.add(holder);
        Log.d(TAG, "Faccio il bind tra RecyclerView e ViewHolder");
    }

    @Override
    public int getItemCount() {
        return mUsedKilograms.size();
    }

    public ArrayList<Float> getRecyclerValue() throws NumberFormatException {
        ArrayList<Float> returnValue = new ArrayList<>();
        for (ViewHoled holed : mViewHolderList) {
            returnValue.add(Float.parseFloat(holed.mTextInputLayout.getEditText().getText().toString().trim()));
        }
        return returnValue;
    }

    static class ViewHoled extends RecyclerView.ViewHolder {

        private TextInputLayout mTextInputLayout;

        ViewHoled(@NonNull View itemView) {
            super(itemView);
            mTextInputLayout = itemView.findViewById(R.id.input_weight_item);
            Log.d(TAG, "Creo il ViewHolder");
        }
    }
}
