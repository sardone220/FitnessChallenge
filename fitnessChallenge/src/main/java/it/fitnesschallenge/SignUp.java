package it.fitnesschallenge;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import it.fitnesschallenge.model.User;

public class SignUp extends Fragment {

    private static String[] ROLE;
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputLayout mEmailTextInput;
    private TextInputLayout mPasswordTextInput;
    private TextInputLayout mRePasswordTextInput;
    private TextInputLayout mNomeTextInput;
    private TextInputLayout mSurnameTextInput;
    private TextInputLayout mRoleTextInput;
    private ProgressBar mProgressBar;
    private FirebaseAuth mAuth;

    private static final String TAG = "SignUp";

    public SignUp() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_sign_up, container, false);
        mEmailTextInput = view.findViewById(R.id.email_text_input);
        mPasswordTextInput = view.findViewById(R.id.password_text_input);
        mRePasswordTextInput = view.findViewById(R.id.re_password_text_input);
        mNomeTextInput = view.findViewById(R.id.name_text_input);
        mSurnameTextInput = view.findViewById(R.id.surname_text_input);
        mRoleTextInput = view.findViewById(R.id.role_text_input);
        mProgressBar = view.findViewById(R.id.sign_up_progress_bar);
        AutoCompleteTextView roleAutoComplete = view.findViewById(R.id.dropdown_role);
        MaterialButton signIn = view.findViewById(R.id.sign_in);

        ROLE = new String[] {getContext().getString(R.string.trainer),
                getContext().getString(R.string.user),
                getContext().getString(R.string.trainer_user)};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.drop_down_single_layout,
                ROLE);
        roleAutoComplete.setAdapter(adapter);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                String email = mEmailTextInput.getEditText().getText().toString();
                String password = mPasswordTextInput.getEditText().getText().toString();
                String rePassword = mRePasswordTextInput.getEditText().getText().toString();
                String nome = mNomeTextInput.getEditText().getText().toString();
                String surname = mSurnameTextInput.getEditText().getText().toString();
                String role = mRoleTextInput.getEditText().getText().toString();
                validateForm(email, password, rePassword, nome, surname, role);
            }
        });

        return view;
    }

    private void validateForm(String email, String password, String rePassword,
                              String nome, String cognome, String role){
        if(email.isEmpty()){
            mEmailTextInput.setError(getString(R.string.complete_correctly_field));
            return;
        }
        if(password.isEmpty() && rePassword.isEmpty() && password.equals(rePassword)){
            mPasswordTextInput.setError(getString(R.string.complete_correctly_field));
            mRePasswordTextInput.setError(getString(R.string.complete_correctly_field));
            return;
        }
        if(nome.isEmpty()){
            mNomeTextInput.setError(getString(R.string.complete_correctly_field));
            return;
        }
        if(cognome.isEmpty()){
            mSurnameTextInput.setError(getString(R.string.complete_correctly_field));
            return;
        }
        if(role.isEmpty()){
            mRoleTextInput.setError(getString(R.string.complete_correctly_field));
            return;
        }

        registerUser(email, password, nome, cognome, role);
    }

    private void registerUser(final String email, final String password, final String nome,
                              final String surname, final String role) {
        Log.d(TAG, "Registro l'utente");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "Utente correttamente registrato.");
                        sendDataToFirebase(new User(email, nome, surname, role));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mPasswordTextInput.setError(e.getMessage());
                        mRePasswordTextInput.setError(e.getMessage());
                        mProgressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Exception throw", e);
                    }
                });
    }

    private void sendDataToFirebase(User user){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("user")
                .document(user.getUsername())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar.setVisibility(View.GONE);
                        Log.d(TAG, "Data correctly upload");
                        new MaterialAlertDialogBuilder(getContext())
                                .setTitle("Success")
                                .setMessage(getContext().getString(R.string.sign_in_successfully))
                                .show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().getSupportFragmentManager().popBackStackImmediate();
                            }
                        }, 500);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        new MaterialAlertDialogBuilder(getContext())
                                .setTitle(getContext().getString(R.string.sign_in_fail))
                                .show();
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

}
