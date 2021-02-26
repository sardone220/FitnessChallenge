/**
 * Questa activity permette di creare una nuova room, e contemporaneamente salvare la propria partecipazione
 * alla stessa da entrabe le parti, ovvero nella lista di partecipazioni nel documento dell'utente e
 * nella lista di partecipanti della room stessa.
 * Inoltre mantiene sincronizzati gli indici su Algolia per poter effettuare le ricerche sul nome della
 * room successivamente.
 */
package it.fitnesschallenge;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.fitnesschallenge.model.AlgoliaApiKeys;
import it.fitnesschallenge.model.Participation;
import it.fitnesschallenge.model.Room;

public class NewRoomActivity extends AppCompatActivity {

    private static final String TAG = "NewRoomActivity";

    private FloatingActionButton mFab;
    private TextInputLayout mRoomNameInput;
    private TextView mGeneratedCode;
    private ProgressBar mProgressBar;
    private TextView mGeneratedCodeLabel;
    private FirebaseFirestore mDatabase;
    private FirebaseUser mUser;
    private AlgoliaApiKeys mApiKeys;
    private Client mClient;
    private Index mIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_room);

        Toolbar toolbar = findViewById(R.id.new_room_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.setTitle(getString(R.string.new_room));
        toolbar.setNavigationContentDescription(R.string.close_new_room);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mFab = findViewById(R.id.new_room_save_FAB);
        mRoomNameInput = findViewById(R.id.new_room_room_name);
        mGeneratedCode = findViewById(R.id.new_room_generated_code);
        mProgressBar = findViewById(R.id.new_room_progress_bar);
        mGeneratedCodeLabel = findViewById(R.id.new_room_generated_code_label);

        ConnectivityManager connectivityManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting()) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveNewRoom();
                }
            });
            mDatabase = FirebaseFirestore.getInstance();
            mUser = FirebaseAuth.getInstance().getCurrentUser();
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.ops)
                    .setMessage(R.string.connection_error_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }

        /*
         * Questo metodo permette di ottenere i riferimenti ad Algolia che sono stati salvati su Firebase
         * per una questione di sicirezza dell'applicazione stessa.
         */
        mDatabase.collection("AlgoliaApi").document("apiCode").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        mApiKeys = documentSnapshot.toObject(AlgoliaApiKeys.class);
                        mClient = new Client(mApiKeys.getClientCode(), mApiKeys.getApiKey());
                        mIndex = mClient.getIndex("rooms_name");
                        Log.d(TAG, "ClientCode: " + mApiKeys.getClientCode());
                        Log.d(TAG, "ApiKey: " + mApiKeys.getApiKey());
                    }
                });
    }

    /**
     * Questo metodo permette di savare una nuova room in firebase generando prima di tutto la chiave
     * della room basata sul timestamp del dispositivo, e poi inserendo la classe Room.
     */
    private void saveNewRoom() {
        mProgressBar.setVisibility(View.VISIBLE);
        String roomName = mRoomNameInput.getEditText().getText().toString().trim();
        if (!roomName.isEmpty()) {
            final String generatedCode = createNewId();
            Room room = new Room(generatedCode, roomName, mUser.getEmail());
            room.addMembers(mUser.getEmail());
            mDatabase.collection("room").document(generatedCode).set(room)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mGeneratedCode.setText(generatedCode);
                            saveRoomOnAlgolia();
                            setParticipation(generatedCode);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mGeneratedCode.setTextColor(Color.RED);
                            mGeneratedCode.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_error,
                                    0, 0, 0);
                            mGeneratedCode.setText(R.string.upload_room_failed);
                            e.printStackTrace();
                        }
                    });
        }
    }

    /**
     * Questo metodo permette di inserire nel docuemento all'utente su firebase il riferimento alla
     * room appena create, in modo da indicare che l'utente che crea la room, vi partecipa, inoltre
     * distingue l'inserimento di una nuova partecipazione della creazione di una lista di
     * partecipazioni, indirizzando il codice su due metodi differenti a seconda dei casi.
     *
     * @param generatedCode contiene la key della room.
     */
    private void setParticipation(final String generatedCode) {
        mDatabase.collection("user/").document(mUser.getEmail())
                .collection("/participation")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG, "Documenti prelevati da FireBase");
                ArrayList<String> roomsList = new ArrayList<>();
                if (queryDocumentSnapshots.getDocuments().size() > 0) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Participation participation = documentSnapshot.toObject(Participation.class);
                        roomsList = participation.getRoomsList();
                        roomsList.add(generatedCode);
                        participation.setRoomsList(roomsList);
                        updateDocument(participation, documentSnapshot.getId());
                    }
                } else {
                    roomsList.add(generatedCode);
                    createNewDocument(new Participation(roomsList));
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Errore connessione");
                    }
                });
    }

    /**
     * Questo metodo permette di aggiornare la lista di partecipazioni dell'utente perlevando la lista
     * già esistente nel documento dell'utente e aggiungendo il nuovo item.
     * @param participation contiene la lista di partecipazioni
     * @param id contiene l'id della nuova room.
     */
    private void updateDocument(Participation participation, String id) {
        if (participation != null) {
            try {
                mDatabase.collection("user").document(mUser.getEmail())
                        .collection("participation").document(id)
                        .update("roomsList", participation.getRoomsList())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mProgressBar.setVisibility(View.GONE);
                                showSuccessDialog();
                            }
                        });
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(TAG, "user/" + mUser.getEmail() + "/participation/");
                Toast.makeText(this, "user/" + mUser.getEmail() + "/participation/", Toast.LENGTH_LONG).show();
            }
            Log.d(TAG, "Aggiorno il documento");
        }
    }

    /**
     * In questo metodo viene creata una nuova lista di riferimenti alle room e salvata su Firebase.
     * @param participation contiene la lista di partecipazioni aggiornata con la prima room da inserire.
     */
    private void createNewDocument(Participation participation) {
        if (participation != null) {
            mDatabase.collection("user").document(mUser.getEmail())
                    .collection("participation").add(participation)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            mProgressBar.setVisibility(View.GONE);
                            showSuccessDialog();
                        }
                    });
            Log.d(TAG, "Aggiorno il documento");
        }
    }

    /**
     * Questo metodo permette di salvare il nome della nuova room su Algolia in modo da poter effettuare
     * successivamente le ricerche tramite il nome della room
     */
    private void saveRoomOnAlgolia() {
        List<JSONObject> array = new ArrayList<>();
        try {
            array.add(new JSONObject().put("roomName",
                    mRoomNameInput.getEditText().getText().toString().trim()));
            mIndex.addObjectsAsync(new JSONArray(array), null);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void showSuccessDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.success)
                .setMessage(R.string.upload_complete_successful)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.show();
    }

    private String createNewId() {
        return Long.toString(System.currentTimeMillis());
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        closeActivity();
        super.onBackPressed();
    }

    private void closeActivity() {
        mFab.setVisibility(View.GONE);
        mRoomNameInput.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mGeneratedCode.setVisibility(View.GONE);
        mGeneratedCodeLabel.setVisibility(View.GONE);
    }
}
