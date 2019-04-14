package com.example.maxbank;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.maxbank.fragments.AccountBalanceFragment;
import com.example.maxbank.fragments.AccountFragment;
import com.example.maxbank.fragments.PaymentFragment;
import com.example.maxbank.fragments.TransactionFragment;
import com.example.maxbank.objects.Account;
import com.example.maxbank.objects.Transaction;
import com.example.maxbank.objects.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity implements
        AccountBalanceFragment.OnAccountBalanceInteractionListener,
        PaymentFragment.OnPaymentInteractionListener, TransactionFragment.OnTransactionInteractionListener,
        AccountFragment.OnAccountInteractionListener {

    private String userId = "rnOMcdTSeDRiZ6JCBDxnMyeuRkI2";
    private User user;

    private Boolean initialLoad = true;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FragmentManager fm = getSupportFragmentManager();
    private AccountBalanceFragment accountBalanceFragment = new AccountBalanceFragment();
    private PaymentFragment paymentFragment = new PaymentFragment();
    private TransactionFragment transactionFragment = new TransactionFragment();

    private static String TAG = "MainActivity";

    private TextView title;
    private BottomNavigationView bottomNavbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adjustTimeZone();
        fetchUser();
    }

    private void fetchUser(){
        DocumentReference docRef = db.document("users/" + userId);
        docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    Date dayOfBirth = documentSnapshot.getTimestamp("day_of_birth").toDate();
                    user = new User(userId, name, dayOfBirth);
                    fetchAccounts();
                } else if (e != null){
                    Log.w(TAG, "Got an exception while trying to retrieve user data.");
                }
            }
        });
    }

    private void fetchAccounts(){
        db.collection("accounts")
                .whereEqualTo("user_id", user.getId())
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
                user.setAccounts(accounts);
                if(initialLoad){
                    initialLoad = false;
                    openFragment(R.id.account_balance);
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
            for (Account account : user.getAccounts()) {
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

    private void initViews(){
        title = findViewById(R.id.title);
        bottomNavbar = findViewById(R.id.bottom_navigation);
        bottomNavbar.setOnNavigationItemSelectedListener(navbarListener());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navbarListener(){
        return new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                openFragment(menuItem.getItemId());
                return true;
            }
        };
    }

    public void openFragment(int id){
        clearBackStack();
        FragmentTransaction transaction = fm.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.USER_KEY), user);
        switch (id){
            case R.id.account_balance:

                accountBalanceFragment.setArguments(bundle);
                transaction.replace(R.id.fragment_container, accountBalanceFragment);
                transaction.commit();
                title.setText(getString(R.string.overviewTitle));
                break;
            case R.id.pay:
                paymentFragment.setArguments(bundle);
                transaction.replace(R.id.fragment_container, paymentFragment);
                transaction.commit();
                title.setText(getString(R.string.payTitle));
                break;
            case R.id.transaction:
                transactionFragment.setArguments(bundle);
                transaction.replace(R.id.fragment_container, transactionFragment);
                transaction.commit();
                title.setText(getString(R.string.transactionTitle));
                break;
        }
    }

    private void clearBackStack(){
        if(fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStackImmediate(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
