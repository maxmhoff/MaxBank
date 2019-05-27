package com.example.maxbank.fragments.dialogs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.example.maxbank.AuthActivity;
import com.example.maxbank.R;
import com.example.maxbank.repositories.FireStoreRepo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class CreateUserDialogFragment extends DialogFragment {

    private final String TAG = "CreateUserDialogFragment";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Activity mActivity;


    public CreateUserDialogFragment() {
        // Required empty public constructor
    }

    public static CreateUserDialogFragment newInstance(){
        return new CreateUserDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.form_create_user, (ViewGroup) getView(), false);

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
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final TextInputEditText inputUserEmail =  viewInflated.findViewById(R.id.create_user_email);
        final TextInputEditText inputUserPassword = viewInflated.findViewById(R.id.create_user_password);

        final AlertDialog builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom)
                .setTitle(R.string.create_user_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.create, null)
                .setView(viewInflated)
                .create();
        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                builder.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        builder.getWindow().getAttributes().height);
                Button btnPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setEnabled(false);
                ArrayList<TextInputEditText> inputs = new ArrayList<>();
                inputs.add(inputUserName);
                inputs.add(inputUserDayOfBirth);
                inputs.add(inputUserEmail);
                inputs.add(inputUserPassword);

                // to make sure button is enabled on state changes.
                checkIfAnyFieldIsEmpty(btnPositive, inputs);

                inputUserName.addTextChangedListener(textWatcher(btnPositive, inputs));
                inputUserDayOfBirth.addTextChangedListener(textWatcher(btnPositive, inputs));
                inputUserEmail.addTextChangedListener(textWatcher(btnPositive, inputs));
                inputUserPassword.addTextChangedListener(textWatcher(btnPositive, inputs));

                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        String name = inputUserName.getText().toString();
                        String dayOfBirth = inputUserDayOfBirth.getText().toString();
                        String email = inputUserEmail.getText().toString();
                        String password = inputUserPassword.getText().toString();
                        // checking if inputs are empty before calling the repo to store the account
                        if(verifyUserInputs(inputUserName, inputUserDayOfBirth, inputUserEmail, inputUserPassword)){
                            createUser(name, dayOfBirth, email, password);
                            dialog.dismiss();

                        }
                    }
                });
            }
        });




        return builder;
    }

    private void updateInputDayOfBirth(TextInputEditText v, Calendar myCalendar) {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        v.setText(sdf.format(myCalendar.getTime()));
    }

    private boolean verifyUserInputs(TextInputEditText inputUserName, TextInputEditText inputDayOfBirth, TextInputEditText inputEmail, TextInputEditText inputPassword){
        boolean userInputAcceptable = true;

        // name
        String expression = "^[a-zA-ZæÆøØåÅ\\s]+";

        if(!inputUserName.getText().toString().matches(expression)){
            inputUserName.setError(getString(R.string.error_name_character));
            userInputAcceptable = false;
        }
        if(!inputUserName.getText().toString().contains(" ")) {
            inputUserName.setError(getString(R.string.error_full_name_missing));
            userInputAcceptable = false;
        }

        // day of birth - I'm using LocalDate(), since it's a lot less clumsy than Date()
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dayOfBirth = LocalDate.parse(inputDayOfBirth.getText().toString(), formatter);
        Period period = Period.between(dayOfBirth, LocalDate.now());
        if(period.getYears() < 16){
            inputDayOfBirth.setError(getString(R.string.error_too_young));
            userInputAcceptable = false;
        }
        if(period.getYears() > 150){
            inputDayOfBirth.setError(getString(R.string.error_too_old));
            userInputAcceptable = false;
        }

        // email
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(inputEmail.getText().toString());
        if(!m.matches()){
            inputEmail.setError(getString(R.string.error_invalid_email_address));
            userInputAcceptable = false;
        }

        // password
        if(inputPassword.getText().toString().length() < 6){
            inputPassword.setError(getString(R.string.error_password_too_short));
            userInputAcceptable = false;
        }

        return userInputAcceptable;
    }

    private TextWatcher textWatcher(final Button btnPositive, final ArrayList<TextInputEditText> inputs){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkIfAnyFieldIsEmpty(btnPositive, inputs);
            }
        };
    }

    private void createUser(final String name, String dayOfBirth,String email, String password){
        try {
            final FireStoreRepo fireStoreRepo = new FireStoreRepo();
            final Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dayOfBirth);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
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
                                sendResult(true);

                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                sendResult(false);
                            }
                        }
                    });
        } catch (ParseException e) {
            Log.w(TAG, "Could not parse " + dayOfBirth + " to Date.");
        }

    }

    private void checkIfAnyFieldIsEmpty(Button btnPositive, ArrayList<TextInputEditText> inputs){
        boolean noFieldsAreEmpty = true;
        for (TextInputEditText tIET: inputs) {
            if(tIET.getText().toString().length()==0){
                noFieldsAreEmpty = false;
            }
        }
        btnPositive.setEnabled(noFieldsAreEmpty);
    }

    // I should implement a listener interface if I want to reuse this form, but as of now this is sufficient
    private void sendResult(boolean successful){
        ((AuthActivity) mActivity).createUserSuccessful(successful);
    }
}
