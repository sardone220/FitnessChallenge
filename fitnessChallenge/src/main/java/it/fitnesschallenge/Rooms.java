package it.fitnesschallenge;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.fitnesschallenge.adapter.RoomsAdapter;
import it.fitnesschallenge.model.Participation;
import it.fitnesschallenge.model.Room;

public class Rooms extends Fragment {

    private static final String TAG = "Room";
    private static final String ROOM = "room";

    private RecyclerView mRecyclerView;
    private RoomsAdapter mRoomsAdapter;
    private ImageView mConnectionImage;
    private ProgressBar mProgressBar;
    private FirebaseFirestore mDatabase;
    private FirebaseUser mUser;

    public Rooms() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);

        Log.d(TAG, "onCreateView");

        mRecyclerView = view.findViewById(R.id.fragment_rooms_recyclerview);
        mConnectionImage = view.findViewById(R.id.fragment_rooms_no_connection_image);
        mProgressBar = view.findViewById(R.id.rooms_fragment_progress_bar);

        final MaterialButton createNewRoom = view.findViewById(R.id.rooms_create_new_room_button);

        mDatabase = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mDatabase != null) {
            readUserRoomFirestone();
        }

        createNewRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewRoomActivity.class);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(getActivity(),
                                createNewRoom, "new_room");
                getActivity().startActivity(intent, options.toBundle());
            }
        });
        return view;
    }

    private void readUserRoomFirestone() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mDatabase.collection("user").document(mUser.getEmail())
                    .collection("participation")
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    Log.d(TAG, "Prelevate room personali: " + queryDocumentSnapshots.getDocuments().size());
                    if (queryDocumentSnapshots.getDocuments().size() > 0) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Participation participation = documentSnapshot.toObject(Participation.class);
                            readRoomsFromFirestone(participation.getRoomsList());
                        }
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.GONE);
                        mConnectionImage.setImageResource(R.mipmap.into_the_night);
                        mConnectionImage.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.ops)
                    .setMessage(R.string.connection_error_message);
            builder.show();
            mRecyclerView.setVisibility(View.GONE);
            mConnectionImage.setVisibility(View.VISIBLE);
        }
    }

    private void readRoomsFromFirestone(List<String> roomsStringList) {
        Log.d(TAG, "Room prelevate: " + roomsStringList.toString());
        final ArrayList<Room> roomArrayList = new ArrayList<>();
        mRoomsAdapter = new RoomsAdapter(roomArrayList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRoomsAdapter);
        for (String roomCode : roomsStringList) {
            mDatabase.collection("room").document(roomCode).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Log.d(TAG, "Prelevate room");
                            Room room = documentSnapshot.toObject(Room.class);
                            roomArrayList.add(room);
                            if (mRoomsAdapter != null)
                                mRoomsAdapter.notifyDataSetChanged();
                        }
                    });
        }
        Log.d(TAG, "Setto la ui");
        mProgressBar.setVisibility(View.GONE);

        mRoomsAdapter.setOnClickListener(new RoomsAdapter.OnClickListener() {
            @Override
            public void onClick(int position, RoomsAdapter.ViewHolder view) {
                openRoomActivity(position, view);
            }
        });
    }

    private void openRoomActivity(int position, RoomsAdapter.ViewHolder viewHolder) {
        Intent intent = new Intent(getActivity(), RoomActivity.class);
        intent.putExtra(ROOM, mRoomsAdapter.getItemAtPosition(position));
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                Pair.create((View) viewHolder.getRoomName(), "room_name"),
                Pair.create((View) viewHolder.getRoomCreator(), "room_creator"),
                Pair.create((View) viewHolder.getImageRoom(), "room_image"));
        startActivity(intent, options.toBundle());
    }
}
