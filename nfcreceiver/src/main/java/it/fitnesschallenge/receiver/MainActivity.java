package it.fitnesschallenge.receiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.setNdefPushMessageCallback(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void processIntent(Intent intent) {
        TextView textView = findViewById(R.id.loggin_user);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        textView.setText(new String(msg.getRecords()[0].getPayload()));
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        return null;
    }
}
