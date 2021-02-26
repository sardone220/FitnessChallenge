package it.fitnesschallenge;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class Settings extends PreferenceFragmentCompat {

    public Settings() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);
    }
}
