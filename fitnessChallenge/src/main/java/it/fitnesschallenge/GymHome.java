package it.fitnesschallenge;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import it.fitnesschallenge.model.User;

import static it.fitnesschallenge.model.SharedConstance.PLAYING_WORKOUT;

public class GymHome extends Fragment {

    private static final String USER = "user";

    private User mUser;


    public GymHome() {
        // Required empty public constructor
    }

    static GymHome newInstance(User user) {
        GymHome fragment = new GymHome();
        Bundle args = new Bundle();
        args.putParcelable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getParcelable(USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gym_home, container, false);

        Button openWorkoutStatistics = view.findViewById(R.id.statistics_oper_button);
        Button startWorkoutStatistics = view.findViewById(R.id.start_training_button);

        startWorkoutStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkoutIndoorList workoutIndoorList = WorkoutIndoorList.newInstance(mUser);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                transaction.replace(R.id.fragmentContainer, workoutIndoorList, PLAYING_WORKOUT)
                        .addToBackStack(PLAYING_WORKOUT)
                        .commit();
            }
        });

        openWorkoutStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StatisticsRoomActivity.class);
                intent.putExtra(USER, mUser);
                getActivity().startActivity(intent);
            }
        });
        return view;
    }

}
