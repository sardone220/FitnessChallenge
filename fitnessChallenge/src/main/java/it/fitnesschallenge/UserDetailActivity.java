package it.fitnesschallenge;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import static it.fitnesschallenge.model.SharedConstance.USER_DETAIL_FRAGMENT;

public class UserDetailActivity extends AppCompatActivity {

    private static final String TAG = "UserDetailActivity";
    private static final String ARG_ITEM_ID = "itemId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        Toolbar toolbar = findViewById(R.id.user_detail_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (savedInstanceState == null) {
            Log.d(TAG, "Chiamo il fragment");
            String userId = getIntent().getStringExtra(ARG_ITEM_ID);
            getSupportActionBar().setTitle(userId);
            UserDetailFragment userDetailFragment = UserDetailFragment.newInstance(userId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.user_detail_container, userDetailFragment, USER_DETAIL_FRAGMENT)
                    .commit();
        }
    }
}
