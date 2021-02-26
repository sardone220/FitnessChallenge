package it.fitnesschallenge;

import android.os.Bundle;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import it.fitnesschallenge.adapter.RankingAdapter;
import it.fitnesschallenge.model.RankingModel;
import it.fitnesschallenge.model.Room;

public class RoomActivity extends AppCompatActivity {

    private static final String TAG = "RoomActivity";
    private static final String ROOM = "room";

    private TextView mRoomName;
    private TextView mRoomCode;
    private TextView mRoomRanking;
    private TextView mRoomCreator;
    private FirebaseFirestore mDatabase;
    private FirebaseUser mUser;
    private Room mRoom;
    private RankingAdapter mAdapter;
    private ArrayList<RankingModel> mRankingModelList;
    private RecyclerView mRankingRecycler;
    private boolean isClosed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Toolbar toolbar = findViewById(R.id.room_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setNavigationContentDescription(getString(R.string.close_details));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mDatabase = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mRoom = getIntent().getParcelableExtra(ROOM);
        mRoomName = findViewById(R.id.room_name);
        mRoomCode = findViewById(R.id.room_code);
        mRoomRanking = findViewById(R.id.room_ranking_label);
        mRankingRecycler = findViewById(R.id.room_recyclerview);
        mRoomCreator = findViewById(R.id.room_creator);
        mRoomName.setText(mRoom.getRoomName());
        mRoomCreator.setText(mRoom.getRoomCreator());
        mRoomCode.setText(mRoom.getIdCode());

        mRankingModelList = new ArrayList<>();
        mAdapter = new RankingAdapter(mRankingModelList);
        mRankingRecycler.setLayoutManager(new LinearLayoutManager(RoomActivity.this));
        mRankingRecycler.setAdapter(mAdapter);

        setTransitionListener();

        selectSharedPoint();
    }

    private void setTransitionListener() {
        Transition transition = getWindow().getSharedElementEnterTransition();
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                Log.d(TAG, "Transizione avviata");
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                Log.d(TAG, "Transizione conclusa");
                if (!isClosed) {
                    mRoomCode.setVisibility(View.VISIBLE);
                    mRoomRanking.setVisibility(View.VISIBLE);
                    mRankingRecycler.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                Log.d(TAG, "Transizione cancellata");
            }

            @Override
            public void onTransitionPause(Transition transition) {
                Log.d(TAG, "Transizione in pausa");
            }

            @Override
            public void onTransitionResume(Transition transition) {
                Log.d(TAG, "Transizione ripristinata");
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        closeActivity();
        super.onBackPressed();
    }

    private void closeActivity() {
        mRoomCode.setVisibility(View.GONE);
        mRoomRanking.setVisibility(View.GONE);
        mRankingRecycler.setVisibility(View.GONE);
        mRoomCreator.setVisibility(View.GONE);
        isClosed = true;
    }

    private void selectSharedPoint() {
        for (final String user : mRoom.getMembers()) {
            mDatabase.collection("user").document(user).collection("sharedValue")
                    .document("value").get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Log.d(TAG, "User: " + user);
                            HashMap<String, Object> hashMap = (HashMap<String, Object>) documentSnapshot.getData();
                            Log.d(TAG, "HashMap: " + hashMap.get("SharedPoint"));
                            RankingModel rankingModel = new RankingModel(user, (Double) hashMap.get("SharedPoint"));
                            mRankingModelList.add(rankingModel);
                            Collections.sort(mRankingModelList);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }
}
