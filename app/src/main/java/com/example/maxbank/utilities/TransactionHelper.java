package com.example.maxbank.utilities;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.example.maxbank.R;
import com.example.maxbank.interfaces.GetAccountListener;
import com.example.maxbank.objects.Account;
import com.example.maxbank.objects.Transaction;
import com.example.maxbank.objects.User;
import com.example.maxbank.repositories.FireStoreRepo;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigDecimal;
import java.util.Date;

public class TransactionHelper implements GetAccountListener {
    private static final String TAG = "TransactionHelper";

    private FireStoreRepo fireStoreRepo = new FireStoreRepo();

    private Context context;
    private View view;

    private User user;

    private Account sender;
    private Account receiver;
    private BigDecimal amount;

    private Transaction received;
    private Transaction send;

    // problem with this method is that in some cases users will be able to submit before server response, even though the account exists.
    private boolean receiverAccountIsIdentified = false;
    private boolean receiverUnknown;

    public TransactionHelper (Context context, View view, User user, Account sender, Account receiver, BigDecimal amount){
        Log.d(TAG, "A new TransactionHelper was instantiated.");
        this.context = context;
        this.view = view;
        this.user = user;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;

        receiverAccountIsIdentified = true;
        receiverUnknown = false;

        createTransactions();
    }

    // Overloading constructor: If the receiver account is unknown, we have to fetch it.
    public TransactionHelper(Context context, View view, User user, Account sender, BigDecimal amount, String receiverId){
        this.context = context;
        this.view = view;
        this.user = user;
        this.sender = sender;
        this.amount = amount;
        fireStoreRepo.getAccount(this, receiverId);

        receiverUnknown = true;
    }

    public Boolean validate(){
        if(sender.getId().equals(receiver.getId())){
            Snackbar.make(view, R.string.error_same_account_id, Snackbar.LENGTH_LONG ).show();
            return false;
        }

        // sufficient funds
        if(sender.getBalance().compareTo(amount) < 0){
            Snackbar.make(view, R.string.snackbar_insufficient_funds , Snackbar.LENGTH_LONG).show();
            return false;
        }
        // sufficient age
        if(sender.getType().equals("pension")){
            if(!user.isSeniorCitizen()){
                Snackbar.make(view, R.string.snackbar_not_senior_citizen , Snackbar.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    public Boolean checkIfNemIdIsNeeded(){
        // we should always request NemID if the receiver is unknown.
        return receiverUnknown || sender.getType().equals("savings") || receiver.getType().equals("pension");

    }
    private void createTransactions(){
        String textSender;
        String textReceiver;

        if(receiverUnknown){
            textSender = context.getString(R.string.input_to_account) + " " + receiver.getId();
            textReceiver = context.getString(R.string.input_from_account) + " " + user.getName();
        } else {
            textSender = context.getString(R.string.input_to_account) + " " + receiver.getName();
            textReceiver = context.getString(R.string.input_from_account) + " " + sender.getName();
        }

        send = new Transaction(new BigDecimal(0).subtract(amount), new Date(), textSender, sender.getId());
        received = new Transaction(amount, new Date(), textReceiver, receiver.getId());
    }

    public void submit(){
        {
            if(receiverAccountIsIdentified){
                fireStoreRepo.saveTransaction(send);
                fireStoreRepo.saveTransaction(received);
            } else {
                Snackbar.make(view, R.string.snackbar_could_not_identify_receiver_account , Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onGetAccountSuccess(Account account) {
        receiver = account;
        receiverAccountIsIdentified = true;
        createTransactions();
    }

    @Override
    public void onGetAccountError() {
        Log.d(TAG, "Was unable to get account.");
    }
}
