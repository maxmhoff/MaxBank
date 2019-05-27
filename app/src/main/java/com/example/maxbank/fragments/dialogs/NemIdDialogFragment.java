package com.example.maxbank.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.maxbank.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class NemIdDialogFragment extends DialogFragment {

    private static final String TITLE_KEY = "TITLE_KEY";
    private String title;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public NemIdDialogFragment() {
        // Required empty public constructor
    }

    public static NemIdDialogFragment newInstance(String title){
        NemIdDialogFragment fragment = new NemIdDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(TITLE_KEY);
        }
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.form_nem_id, (ViewGroup) getView(), false);
        final TextInputEditText inputNemID = viewInflated.findViewById(R.id.input_nemid);
        // generate random code for the heck of it.
        Chip nemidCode = viewInflated.findViewById(R.id.nemid_code);
        Random r = new Random();
        String code = "#" + r.nextInt(10) + r.nextInt(10) + r.nextInt(10) + r.nextInt(10);
        nemidCode.setText(code);

        final AlertDialog builder = new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogCustom)
                .setTitle(title)
                .setMessage(R.string.nem_id_description)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(false);
                    }
                })
                .setPositiveButton(R.string.verify, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(true);
                    }
                })
                .setView(viewInflated)
                .create();

        builder.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                builder.getWindow().getAttributes().height);

        // Creating a textwatcher to ensure the input is 6 characters long.


        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button btnPositive = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setEnabled(false);
                inputNemID.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(inputNemID.getText().length() == 6){
                            btnPositive.setEnabled(true);
                        } else {
                            btnPositive.setEnabled(false);
                        }
                    }
                });
            }
        });
        return builder;
    }

    private void sendResult(boolean successful){
        if(successful){
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
        } else {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
        }
    }
}
