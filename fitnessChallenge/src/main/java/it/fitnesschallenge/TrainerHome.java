package it.fitnesschallenge;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;

import it.fitnesschallenge.model.User;

import static it.fitnesschallenge.model.SharedConstance.CREATE_TRAINING_LIST;
import static it.fitnesschallenge.model.SharedConstance.LOGGED_IN;
import static it.fitnesschallenge.model.SharedConstance.SHARED_PREFERENCES;

public class TrainerHome extends Fragment {

    private static final String USER_INSTANCE = "userInstance";

    private User mUser;

    public TrainerHome() {
        // Required empty public constructor
    }

    public static TrainerHome newInstance(User user) {
        TrainerHome fragment = new TrainerHome();
        Bundle args = new Bundle();
        args.putParcelable(USER_INSTANCE, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getParcelable(USER_INSTANCE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trainer_home, container, false);
        TextView trainerTextName = view.findViewById(R.id.trainer_home_welcome_message);
        MaterialButton openTrainigList = view.findViewById(R.id.show_training_card_btn);
        MaterialButton createTrainingListButton = view.findViewById(R.id.create_training_card_button);


        trainerTextName.setText(mUser.getNome());

        createTrainingListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateTrainingList createTrainingList = new CreateTrainingList();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                transaction.replace(R.id.fragmentContainer, createTrainingList, CREATE_TRAINING_LIST)
                        .addToBackStack(CREATE_TRAINING_LIST)
                        .commit();
            }
        });




        openTrainigList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserListActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOGGED_IN, false);
        editor.apply();
    }
}
