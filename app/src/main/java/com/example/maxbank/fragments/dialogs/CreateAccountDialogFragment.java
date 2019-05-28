package com.example.maxbank.fragments.dialogs;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.example.maxbank.MainActivity;
import com.example.maxbank.R;
import com.example.maxbank.objects.Account;
import com.example.maxbank.repositories.FireStoreRepo;
import com.example.maxbank.viewmodels.UserViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CreateAccountDialogFragment extends DialogFragment {

    public static CreateAccountDialogFragment newInstance(){
        return new CreateAccountDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final UserViewModel userViewModel = ((MainActivity) getActivity()).getUserViewModel();
        final View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.form_create_account, (ViewGroup) getView(), false);

        // populating the spinner

        final AutoCompleteTextView textViewAccountType = viewInflated.findViewById(R.id.create_account_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.dropdown_menu_popup_item, getAccountTypes(userViewModel));
        if(textViewAccountType != null){
            textViewAccountType.setAdapter(adapter);
        }

        final TextInputEditText inputAccountName =  viewInflated.findViewById(R.id.create_account_name);

        // to dismiss the keyboard, when we click somewhere else.
        inputAccountName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputAccountName.getWindowToken(), 0);
            }
        });


        final AlertDialog builder = new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogCustom)
                .setTitle(R.string.create_account_title)
                .setMessage(R.string.create_account_description)
                .setView(viewInflated)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.create, null)
                .create();

        builder.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                builder.getWindow().getAttributes().height);

        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setEnabled(false);
                textViewAccountType.addTextChangedListener(textWatcher(btnPositive, textViewAccountType, inputAccountName));
                inputAccountName.addTextChangedListener(textWatcher(btnPositive, textViewAccountType, inputAccountName));
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textViewAccountType.setError(null);
                        inputAccountName.setError(null);


                        String type = typeConversion(textViewAccountType.getText().toString());

                        if (validateInput(type, userViewModel, textViewAccountType, inputAccountName)) {
                            FireStoreRepo fireStoreRepo = new FireStoreRepo();
                            fireStoreRepo.saveAccount(
                                    userViewModel.getUser().getValue().getId(),
                                    inputAccountName.getText().toString(),
                                    type,
                                    BigDecimal.valueOf(0));
                            dismiss();
                        }
                    }
                });
            }
        });
        return builder;
    }

    private boolean validateInput(String type, UserViewModel userViewModel, AutoCompleteTextView textViewAccountType, TextInputEditText inputAccountName){
        boolean userInputAcceptable = true;

        // making sure the account name isn't already in use.
        for (Account account: userViewModel.getUser().getValue().getAccounts()) {
            if(inputAccountName.getText().toString().toLowerCase().equals(account.getName().toLowerCase())){
                inputAccountName.setError(getString(R.string.error_account_name_in_use));
                userInputAcceptable = false;
            }
        }

        return userInputAcceptable;
    }
    private String[] getAccountTypes(UserViewModel userViewModel){
        // removing business type, if user has no branch assigned.
        String[] accountTypes = getResources().getStringArray(R.array.account_types);

        if(userViewModel.getUser().getValue().getBranch().equals("Ingen")){
            String[] fewerAccountTypes = new String[accountTypes.length-1];
            int j = 0;
            for (int i = 0; i < accountTypes.length-1; i++) {
                if(!accountTypes[i].equals("Ingen")){
                    fewerAccountTypes[j] = accountTypes[i];
                    j++;
                }
            }
            return fewerAccountTypes;
        }
        return accountTypes;
    }

    private String typeConversion(String type){
        switch (type.toLowerCase()){
            case "privat":
                return "default";
            case "budget":
                return "budget";
            case "pension":
                return "pension";
            case "opsparing":
                return "savings";
            case "erhverv":
                return "business";
        }
        return null;
    }

    private TextWatcher textWatcher(final Button btnPositive, final AutoCompleteTextView textViewAccountType, final TextInputEditText inputAccountName) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkIfAnyFieldIsEmpty(btnPositive, textViewAccountType, inputAccountName);
            }
        };
    }

    private void checkIfAnyFieldIsEmpty(Button btnPositive, AutoCompleteTextView textViewAccountType, TextInputEditText inputAccountName){
        boolean noFieldsAreEmpty = true;
        if(textViewAccountType.getText().length() == 0) {
            noFieldsAreEmpty = false;
        } else if(inputAccountName.getText().length() == 0) {
            noFieldsAreEmpty = false;
        }

        btnPositive.setEnabled(noFieldsAreEmpty);
    }
}
