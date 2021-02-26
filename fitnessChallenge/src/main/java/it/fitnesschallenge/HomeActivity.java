package it.fitnesschallenge;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import it.fitnesschallenge.model.room.entity.Exercise;
import it.fitnesschallenge.model.view.HomeViewModel;

import static it.fitnesschallenge.model.SharedConstance.HOME_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.LAST_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.PROFILE_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.SETTING_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.TIMER_FRAGMENT;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private Home mHomeFragment;
    private BottomNavigationView mBottomNavigation;
    private static ImageButton mBackButton;
    private HomeViewModel homeViewModel;
    private static Context mContext;
    private boolean isHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        homeViewModel.getExerciseList().observe(this, new Observer<List<Exercise>>() {
            @Override
            public void onChanged(List<Exercise> exercises) {
                Log.d(TAG, "Esercizi prelevati dal DB: " + exercises.size());
            }
        });
        mBottomNavigation = findViewById(R.id.bottom_navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        mBackButton = findViewById(R.id.btn_back);
        mBackButton.setVisibility(View.GONE);
        mContext = this;

        if (savedInstanceState == null) {
            mBottomNavigation.setSelectedItemId(R.id.navigation_home);
            mHomeFragment = new Home();
            isHome = true;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, mHomeFragment, HOME_FRAGMENT)
                    .addToBackStack(HOME_FRAGMENT)
                    .commit();
        } else {
            String lastFragment = savedInstanceState.getString(LAST_FRAGMENT);
            setCurrentFragment(lastFragment);
        }

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().popBackStackImmediate();
                try {
                    Log.d(TAG, "back button pressed");
                    setCurrentFragment(getSupportFragmentManager().findFragmentById(R.id.fragmentContainer).getTag());
                } catch (NullPointerException ex) {
                    Toast.makeText(mContext, mContext.getString(R.string.shit_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    String selectedFragmentTag = null;
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction;
                    transaction = manager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            selectedFragment = new Home();
                            selectedFragmentTag = HOME_FRAGMENT;
                            break;
                        case R.id.navigation_timer:
                            selectedFragment = new Timer();
                            selectedFragmentTag = TIMER_FRAGMENT;
                            break;
                        case R.id.navigation_profile:
                            selectedFragment = new Profile();
                            selectedFragmentTag = PROFILE_FRAGMENT;
                            break;
                        case R.id.navigation_settings:
                            selectedFragment = new it.fitnesschallenge.Settings();
                            selectedFragmentTag = SETTING_FRAGMENT;
                            break;
                    }
                    try {
                        if (!manager.findFragmentById(R.id.fragmentContainer).getTag().equals(selectedFragmentTag)) {
                            Log.d(TAG, "Niente da modificare, si sta cercando di invocare lo stesso fragment");
                            transaction.replace(R.id.fragmentContainer, selectedFragment, selectedFragmentTag)
                                    .addToBackStack(selectedFragmentTag)
                                    .commit();
                            setCurrentFragment(selectedFragmentTag);
                        }
                    } catch (NullPointerException ex) {
                        Log.d(TAG, "Primo avvio dell'applicazione, nessun fragment impostato");
                    }
                    return true;
                }
            };


    public static void setCurrentFragment(String currentFragment) {
        Log.d(TAG, "SetCurrentFragment on: " + currentFragment);
        if (HOME_FRAGMENT.equals(currentFragment)) {
            mBackButton.setVisibility(View.GONE);
        } else {
            mBackButton.setVisibility(View.VISIBLE);
        }
    }

    public static Context getHomeActivityContext() {
        return mContext;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() <= 0)
            finish();
    }

}
