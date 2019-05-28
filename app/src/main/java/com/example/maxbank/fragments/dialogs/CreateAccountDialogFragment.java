package com.example.maxbank.fragments.dialogs;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CreateAccountDialogFragment extends DialogFragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Activity mActivity;

    public static CreateAccountDialogFragment newInstance(){
        return new CreateAccountDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        final UserViewModel userViewModel = ((MainActivity) getActivity()).getUserViewModel();

        final View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.form_create_account, (ViewGroup) getView(), false);

        // populating the spinner
        final AutoCompleteTextView textViewAccountType = viewInflated.findViewById(R.id.create_account_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.account_types));
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
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // checking if inputs are empty before calling the repo to store the account.
                        if(!inputAccountName.getText().toString().equals("") &&
                                !textViewAccountType.getText().toString().equals("")){

                            // making sure the account name isn't already in use.
                            boolean accountNameTaken = false;
                            for (Account account: userViewModel.getUser().getValue().getAccounts()) {
                                if(inputAccountName.getText().toString().toLowerCase().equals(account.getName().toLowerCase())){
                                    accountNameTaken = true;
                                }
                            }
                            if(!accountNameTaken){
                                //making sure a userViewModel has a branch assigned if the userViewModel is trying to make a business account.
                                String type = typeConversion(textViewAccountType.getText().toString());
                                if(type.equals("business") && userViewModel.getUser().getValue().getBranch().equals("Ingen")){
                                    Snackbar.make(getView(), R.string.snackbar_business_account_without_branch, Snackbar.LENGTH_LONG).show();
                                } else {
                                    FireStoreRepo fireStoreRepo = new FireStoreRepo();
                                    fireStoreRepo.saveAccount(
                                            userViewModel.getUser().getValue().getId(),
                                            inputAccountName.getText().toString(),
                                            type,
                                            BigDecimal.valueOf(0));
                                    dismiss();
                                }
                            } else {
                                Snackbar.make(getView(), R.string.snackbar_account_name_in_use, Snackbar.LENGTH_LONG).show();
                            }

                        }
                    }
                });
            }
        });
        return builder;
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
}
