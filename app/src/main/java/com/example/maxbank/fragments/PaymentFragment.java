package com.example.maxbank.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.maxbank.R;
import com.example.maxbank.fragments.dialogs.NemIdDialogFragment;
import com.example.maxbank.objects.Account;
import com.example.maxbank.objects.User;
import com.example.maxbank.utilities.TransactionHelper;
import com.example.maxbank.viewmodels.UserViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PaymentFragment extends Fragment {
    private static final String TAG = "PaymentFragment";
    private static final int NEM_ID_VERIFICATION = 1;

    private View mView;

    private UserViewModel userViewModel;

    private TextInputEditText inputAmount;
    private CheckBox toggleFixedTransfer;
    private AutoCompleteTextView inputFromAccount;
    private TextInputEditText inputToAccount;
    private TextInputLayout layoutFixedTransfer;
    private AutoCompleteTextView inputFixedTransfer;
    private MaterialButton btnConfirm;

    private TransactionHelper th;

    private OnPaymentInteractionListener mListener;

    public PaymentFragment() {
        // Required empty public constructor
    }

    public static PaymentFragment newInstance() {
        PaymentFragment fragment = new PaymentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        final Observer<User> userObserver = new Observer<User>() {
            @Override
            public void onChanged(User user) {

            }
        };
        userViewModel.getUser().observe(this, userObserver);
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mListener != null) {
            mListener.onFragmentInteraction(getResources().getString(R.string.pay_title));
        }
        mView = inflater.inflate(R.layout.fragment_payment, container, false);
        if (mListener != null) {
            mListener.onFragmentInteraction(getResources().getString(R.string.transaction_title));
        }
        initViews();
        // Inflate the layout for this fragment
        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPaymentInteractionListener) {
            mListener = (OnPaymentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAccountBalanceInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case NEM_ID_VERIFICATION:

                if (resultCode == Activity.RESULT_OK) {
                    th.submit();
                    onSubmitAnimation();

                } else if (resultCode == Activity.RESULT_CANCELED){
                    Snackbar.make(getView(), R.string.snackbar_nem_id_error, Snackbar.LENGTH_SHORT).show();
                    toggleViewsEnabled(true);
                }

                break;
        }
    }

    public interface OnPaymentInteractionListener {
        void onFragmentInteraction(String title);
    }

    private void initViews(){
        toggleFixedTransfer = mView.findViewById(R.id.toggle_fixed_transfer);

        inputAmount = mView.findViewById(R.id.input_amount);
        inputAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputAmount.getWindowToken(), 0);
            }
        });

        inputFromAccount = mView.findViewById(R.id.input_from_account);
        ArrayList<String> accounts = new ArrayList<>();
        for (Account account : userViewModel.getUser().getValue().getAccounts()) {
            accounts.add(account.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_popup_item, accounts);
        inputFromAccount.setAdapter(adapter);

        inputToAccount = mView.findViewById(R.id.input_to_account);
        layoutFixedTransfer = mView.findViewById(R.id.layout_fixed_transfer);
        inputFixedTransfer = mView.findViewById(R.id.input_fixed_transfer_options);
        ArrayAdapter<String> fixedTransferAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_popup_item,
                getResources().getStringArray(R.array.fixed_transfer_options));
        inputFixedTransfer.setAdapter(fixedTransferAdapter);

        btnConfirm = mView.findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(onClickListener());

        toggleFixedTransfer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    layoutFixedTransfer.setVisibility(View.VISIBLE);
                    toggleFixedTransfer.setTextColor(getContext().getColor(R.color.colorTextLight));
                } else {
                    layoutFixedTransfer.setVisibility(View.GONE);
                    toggleFixedTransfer.setTextColor(getContext().getColor(R.color.colorTextDark));
                }
            }
        });
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

        BigDecimal amount = new BigDecimal(inputAmount.getText().toString());

        if(fromAccount != null && inputToAccount != null){
            th = new TransactionHelper(getContext(), getView(), userViewModel.getUser().getValue(), fromAccount, amount, inputToAccount.getText().toString());
            if(th.validate()){
                if(th.checkIfNemIdIsNeeded()){
                    showNemIDDialog();
                } else {
                    // this serves as a placeholder. Never triggered since NemID always is needed on payments currently.
                    th.submit();
                    onSubmitAnimation();
                }
            }
        }
    }

    private Account retrieveAccount(String accountName){
        for (Account account : userViewModel.getUser().getValue().getAccounts()) {
            if(account.getName().equals(accountName)){
                return account;
            }
        }
        return null;
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


    private void toggleViewsEnabled(boolean toggle){
        inputAmount.setEnabled(toggle);
        inputFromAccount.setEnabled(toggle);
        inputToAccount.setEnabled(toggle);
        btnConfirm.setEnabled(toggle);
    }

    private void onSubmitAnimation(){
        toggleViewsEnabled(false);
        resetViews();
        Snackbar.make(getView(), R.string.snackbar_payment_succeeded, Snackbar.LENGTH_LONG).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleViewsEnabled(true);

            }
        }, getResources().getInteger(R.integer.on_submit_animation));
    }

    private void resetViews(){
        toggleFixedTransfer.setChecked(false);
        inputAmount.setText(null);
        inputFromAccount.setText(null);
        inputToAccount.setText(null);
        inputFixedTransfer.setText(null);
    }
}
