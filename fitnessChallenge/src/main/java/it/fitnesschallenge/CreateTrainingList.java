package it.fitnesschallenge;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.fitnesschallenge.model.room.entity.PersonalExercise;
import it.fitnesschallenge.model.view.CreationViewModel;

import static it.fitnesschallenge.model.SharedConstance.ADD_EXERCISE_TO_LIST;
import static it.fitnesschallenge.model.SharedConstance.ADD_FINISH_DATE;
import static it.fitnesschallenge.model.SharedConstance.ADD_USERNAME_AND_START_DATE;
import static it.fitnesschallenge.model.SharedConstance.CREATE_TRAINING_LIST;
import static it.fitnesschallenge.model.SharedConstance.UPLOAD_NEW_WORKOUT;

public class CreateTrainingList extends Fragment {

    private static final String TAG = "CreateTrainingList";
    private CreationViewModel mViewModel;
    private ProgressBar mProgressBar;
    private TextView mProgressTextView;
    private TextView mPrevText;
    private TextView mNextText;
    private ImageButton mNext;
    private ImageButton mPrev;
    private FloatingActionButton mAddExerciseFAB;

    public CreateTrainingList() {
        //empty creation method needed
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_training_list, container, false);
        mProgressBar = view.findViewById(R.id.create_list_progress_bar);
        mProgressTextView = view.findViewById(R.id.create_list_percent);
        mPrevText = view.findViewById(R.id.previous_text);
        mNext = view.findViewById(R.id.right_key_arrow);
        mPrev = view.findViewById(R.id.left_key_arrow);
        mNextText = view.findViewById(R.id.next_text);
        mAddExerciseFAB = view.findViewById(R.id.add_exercise_FAB);

