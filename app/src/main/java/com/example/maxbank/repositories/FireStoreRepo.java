package com.example.maxbank.repositories;

import android.util.Log;

import com.example.maxbank.interfaces.GetAccountListener;
import com.example.maxbank.objects.Account;
import com.example.maxbank.objects.Transaction;
import com.example.maxbank.objects.User;
import com.example.maxbank.viewmodels.UserViewModel;
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

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FireStoreRepo() {
        adjustTimeZone();
    }

    public void getUser(final String userId, final UserViewModel userViewModel) {
        final DocumentReference docRef = db.document("users/" + userId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot docSnap, @Nullable FirebaseFirestoreException e) {
                if (docSnap.exists()) {
                    try {
                        String name = docSnap.getString("name");
                        Date dayOfBirth = docSnap.getTimestamp("day_of_birth").toDate();
                        String branch = docSnap.getString("branch");
                        userViewModel.getUser().setValue(new User(userId, name, dayOfBirth, branch));
                    } catch (NullPointerException nPEX) {
                        Log.d(TAG, "Unable to retrieve user: " + userId);
                    }
                } else if (e != null) {
                    Log.d(TAG, "Unable to retrieve user: " + userId);
                }
                getAccounts(userId, userViewModel);
            }
        });
    }

    private void getAccounts(final String userId, final UserViewModel userViewModel) {
        db.collection("accounts")
                .whereEqualTo("user_id", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Got an exception while trying to retrieve account data.");
                            return;
                        }
                        List<Account> accounts = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            if (doc.exists()) {
                                String id = doc.getId();
                                String name = doc.getString("name");
                                String type = doc.getString("type");
                                BigDecimal balance = BigDecimal.valueOf(doc.getDouble("balance"));
                                accounts.add(new Account(id, name, type, balance));

                            } else {
                                Log.d(TAG, "Unable to retrieve accounts for user: " + userId);
                            }
                        }
                        userViewModel.getUser().getValue().setAccounts(accounts);
                        for (Account account: accounts) {
                            getTransactions(account, userViewModel);
                        }
                    }
                });
    }

    private void getTransactions(final Account account, final UserViewModel userViewModel) {
        db.collection("transactions")
                .whereEqualTo("account_id", account.getId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Got a FirebaseFireStoreException while trying to retrieve transaction data");
                            return;
                        }
                        List<Transaction> transactions = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            if (doc.exists()) {
                                try {
                                    String id = doc.getId();
                                    BigDecimal amount = BigDecimal.valueOf(doc.getDouble("amount"));
                                    Date dayOfBirth = doc.getTimestamp("entry_date").toDate();
                                    String text = doc.getString("text");
                                    String accountId = doc.getString("account_id");
                                    transactions.add(new Transaction(id, amount, dayOfBirth, text, accountId));
                                } catch (NullPointerException nPEX) {
                                    Log.d(TAG, "Unable to retrieve transactions for acount: " + account.getId());
                                    return;
                                }
                            } else {
                                Log.d(TAG, "Unable to retrieve transactions for acount: " + account.getId());
                            }
                        }
                        account.setTransactions(transactions);
                        setUserCallback(userViewModel, account);
                    }
                });


    }

    public void getAccount(final GetAccountListener accountListener, String id) {
        final String accountId = id;
        db.collection("accounts").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    try {
                        String name = doc.getString("name");
                        String type = doc.getString("type");
                        BigDecimal balance = BigDecimal.valueOf(doc.getDouble("balance"));
                        accountListener.onGetAccountSuccess(new Account(accountId, name, type, balance));
                    } catch (NullPointerException nPEX) {
                        accountListener.onGetAccountError();
                    }
                } else {
                    accountListener.onGetAccountError();
                }
            }
        });
    }

    public void saveUser(String userId, String name, Date dayOfBirth, String branch) {
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

    public void updateUserBranch(final String userId, String branch) {
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

    public void saveAccount(String userId, String name, String type, BigDecimal bigDecimalBalance) {
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
                        Log.d(TAG, "Account with ID: " + documentReference.getId() + " was successfully created.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding account", e);
                    }
                });
    }

    public void saveTransaction(Transaction transaction) {
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
                        Log.d(TAG, "Transaction with ID: " + documentReference.getId() + " was successfully created.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding transaction", e);
                    }
                });
    }

    // Utility
    private void setUserCallback(UserViewModel userViewModel, Account account){
        User user = userViewModel.getUser().getValue();
        user.addOrUpdateAccount(account);
        userViewModel.getUser().setValue(user);
    }

    private void adjustTimeZone() {
        // Could probably find a more dynamic way to do this. Apparently my JVM & my compiler operates in different time zones.
        TimeZone timeZone;
        timeZone = TimeZone.getTimeZone("GMT+2:00");
        TimeZone.setDefault(timeZone);
    }
}
