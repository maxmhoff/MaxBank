package com.example.maxbank;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.maxbank.fragments.AccountBalanceFragment;
import com.example.maxbank.fragments.AccountFragment;
import com.example.maxbank.fragments.PaymentFragment;
import com.example.maxbank.fragments.TransactionFragment;
import com.example.maxbank.objects.User;
import com.example.maxbank.repositories.FireStoreRepo;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

    private FragmentManager fm = getSupportFragmentManager();
    private AccountBalanceFragment accountBalanceFragment = new AccountBalanceFragment();
    private PaymentFragment paymentFragment = new PaymentFragment();
    private TransactionFragment transactionFragment = new TransactionFragment();

    private static final String TAG = "MainActivity";

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
        new FireStoreRepo(userId, this);
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AccountBalanceFragment getAccountBalanceFragment() {
        return accountBalanceFragment;
    }
}
