package com.example.maxbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.example.maxbank.objects.User;
import com.example.maxbank.repositories.FireStoreRepo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditUserActivity extends AppCompatActivity {
    private final String TAG = "EditUserActivity";
    private final String USER_KEY = "USER_KEY";

    private FireStoreRepo fireStoreRepo;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private User user;

    private Boolean passwordHasBeenReset = false;

    private TextView title;
    private SwitchMaterial toggleEdits;
    private TextInputEditText email;
    private AutoCompleteTextView branch;
    private MaterialButton btnResetPassword;
    private MaterialButton btnSaveChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        Intent intent = getIntent();
        user = intent.getExtras().getParcelable(USER_KEY);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fireStoreRepo = new FireStoreRepo();
        initViews();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        toggleEdits.setChecked(false);

    }

    private void initViews() {
        title = findViewById(R.id.title);
        title.setText(user.getName());

        email = findViewById(R.id.user_email);
        email.setText(currentUser.getEmail());

        branch = findViewById(R.id.user_branch);
        branch.setText(user.getBranch());

        btnResetPassword = findViewById(R.id.button_reset_password);
        btnResetPassword.setOnClickListener(onClickListener());

        btnSaveChanges = findViewById(R.id.button_save_changes);
        btnSaveChanges.setOnClickListener(onClickListener());

        toggleEdits = findViewById(R.id.toggle_edits);
        toggleEdits.setOnClickListener(onClickListener());
    }

    private void toggleEditing(Boolean toggle){
        email.setEnabled(toggle);
        branch.setEnabled(toggle);
        if(toggle){
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.branches));
            branch.setAdapter(adapter);
        } else {
            branch.setAdapter(null);
        }


        // To make sure users can't hit the reset button multiple times.
        if(!passwordHasBeenReset){
            btnResetPassword.setEnabled(toggle);
        }
        btnSaveChanges.setEnabled(toggle);
    }

    private View.OnClickListener onClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.toggle_edits){
                    toggleEditing(toggleEdits.isChecked());
                }
                if(v.getId() == R.id.button_reset_password){
                    mAuth.sendPasswordResetEmail(currentUser.getEmail());
                    btnResetPassword.setEnabled(false);
                    passwordHasBeenReset = true;
                    Snackbar.make(v, R.string.snackbar_password_reset_email, Snackbar.LENGTH_LONG).show();
                }
                if(v.getId() == R.id.button_save_changes){
                    handleChanges(v);
                }
            }
        };
    }

    private void handleChanges(View v){
        boolean changeOccurred = false;
        if(!email.getText().toString().equals(currentUser.getEmail())){
            changeEmailForm(v);
            currentUser.updateEmail(email.getText().toString());
            changeOccurred = true;
        }
        if(!branch.getText().toString().equals(user.getBranch())){
            fireStoreRepo.updateUserBranch(currentUser.getUid(), branch.getText().toString());
            changeOccurred = true;
        }
        toggleEditing(false);
        toggleEdits.setChecked(false);

        if(!changeOccurred){
            Snackbar.make(v, R.string.snackbar_no_changes, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void changeEmailForm(View v){
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.form_change_email, (ViewGroup) v.getRootView(), false);
        final TextInputEditText inputPassword = viewInflated.findViewById(R.id.input_password);
        new MaterialAlertDialogBuilder(this, R.style.AlertDialogCustom)
                .setTitle(R.string.change_email_title)
                .setMessage(R.string.change_email_description)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.change_email_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateEmail(inputPassword.getText().toString());
                    }
                })
                .setView(viewInflated)
                .show();
    }

    private void updateEmail(String password){
        AuthCredential credential = EmailAuthProvider
                .getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "User re-authenticated.");

                currentUser.updateEmail(email.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User email address updated.");
                                } else {
                                    Log.w(TAG, "Unable to update the user's email address");
                                }
                            }
                        });
            }
        });
    }

}
