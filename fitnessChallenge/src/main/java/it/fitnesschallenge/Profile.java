package it.fitnesschallenge;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import it.fitnesschallenge.model.User;

import static it.fitnesschallenge.model.SharedConstance.LOGIN_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.PROFILE_FRAGMENT;

public class Profile extends Fragment {

    private static final String TAG = "Profile";

    private ImageView mImageView;
    private MaterialButton mLoginButton;
    private TextView mUserMail;
    private TextView mName;
    private TextView mSurname;
    private TextView mRole;

    private FirebaseFirestore mDatabase;
    private FirebaseUser mUser;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mImageView = view.findViewById(R.id.profile_image);
        mLoginButton = view.findViewById(R.id.profile_log_in_button);
        mUserMail = view.findViewById(R.id.profile_email);
        mName = view.findViewById(R.id.profile_name);
        mSurname = view.findViewById(R.id.profile_last_name);
        mRole = view.findViewById(R.id.profile_role);

        mDatabase = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login login = Login.newInstance(PROFILE_FRAGMENT);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit,
                        R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                        .replace(R.id.fragmentContainer, login, LOGIN_FRAGMENT)
                        .addToBackStack(LOGIN_FRAGMENT)
                        .commit();
            }
        });

        if (mUser != null) {
            Log.d(TAG, "Utente firebase");
            mLoginButton.setVisibility(View.GONE);
            getUserFromFireBase();
        }
        return view;
    }

    private void getUserFromFireBase() {
        mDatabase.collection("user").document(mUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                mUserMail.setText(user.getUsername());
                mName.setText(user.getNome());
                mSurname.setText(user.getSurname());
                mRole.setText(user.getRole());
            }
        });
    }
}
