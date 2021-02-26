package it.fitnesschallenge;


import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static it.fitnesschallenge.model.SharedConstance.ENTER_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.GYM_HOME_FRAGMENT;
import static it.fitnesschallenge.model.SharedConstance.LOGIN_FRAGMENT;

public class GymEnter extends Fragment implements NfcAdapter.CreateNdefMessageCallback {

    private static final String TAG = "GymEnter";

    private Context mContext;
    private ImageView mImage;

    public GymEnter() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        HomeActivity.setCurrentFragment(ENTER_FRAGMENT);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gym_enter, container, false);
        mImage = view.findViewById(R.id.gym_enter_nfc);
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());
        nfcAdapter.setNdefPushMessageCallback(this, getActivity());
        nfcAdapter.setOnNdefPushCompleteCallback(new NfcAdapter.OnNdefPushCompleteCallback() {
            @Override
            public void onNdefPushComplete(NfcEvent event) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImage.setImageResource(R.drawable.ic_check_circle_100dp);
                        Login login = Login.newInstance(GYM_HOME_FRAGMENT);
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit, R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                                .replace(R.id.fragmentContainer, login, LOGIN_FRAGMENT)
                                .addToBackStack(LOGIN_FRAGMENT)
                                .commit();
                    }
                });
            }
        }, getActivity());
        return view;
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        NdefMessage msg = new NdefMessage(NdefRecord.createMime("application/vnd.it.fitnesschallege.nfc.reciver", user.getEmail().getBytes()));
        return msg;
    }
}
