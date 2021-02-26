package it.fitnesschallenge;

import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import static it.fitnesschallenge.model.SharedConstance.ENTER_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.GYM_HOME_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.HOME_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.LOGIN_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.TRAINER_HOME_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.USE_NFC;
import static it.fitnesschallenge.model.SharedConstance.WORKOUT_FRAGMENT;


public class Home extends Fragment{

    private static final String TAG = "Home";

    public Home() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        Log.d(TAG, "Home set button GONE");
        HomeActivity.setCurrentFragment(HOME_FRAGMENT);
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        MaterialButton enterGymButton = view.findViewById(R.id.statistics_oper_button);
        MaterialButton startOutdoor = view.findViewById(R.id.start_training_button);
        MaterialButton login = view.findViewById(R.id.workout_home_create_new_workout);

        enterGymButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                if (NfcAdapter.getDefaultAdapter(getContext()) != null && preferences.getBoolean(USE_NFC, false))
                    fragment = new GymEnter();
                else
                    fragment = Login.newInstance(GYM_HOME_FRAGMENT);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit,
                        R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                transaction.replace(R.id.fragmentContainer, fragment, ENTER_FRAGMENT)
                        .addToBackStack(ENTER_FRAGMENT)
                        .commit();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login login = Login.newInstance(TRAINER_HOME_FRAGMENT);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit,
                        R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                transaction.replace(R.id.fragmentContainer, login, LOGIN_FRAGMENT)
                        .addToBackStack(LOGIN_FRAGMENT)
                        .commit();
            }
        });


        startOutdoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkoutHome workoutHome = new WorkoutHome();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                transaction.replace(R.id.fragmentContainer, workoutHome, WORKOUT_FRAGMENT)
                        .addToBackStack(WORKOUT_FRAGMENT)
                        .commit();
            }
        });

        return view;
    }

}
