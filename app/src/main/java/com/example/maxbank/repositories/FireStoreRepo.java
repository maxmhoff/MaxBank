package com.example.maxbank.repositories;

import android.util.Log;

import com.example.maxbank.MainActivity;
import com.example.maxbank.fragments.AccountBalanceFragment;
import com.example.maxbank.interfaces.GetAccountListener;
import com.example.maxbank.objects.Account;
import com.example.maxbank.objects.Transaction;
import com.example.maxbank.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;

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
    }

    public FireStoreRepo(){

    }

    public void getUser(){
        final DocumentReference docRef = db.document("users/" + userId);
        docRef.addSnapshotListener(mainActivity, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot docSnap, @Nullable FirebaseFirestoreException e) {
                if(docSnap.exists()) {
                    String name = docSnap.getString("name");
                    Date dayOfBirth = docSnap.getTimestamp("day_of_birth").toDate();
                    String branch = docSnap.getString("branch");
                    mainActivity.setUser(new User(userId, name, dayOfBirth, branch));
                    fetchAccounts();
                } else if (e != null){
                    Log.w(TAG, "Got an exception while trying to retrieve user data: \n" + docSnap);
                }
            }
        });
    }

    private void fetchAccounts(){
        db.collection("accounts")
                .whereEqualTo("user_id", userId)
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
                                    String type = doc.getString("type");
                                    BigDecimal balance = BigDecimal.valueOf(doc.getDouble("balance"));
                                    Account account = new Account(id, name, type, balance);
                                    accounts.add(account);
                                    fetchTransactions(account);
                                } catch (NullPointerException nPEX){
                                    Log.w(TAG, "Got an exception while trying to retrieve account data: \n" + doc + "\n" + nPEX.toString());
                                    return;
                                }
                            }
                        }
                        mainActivity.getUser().setAccounts(accounts);
                        if(initialLoad){
                            initialLoad = false;
                            mainActivity.openFragment(mainActivity.fragmentId);
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
                            Log.w(TAG, "Got a FirebaseFireStoreException while trying to retrieve transaction data");
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
                                    Log.e(TAG, "Got a NullPointerException while trying to retrieve transaction data: \n" + doc + "\n" + nPEX.toString());
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
        try{
            accountBalanceFragment.updateViews();
        } catch (NullPointerException nPEX){
            Log.w(TAG, "Got a NullPointerException while trying to use accountBalanceFragment.updateViews(): \n" + nPEX.toString());
        }

    }

    // level of abstractions is decent on every function below this line:
    public void getAccount(final GetAccountListener accountListener, String id){
        final String accountId = id;
        db.collection("accounts").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    try{
                        String name = doc.getString("name");
                        String type = doc.getString("type");
                        BigDecimal balance = BigDecimal.valueOf(doc.getDouble("balance"));
                        accountListener.onSuccess(new Account(accountId, name, type, balance));
                    } catch (NullPointerException nPEX){
                        accountListener.onError();
                    }
                } else {
                    accountListener.onError();
                }
            }
        });
    }

    // Could probably fetch the userId straight from firebase auth within this class.
    public void saveUser(String userId, String name, Date dayOfBirth, String branch){
        Timestamp day_of_birth = new Timestamp(dayOfBirth);
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("day_of_birth", day_of_birth);
        data.put("branch", branch);
        db.collection("users").document(userId)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    public void updateUserBranch(final String userId, String branch){
        db.collection("users").document(userId).update("branch", branch)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating the branch field of user_id: " + userId, e);
                    }
                });

    }

    public void saveAccount(String userId, String name, String type, BigDecimal bigDecimalBalance){
        double balance = bigDecimalBalance.doubleValue();
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("name", name);
        data.put("type", type);
        data.put("balance", balance);

        db.collection("accounts")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Account written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding account", e);
                    }
                });
    }

    public void saveTransaction(Transaction transaction){
        double amount = transaction.getAmount().doubleValue();
        Timestamp entry_date = new Timestamp(transaction.getEntryDate());
        Map<String, Object> data = new HashMap<>();
        data.put("account_id", transaction.getAccountId());
        data.put("amount", amount);
        data.put("entry_date", entry_date);
        data.put("text", transaction.getText());
        db.collection("transactions")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Transaction with ID: " + documentReference.getId() + " was successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding transaction", e);
                    }
                });
    }

    private void adjustTimeZone(){
        // Could probably find a more dynamic way to do this. Apparently my JVM & my compiler operates in different time zones.
        TimeZone timeZone;
        timeZone = TimeZone.getTimeZone("GMT+2:00");
        TimeZone.setDefault(timeZone);
    }
}
