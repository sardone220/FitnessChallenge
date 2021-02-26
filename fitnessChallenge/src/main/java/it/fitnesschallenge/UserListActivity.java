package it.fitnesschallenge;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.fitnesschallenge.adapter.UserItemAdapter;

public class UserListActivity extends AppCompatActivity {

    private List<String> arrayListUser = new ArrayList<>();
    private UserItemAdapter mUserItemAdapter;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar toolbar = findViewById(R.id.user_list_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.user_list);

        if (findViewById(R.id.user_detail_container) != null) {
            mTwoPane = true;
        }

        mUserItemAdapter = new UserItemAdapter(this, arrayListUser, mTwoPane);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mUserItemAdapter);

        /**
         * otteniamo collection user firebase
         */

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        arrayListUser.add(documentSnapshot.getId());
                        mUserItemAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