        //collego il View model al Fragment
        mViewModel = ViewModelProviders.of(getActivity()).get(CreationViewModel.class);
        mViewModel.getLiveDataProgress().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (mProgressBar != null && mProgressTextView != null) {
                    Log.d(TAG, "Nuovo progresso: " + integer);
                    Log.d(TAG, "ProgressBar getProgress(): " + mProgressBar.getProgress());
                    ValueAnimator animator = ValueAnimator.ofInt(mProgressBar.getProgress(), integer);
                    animator.setDuration(500)
                            .setInterpolator(new DecelerateInterpolator());
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int progress = (int) animation.getAnimatedValue();
                            mProgressBar.setProgress(progress);
                            mProgressTextView.setText(NumberFormat.getInstance().format(progress));
                        }
                    });
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mProgressTextView.setText(NumberFormat
                                    .getNumberInstance()
                                    .format(mProgressBar.getProgress()));
                        }
                    });
                    animator.start();
                } else {
                    Log.d(TAG, "Change detected on progress: " + integer);
                }
            }
        });

        mViewModel.getLiveDataSteps().observe(getViewLifecycleOwner(), new Observer<ArrayList<Integer>>() {
            @Override
            public void onChanged(ArrayList<Integer> integers) {
                setCurrentStep(integers);
                Log.d(TAG, "Step changed: " + integers.size());
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.inner_frame_creation_list).getTag();
                switch (currentFragment) {
                    case ADD_USERNAME_AND_START_DATE:
                        if (!checkFirstStepCompleted())
                            mViewModel.nextStep();
                        break;
                    case ADD_EXERCISE_TO_LIST:
                        if (!checkSecondStepCompleted())
                            mViewModel.nextStep();
                        break;
                    case ADD_FINISH_DATE:
                        if (!checkThirdStepCompleted())
                            mViewModel.nextStep();
                        break;
                }
            }
        });
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.setIsError(false);
                mViewModel.prevStep();
            }
        });

        mAddExerciseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddExerciseToList addExerciseToList = AddExerciseToList.newInstance(CREATE_TRAINING_LIST);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                        .replace(R.id.fragmentContainer, addExerciseToList, ADD_EXERCISE_TO_LIST)
                        .addToBackStack(ADD_EXERCISE_TO_LIST)
                        .commit();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    /**
     * Questo metodo modifica il fragment nidificato a seconda dello step di creazione in cui si trova
     * l'utente.
     *
     * @param integers contiene lo step di creazione, in un range da [0-4]
     */
    private void setCurrentStep(ArrayList<Integer> integers) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
        Log.d(TAG, "Current step: " + integers.size());
        Log.d(TAG, "Error: " + mViewModel.isError());
        switch (integers.size()) {
            case 0:
            case 1:
                if (!mViewModel.getIsError().getValue()) {
                    AddUserNameAndStartDate addUserNameAndStartDate = new AddUserNameAndStartDate();
                    transaction.replace(R.id.inner_frame_creation_list, addUserNameAndStartDate, ADD_USERNAME_AND_START_DATE)
                            .commit();
                    mPrev.setVisibility(View.GONE);
                    mPrevText.setVisibility(View.GONE);
                    mViewModel.setLiveDataProgress(1);
                    mAddExerciseFAB.setVisibility(View.GONE);
                }
                break;
            case 2:
                if (!mViewModel.getIsError().getValue()) {
                    mPrev.setVisibility(View.VISIBLE);
                    mPrevText.setVisibility(View.VISIBLE);
                    ExerciseList exerciseList = new ExerciseList();
                    transaction.replace(R.id.inner_frame_creation_list, exerciseList, ADD_EXERCISE_TO_LIST)
                            .commit();
                    mViewModel.setLiveDataProgress(33);
                    mAddExerciseFAB.setVisibility(View.VISIBLE);
                }
                break;
            case 3:
                if (!mViewModel.getIsError().getValue()) {
                    if (mNext.getVisibility() == View.GONE) {
                        mNext.setVisibility(View.VISIBLE);
                        mNextText.setVisibility(View.VISIBLE);
                    }
                    AddFinishDate finishDate = new AddFinishDate();
                    transaction.replace(R.id.inner_frame_creation_list, finishDate, ADD_FINISH_DATE)
                            .commit();
                    mViewModel.setLiveDataProgress(66);
                    mAddExerciseFAB.setVisibility(View.GONE);
                }
                break;
            case 4:
                mNext.setVisibility(View.GONE);
                mNextText.setVisibility(View.GONE);
                UploadNewWorkout uploadNewWorkout = new UploadNewWorkout();
                transaction.replace(R.id.inner_frame_creation_list, uploadNewWorkout, UPLOAD_NEW_WORKOUT)
                        .commit();
                mViewModel.setLiveDataProgress(100);
                break;
            default:
                Log.d(TAG, "Non dovrebbe esserci");
        }
    }

    /** Questi metodi in successione permettono di controllare il primo.
     * @return true se è stato completato correttamente.
     */
    private boolean checkFirstStepCompleted() {
        Log.d(TAG, "Verifico il completamento del primo step");
        Log.d(TAG, "Email: " + mViewModel.getEmail().getValue() + "\n" +
                "\t Goal: " + mViewModel.getGoal().getValue() + "\n" +
                "\t Data: " + mViewModel.getStartDate().getValue());
        if (mViewModel.getEmail().getValue() == null
                || mViewModel.getGoal().getValue() == null) {
            mViewModel.setIsError(true);
            return true;
        } else {
            mViewModel.setIsError(false);
            return false;
        }
    }

    /**
     * Secondo step.
     * @return true se è stato competato correttamtne.
     */
    private boolean checkSecondStepCompleted() {
        Log.d(TAG, "Controllo correttezza secondo step");
        List<PersonalExercise> personalExerciseList = mViewModel.getPersonalExerciseList().getValue();
        if (personalExerciseList == null) {
            mViewModel.setIsError(true);
            return true;
        }

        for (PersonalExercise personalExercise : personalExerciseList) {
            if (personalExercise.isDeleted())
                personalExerciseList.remove(personalExercise);
        }
        mViewModel.setPersonalExerciseList(personalExerciseList);
        if (personalExerciseList.size() < 1) {
            Log.d(TAG, "Non ci sono esercizi");
            mViewModel.setIsError(true);
            return true;
        } else {
            Log.d(TAG, "Ci sono esercizi");
            mViewModel.setIsError(false);
            return false;
        }
    }

    /**
     * Terzo step.
     * @return true se è stato completato correttamente.
     */
    private boolean checkThirdStepCompleted() {
        Log.d(TAG, "Controllo correttezza terzo step");
        Date date = mViewModel.getFinishDate().getValue();
        if (date == null) {
            mViewModel.setIsError(true);
            return true;
        } else {
            mViewModel.setIsError(false);
            return false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mViewModel.resetLiveData();
    }
}
