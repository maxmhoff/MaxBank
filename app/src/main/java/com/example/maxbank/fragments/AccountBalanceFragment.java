package com.example.maxbank.fragments;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.maxbank.R;
import com.example.maxbank.adapters.AccountAdapter;
import com.example.maxbank.fragments.dialogs.CreateAccountDialogFragment;
import com.example.maxbank.objects.Account;
import com.example.maxbank.objects.User;
import com.example.maxbank.viewmodels.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AccountBalanceFragment extends Fragment implements AccountAdapter.OnItemClickListener {
    private static final String TAG = "AccountBalanceFragment";

    private UserViewModel userViewModel;

    private View mView;

    private RecyclerView accounts;
    private FloatingActionButton fab;

    private OnAccountBalanceInteractionListener mListener;

    public AccountBalanceFragment() {
        // Required empty public constructor
    }

    public static AccountBalanceFragment newInstance() {
        AccountBalanceFragment fragment = new AccountBalanceFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        final Observer<User> userObserver = new Observer<User>() {
            @Override
            public void onChanged(User user) {
                updateViews();
            }
        };
        userViewModel.getUser().observe(this, userObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_balance, container, false);
        mView = view;
        if (mListener != null) {
            mListener.onFragmentInteraction(getResources().getString(R.string.overview_title));
        }
        initViews();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAccountBalanceInteractionListener) {
            mListener = (OnAccountBalanceInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAccountBalanceInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mView = null;
    }

    public interface OnAccountBalanceInteractionListener {
        void onFragmentInteraction(String title);
    }

    private void initViews(){
        accounts = mView.findViewById(R.id.accounts);
        fab = mView.findViewById(R.id.fab);
        fab.hide();
        fab.setOnClickListener(onClickListener());

        updateViews();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show();
            }
        }, getResources().getInteger(R.integer.fab_show_delay));

    }

    private void updateViews(){
        // null check since updateViews() should only be used when the view is active.
        if(mView != null){
            accounts = mView.findViewById(R.id.accounts);
            try {
                if (userViewModel.getUser().getValue().getAccounts() != null) {
                    AccountAdapter accountAdapter =
                            new AccountAdapter(mView.getContext(), userViewModel.getUser().getValue().getAccounts());
                    accounts.setAdapter(accountAdapter);
                    accountAdapter.setOnItemClickListener(AccountBalanceFragment.this);
                    accounts.setLayoutManager(new LinearLayoutManager(getContext()));

                    int resId = R.anim.layout_animation_rise_up;
                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
                    accounts.setLayoutAnimation(animation);

                } else {
                    Log.d(TAG, "Failed to init the accountAdapter since \"userViewModel.getAccounts\" was null.");
                }
            } catch (NullPointerException nPEX){
                Log.d(TAG, "Encountered a NullPointerException when trying to retrieve account");
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        fab.hide();
        Account account = userViewModel.getUser().getValue().getAccounts().get(position);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.stay_put, R.anim.fade_in, R.anim.slide_out_bottom);
        ft.replace(R.id.fragment_container , AccountFragment.newInstance(account));
        ft.addToBackStack(null);
        ft.commit();
    }

    private View.OnClickListener onClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                switch (v.getId()){
                    case R.id.fab:
                        showCreateAccountDialog();
                        break;
                }
            }
        };
    }

    private void showCreateAccountDialog(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment dialogFrag = CreateAccountDialogFragment.newInstance();
        dialogFrag.show(getFragmentManager().beginTransaction(), "dialog");
    }
}
