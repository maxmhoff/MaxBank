package com.example.maxbank;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.maxbank.fragments.AccountBalanceFragment;
import com.example.maxbank.fragments.AccountFragment;
import com.example.maxbank.fragments.PaymentFragment;
import com.example.maxbank.fragments.TransactionFragment;
import com.example.maxbank.objects.User;
import com.example.maxbank.repositories.FireStoreRepo;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity implements
        AccountBalanceFragment.OnAccountBalanceInteractionListener,
        PaymentFragment.OnPaymentInteractionListener, TransactionFragment.OnTransactionInteractionListener,
        AccountFragment.OnAccountInteractionListener {

    private static final String TAG = "MainActivity";
    private static final String FRAGMENT_STATE = "FRAGMENT_STATE";

    public int fragmentId;

    private FirebaseAuth mAuth;
    private User user;
    private FirebaseUser currentUser;
    private FireStoreRepo fireStoreRepo;

    private FragmentManager fm = getSupportFragmentManager();
    private AccountBalanceFragment accountBalanceFragment = new AccountBalanceFragment();
    private PaymentFragment paymentFragment = new PaymentFragment();


    private TextView title;
    private ImageView iconSettings;
    private BottomNavigationView bottomNavbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fragmentId = R.id.account_balance;
        try {
            fragmentId = savedInstanceState.getInt(FRAGMENT_STATE);
        } catch (NullPointerException nPEX){
            Log.d(TAG, " could not retrieve fragmentId from savedInstanceState.");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fireStoreRepo = new FireStoreRepo(currentUser.getUid(), this);
        fireStoreRepo.getUser();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(FRAGMENT_STATE, fragmentId);
    }

    public void initViews(){
        title = findViewById(R.id.title);
        iconSettings = findViewById(R.id.icon_settings);
        iconSettings.setOnClickListener(onClickListener());
        bottomNavbar = findViewById(R.id.bottom_navigation);
        bottomNavbar.setOnNavigationItemSelectedListener(navbarListener());
        openFragment(fragmentId);
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

    private View.OnClickListener onClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.icon_settings){
                    showPopup(v);
                }
            }
        };
    }
    private void showPopup(View v){
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupCustom);
        PopupMenu popup = new PopupMenu(wrapper, v);
        popup.setOnMenuItemClickListener(onMenuItemClickListener());
        popup.inflate(R.menu.settings);
        popup.show();
    }


    private PopupMenu.OnMenuItemClickListener onMenuItemClickListener(){
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit_user:
                        openEditUserActivity();
                        // animation could be implemented in a more clean way
                        overridePendingTransition(R.anim.slide_in_right, R.anim.stay_put);
                        return true;
                    case R.id.sign_out:
                        AuthUI.getInstance()
                                .signOut(MainActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                        finish();
                                    }
                                });
                        return true;
                    default:
                        return false;
                }
            }
        };
    }

    private void openFragment(int id){
        clearBackStack();
        fragmentId = id;
        FragmentTransaction transaction = fm.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.USER_KEY), user);
        switch (id){
            case R.id.account_balance:
                accountBalanceFragment.setArguments(bundle);
                transaction.replace(R.id.fragment_container, accountBalanceFragment);
                transaction.commit();
                title.setText(getString(R.string.overview_title));
                break;
            case R.id.pay:
                paymentFragment.setArguments(bundle);
                transaction.replace(R.id.fragment_container, paymentFragment);
                transaction.commit();
                title.setText(getString(R.string.pay_title));
                break;
            case R.id.transaction:
                transaction.replace(R.id.fragment_container, TransactionFragment.newInstance(user));
                transaction.commit();
                title.setText(getString(R.string.transaction_title));
                break;
        }
    }

    private void openEditUserActivity(){
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra(getString(R.string.USER_KEY), user);
        startActivity(intent);
    }

    private void clearBackStack(){
        if(fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStackImmediate(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    // I am going to use this once I start setting up my fragments correctly.
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
