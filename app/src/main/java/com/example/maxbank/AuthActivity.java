package com.example.maxbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.maxbank.fragments.dialogs.CreateUserDialogFragment;
import com.example.maxbank.repositories.FireStoreRepo;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";
    private final int RC_SIGN_IN = 1;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FireStoreRepo fireStoreRepo;

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
        setTheme(R.style.AppTheme);
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
    protected void onStart() {
        super.onStart();
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
                        showCreateUserDialog();
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

    private void showCreateUserDialog(){
        FragmentManager fm = getSupportFragmentManager();
        CreateUserDialogFragment createUserDialogFragment = CreateUserDialogFragment.newInstance();
        createUserDialogFragment.show(fm, "dialog");
    }

    public void createUserSuccessful(boolean successful){
        if(successful){
            currentUser = mAuth.getCurrentUser();
            updateViews();
            openMainActivity();
        } else {
            Snackbar.make(mView, R.string.create_user_failed, Snackbar.LENGTH_LONG).show();
        }
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

}
