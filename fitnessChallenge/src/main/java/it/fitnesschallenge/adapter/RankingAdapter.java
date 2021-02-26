package it.fitnesschallenge.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.swing.text.View;

import it.fitnesschallenge.R;
import it.fitnesschallenge.model.RankingModel;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<RankingModel> mList;

    public RankingAdapter(List<RankingModel> mList) {
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.ranking_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mUser.setText(mList.get(position).getUser());
        holder.mPoint.setText(NumberFormat.getInstance(Locale.getDefault())
                .format(mList.get(position).getPoint()));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mUser;
        private TextView mPoint;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mUser = itemView.findViewById(R.id.ranking_user_name);
            mPoint = itemView.findViewById(R.id.ranking_user_point);
        }
    }
}
