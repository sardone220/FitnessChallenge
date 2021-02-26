package it.fitnesschallenge.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import it.fitnesschallenge.R;
import it.fitnesschallenge.model.Room;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.ViewHolder> {

    private static final String TAG = "RoomsAdapter";

    private OnClickListener mListener;
    private List<Room> mRoomList;

    public RoomsAdapter(List<Room> mRoomList) {
        this.mRoomList = mRoomList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.rooms_layout, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mCardView.setTag("Item_" + position);
        holder.mRoomName.setText(mRoomList.get(position).getRoomName());
        holder.mCreator.setText(mRoomList.get(position).getRoomCreator());
    }

    @Override
    public int getItemCount() {
        return mRoomList.size();
    }

    public Room getItemAtPosition(int position) {
        return mRoomList.get(position);
    }

    public interface OnClickListener {
        void onClick(int position, ViewHolder view);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView mCardView;
        private CircularImageView mImageView;
        private TextView mRoomName;
        private TextView mCreator;

        ViewHolder(@NonNull View itemView, final OnClickListener mListener) {
            super(itemView);
            final ViewHolder holder = this;
            mCardView = itemView.findViewById(R.id.rooms_card_view);
            mImageView = itemView.findViewById(R.id.rooms_layout_room_image);
            mRoomName = itemView.findViewById(R.id.rooms_layout_room_name);
            mCreator = itemView.findViewById(R.id.rooms_layout_creator);
            mRoomName.setTransitionName("room_name_shared_" + getAdapterPosition());
            mImageView.setTransitionName("room_image_shared_" + getAdapterPosition());
            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(getAdapterPosition(), holder);
                }
            });
        }

        public TextView getRoomName() {
            return mRoomName;
        }

        public CircularImageView getImageRoom() {
            return mImageView;
        }

        public TextView getRoomCreator() {
            return mCreator;
        }
    }
}
