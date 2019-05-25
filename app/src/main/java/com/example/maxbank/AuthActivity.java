package com.example.maxbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.maxbank.repositories.FireStoreRepo;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FireStoreRepo fireStoreRepo;

    private final int RC_SIGN_IN = 1;

    private View mView;

    private TextView title;
    private TextView info;
    private TextView email;
    private Button btnLogin;
    private Button btnCreateUser;

    // Made as an list to easily enable further login options down the road.
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Slightly hacky way to do this
        mView = getWindow().getDecorView().getRootView();

        fireStoreRepo = new FireStoreRepo();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        initViews();
        updateViews();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        View contextView = findViewById(R.id.container);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                updateViews();
                openMainActivity();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if(response == null){
                    Snackbar.make(contextView, R.string.login_failed, Snackbar.LENGTH_SHORT).show();
                } else {
                    try {
                        Log.w(TAG, "Error when trying to login. ErrorCode:" + response.getError().getErrorCode());
                    } catch (NullPointerException nPEX){
                        Log.w(TAG, "Error when trying to login. ErrorCode: Unknown.");
                    }

                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    private void initViews() {
        title = findViewById(R.id.title);
        title.setText(R.string.app_name);

        info = findViewById(R.id.info);
        email = findViewById(R.id.email);

        btnCreateUser = findViewById(R.id.button_create_user);
        btnCreateUser.setOnClickListener(onClickListener());

        btnLogin = findViewById(R.id.button_login);
        btnLogin.setOnClickListener(onClickListener());

    }

    private void updateViews(){
        if(mAuth.getCurrentUser() != null){
            String infoText = getResources().getString(R.string.info_text_loggedin) + ":";
            info.setText(infoText);
            email.setText(currentUser.getEmail());
            btnLogin.setText(R.string.login_continue);
            btnCreateUser.setText(R.string.create_user_logout);
        } else {
            info.setText(getResources().getString(R.string.info_text));
            email.setText("");
            btnLogin.setText(R.string.login);
            btnCreateUser.setText(R.string.create_user);
        }
    }

    private View.OnClickListener onClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.button_login){
                    if(btnLogin.getText().toString().equals(getResources().getString(R.string.login_continue))){
                        openMainActivity();
                    } else {
                        startActivityForResult(AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setAvailableProviders(providers)
                                        .build(),
                                RC_SIGN_IN);
                    }
                }
                else if(v.getId() == R.id.button_create_user){
                    if(btnCreateUser.getText().toString().equals(getResources().getString(R.string.create_user_logout))){
                        signOut();
                    } else {
                        createUserForm();
                    }

                }
            }
        };
    }

    private void openMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void createUserForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom).setTitle(R.string.create_user_title);
        final View viewInflated = LayoutInflater.from(this).inflate(R.layout.create_user_form, (ViewGroup) mView , false);
        builder.setView(viewInflated);

        final TextInputEditText inputUserName = viewInflated.findViewById(R.id.create_user_name);

        final TextInputEditText inputUserDayOfBirth = viewInflated.findViewById(R.id.create_user_day_of_birth);
        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateInputDayOfBirth(inputUserDayOfBirth, myCalendar);
            }

        };

        inputUserDayOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(mView.getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final TextInputEditText inputUserEmail =  viewInflated.findViewById(R.id.create_user_email);
        final TextInputEditText inputUserPassword = viewInflated.findViewById(R.id.create_user_password);

        builder.setMessage(R.string.create_user_description)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = inputUserName.getText().toString();
                        String dayOfBirth = inputUserDayOfBirth.getText().toString();
                        String email = inputUserEmail.getText().toString();
                        String password = inputUserPassword.getText().toString();
                        // checking if inputs are empty before calling the repo to store the account
                        if(!email.equals("") &&
                                !password.equals("")){
                            createUser(name, dayOfBirth, email, password);
                        }
                    }
                }).show();

    }


    private void signOut(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        updateViews();
                    }
                });
    }

    private void updateInputDayOfBirth(TextInputEditText v, Calendar myCalendar) {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        v.setText(sdf.format(myCalendar.getTime()));
    }

    private void createUser(final String name, String dayOfBirth,String email, String password){
        try {
            final Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dayOfBirth);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                currentUser = mAuth.getCurrentUser();

                                // Create user in FireStore
                                fireStoreRepo.saveUser(currentUser.getUid(), name, date, "Ingen");

                                // Add initial accounts(default & budget)
                                //fireStoreRepo.saveAccount(currentUser.getUid(), "Privat konto", "default", 0);
                                //fireStoreRepo.saveAccount(currentUser.getUid(), "Budget konto", "budget", 0);
                                updateViews();
                                openMainActivity();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Snackbar.make(mView, R.string.create_user_failed, Snackbar.LENGTH_LONG);
                            }

                            // ...
                        }
                    });
        } catch (ParseException e) {
            Log.w(TAG, "Could not parse " + dayOfBirth + " to Date.");
        }

    }
}
