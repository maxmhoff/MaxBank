package com.example.maxbank.repositories;

import android.util.Log;

import com.example.maxbank.MainActivity;
import com.example.maxbank.R;
import com.example.maxbank.fragments.AccountBalanceFragment;
import com.example.maxbank.objects.Account;
import com.example.maxbank.objects.Transaction;
import com.example.maxbank.objects.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Nullable;

public class FireStoreRepo {

    private static final String TAG = "FireStoreRepo";

    private String userId;

    private MainActivity mainActivity;

    private AccountBalanceFragment accountBalanceFragment;

    // to ensure openFragment() only gets called on first fetch
    private Boolean initialLoad = true;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FireStoreRepo(String userId, MainActivity mainActivity){
        this.userId = userId;
        this.mainActivity = mainActivity;
        this.accountBalanceFragment = mainActivity.getAccountBalanceFragment();
        adjustTimeZone();
        fetchUser();
    }

    private void fetchUser(){
        DocumentReference docRef = db.document("users/" + userId);
        docRef.addSnapshotListener(mainActivity, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    Date dayOfBirth = documentSnapshot.getTimestamp("day_of_birth").toDate();
                    mainActivity.setUser(new User(userId, name, dayOfBirth));
                    fetchAccounts();
                } else if (e != null){
                    Log.w(TAG, "Got an exception while trying to retrieve user data.");
                }
            }
        });
    }

    private void fetchAccounts(){
        db.collection("accounts")
                .whereEqualTo("user_id", mainActivity.getUser().getId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if(e != null){
                            Log.w(TAG, "Got an exception while trying to retrieve account data.");
                            return;
                        }
                        List<Account> accounts = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value){
                            if(doc.exists()){
                                try {
                                    String id = doc.getId();
                                    String name = doc.getString("name");
                                    BigDecimal balance = BigDecimal.valueOf(doc.getDouble("balance"));
                                    Account account = new Account(id, name, balance);
                                    accounts.add(account);
                                    fetchTransactions(account);
                                } catch (NullPointerException nPEX){
                                    Log.w(TAG, "Got an exception while trying to retrieve account data.");
                                    return;
                                }
                            }
                        }
                        mainActivity.getUser().setAccounts(accounts);
                        if(initialLoad){
                            initialLoad = false;
                            mainActivity.openFragment(R.id.account_balance);
                        }
                    }
                });
    }

    private void fetchTransactions(Account account){
        db.collection("transactions")
                .whereEqualTo("account_id", account.getId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if(e != null){
                            Log.w(TAG, "Got a FirebasFireStoreException while trying to retrieve transaction data");
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value){
                            if(doc.exists()){
                                try {
                                    String id = doc.getId();
                                    BigDecimal amount = BigDecimal.valueOf(doc.getDouble("amount"));
                                    Date dayOfBirth = doc.getTimestamp("entry_date").toDate();
                                    String text = doc.getString("text");
                                    String accountId = doc.getString("account_id");
                                    Transaction transaction = new Transaction(id, amount, dayOfBirth, text, accountId);
                                    assignTransaction(transaction);
                                } catch (NullPointerException nPEX){
                                    Log.e(TAG, "Got an exception while trying to retrieve transaction data.");
                                    return;
                                }
                            }
                        }
                    }
                });
    }

    private void assignTransaction(Transaction transaction){
        if(transaction.getAccountId() != null){
            for (Account account : mainActivity.getUser().getAccounts()) {
                if(transaction.getAccountId().equals(account.getId())){
                    account.addOrUpdateTransaction(transaction);
                }
            }
        }
        accountBalanceFragment.updateView();
    }

    private void adjustTimeZone(){
        // Could probably find a more dynamic way to do this. Apparently my JVM & my compiler operates in different time zones
        TimeZone timeZone;
        timeZone = TimeZone.getTimeZone("GMT+2:00");
        TimeZone.setDefault(timeZone);
    }
}
