package com.example.maxbank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.maxbank.fragments.AccountBalanceFragment;
import com.example.maxbank.fragments.AccountFragment;
import com.example.maxbank.fragments.PaymentFragment;
import com.example.maxbank.fragments.TransactionFragment;

import com.example.maxbank.objects.User;
import com.example.maxbank.repositories.FireStoreRepo;
import com.example.maxbank.viewmodels.UserViewModel;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


public class MainActivity extends AppCompatActivity implements
        AccountBalanceFragment.OnAccountBalanceInteractionListener,
        PaymentFragment.OnPaymentInteractionListener, TransactionFragment.OnTransactionInteractionListener,
        AccountFragment.OnAccountInteractionListener {

    private static final String TAG = "MainActivity";
    private static final String FRAGMENT_STATE = "FRAGMENT_STATE";
    private static final String USER_KEY = "USER_KEY";

    private int fragmentId;

    private UserViewModel userViewModel;
    private FirebaseUser currentUser;
    private FireStoreRepo fireStoreRepo;

    private FragmentManager fm = getSupportFragmentManager();

    private TextView textViewTitle;
    private ImageView iconSettings;
    private BottomNavigationView bottomNavbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        final Observer<User> userObserver = new Observer<User>() {
            @Override
            public void onChanged(User user) {
                // Update UI here.
            }
        };
        userViewModel.getUser().observe(this, userObserver);

        fragmentId = R.id.account_balance;
        initViews();

        if(savedInstanceState != null) {
            try {
                fragmentId = savedInstanceState.getInt(FRAGMENT_STATE);


            } catch (NullPointerException nPEX) {
                Log.d(TAG, " could not retrieve fragmentId from savedInstanceState.");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fireStoreRepo = new FireStoreRepo();
        fireStoreRepo.getUser(currentUser.getUid(), userViewModel);
        openFragment(fragmentId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(FRAGMENT_STATE, fragmentId);
    }

    private void initViews(){
        textViewTitle = findViewById(R.id.title);
        iconSettings = findViewById(R.id.icon_settings);
        iconSettings.setOnClickListener(onClickListener());
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

    private View.OnClickListener onClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.icon_settings){
                    Animation rotateAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate_cog);
                    iconSettings.startAnimation(rotateAnimation);
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

    public void openFragment(int id){
        clearBackStack();
        fragmentId = id;
        FragmentTransaction transaction = fm.beginTransaction();

        switch (id){
            case R.id.account_balance:
                transaction.replace(R.id.fragment_container, AccountBalanceFragment.newInstance());
                transaction.commit();
                break;
            case R.id.pay:
                transaction.replace(R.id.fragment_container, PaymentFragment.newInstance());
                transaction.commit();
                break;
            case R.id.transaction:
                transaction.replace(R.id.fragment_container, TransactionFragment.newInstance());
                transaction.commit();
                break;
        }
    }

    private void openEditUserActivity(){
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra(USER_KEY, userViewModel.getUser().getValue());
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
    public void onFragmentInteraction(String title) {
        textViewTitle.setText(title);
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }
}
