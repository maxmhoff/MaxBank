package com.example.maxbank.fragments;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.maxbank.R;
import com.example.maxbank.objects.Account;
import com.example.maxbank.objects.Transaction;
import com.example.maxbank.objects.User;
import com.example.maxbank.repositories.FireStoreRepo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnTransactionInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends Fragment {
    private static final String USER_KEY = "USER_KEY";
    public static final int NEM_ID_VERIFICATION = 1;

    private User user;
    private Transaction[] storedTransactions = new Transaction[2];

    private View mView;
    private TextInputEditText inputAmount;
    private AutoCompleteTextView inputFromAccount;
    private AutoCompleteTextView inputToAccount;
    private MaterialButton btnConfirm;

    private boolean nemIDVerificationNeeded;

    private OnTransactionInteractionListener mListener;

    public TransactionFragment() {
        // Required empty public constructor
    }

    public static TransactionFragment newInstance(User user) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        args.putParcelable(USER_KEY, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(USER_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_transaction, container, false);
        initViews();
        // Inflate the layout for this fragment
        return mView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTransactionInteractionListener) {
            mListener = (OnTransactionInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTransactionInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTransactionInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void initViews(){
        if(mView != null){
            inputAmount = mView.findViewById(R.id.input_amount);

            inputFromAccount = mView.findViewById(R.id.input_from_account);
            inputToAccount = mView.findViewById(R.id.input_to_account);
            ArrayList<String> accounts = new ArrayList<>();
            for (Account account : user.getAccounts()) {
                accounts.add(account.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_popup_item, accounts);
            inputFromAccount.setAdapter(adapter);
            inputToAccount.setAdapter(adapter);

            btnConfirm = mView.findViewById(R.id.button_confirm);
            btnConfirm.setOnClickListener(onClickListener());

        }
    }

    private View.OnClickListener onClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.button_confirm){
                    createTransactions();
                }
            }
        };
    }

    private void createTransactions(){

        Account fromAccount = retrieveAccount(inputFromAccount.getText().toString());
        Account toAccount = retrieveAccount(inputToAccount.getText().toString());

        BigDecimal amount = new BigDecimal(inputAmount.getText().toString());
        String textFromAccount = getString(R.string.input_to_account) + " " + toAccount.getName();
        String textToAccount = getString(R.string.input_from_account) + " " + fromAccount.getName();

        if(fromAccount != null && toAccount != null){
            if(validateTransactions(fromAccount, toAccount, amount)){
                Transaction send = new Transaction(new BigDecimal(0).subtract(amount), new Date(), textFromAccount, fromAccount.getId());
                Transaction received = new Transaction(amount, new Date(), textToAccount, toAccount.getId());

                if(nemIDVerificationNeeded == false){
                    FireStoreRepo fireStoreRepo = new FireStoreRepo();
                    submitTransactions(send, received);
                } else {
                    storedTransactions[0] = send;
                    storedTransactions[1] = received;
                }
            }
        }
    }

    private Account retrieveAccount(String accountName){
        for (Account account : user.getAccounts()) {
            if(account.getName().equals(accountName)){
                return account;
            }
        }
        return null;
    }

    private Boolean validateTransactions(Account fromAccount, Account toAccount, BigDecimal amount){
        nemIDVerificationNeeded = false;
        // sufficient funds
        if(fromAccount.getBalance().compareTo(amount) < 0){
            Snackbar.make(getView(), R.string.snackbar_insufficient_funds , Snackbar.LENGTH_LONG).show();
            return false;
        }
        // sufficient age
        if(fromAccount.getType().equals("pension")){
            if(!user.isSeniorCitizen()){
                Snackbar.make(getView(), R.string.snackbar_not_senior_citizen , Snackbar.LENGTH_LONG).show();
                return false;
            }
        }
        // nemId
        if(toAccount.getType().equals("pension")){
            nemIDVerificationNeeded = true;
            showNemIDDialog();
        }
        return true;
    }

    private void submitTransactions(Transaction send, Transaction received){
        if(!nemIDVerificationNeeded){
            FireStoreRepo fireStoreRepo = new FireStoreRepo();
            fireStoreRepo.saveTransaction(send);
            fireStoreRepo.saveTransaction(received);
        }
    }

    private void showNemIDDialog(){
        toggleViewsEnabled(false);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment dialogFrag = NemIdDialogFragment.newInstance(getString(R.string.verify_transaction_title));
        dialogFrag.setTargetFragment(this, NEM_ID_VERIFICATION);
        dialogFrag.show(getFragmentManager().beginTransaction(), "dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case NEM_ID_VERIFICATION:

                if (resultCode == Activity.RESULT_OK) {
                    nemIDVerificationNeeded = false;
                    submitTransactions(storedTransactions[0], storedTransactions[1]);

                } else if (resultCode == Activity.RESULT_CANCELED){
                    Snackbar.make(getView(), R.string.snackbar_nem_id_error, Snackbar.LENGTH_SHORT).show();
                    nemIDVerificationNeeded = false;
                    toggleViewsEnabled(true);
                }

                break;
        }
    }

    private void toggleViewsEnabled(boolean toggle){
        inputAmount.setEnabled(toggle);
        inputFromAccount.setEnabled(toggle);
        inputToAccount.setEnabled(toggle);
        btnConfirm.setEnabled(toggle);
    }
}

