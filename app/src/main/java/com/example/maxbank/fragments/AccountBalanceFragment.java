package com.example.maxbank.fragments;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.maxbank.MainActivity;
import com.example.maxbank.R;
import com.example.maxbank.adapters.AccountAdapter;
import com.example.maxbank.objects.Account;
import com.example.maxbank.objects.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAccountBalanceInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountBalanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class AccountBalanceFragment extends Fragment implements AccountAdapter.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private User user;

    private static final String TAG = "AccountBalanceFragment";

    private View mView;

    private RecyclerView accounts;
    private FloatingActionButton fab;

    private AccountFragment accountFragment = new AccountFragment();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnAccountBalanceInteractionListener mListener;

    public AccountBalanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountBalanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountBalanceFragment newInstance(String param1, String param2) {
        AccountBalanceFragment fragment = new AccountBalanceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(getString(R.string.USER_KEY));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_balance, container, false);
        mView = view;
        initViews();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnAccountBalanceInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void initViews(){
        accounts = mView.findViewById(R.id.accounts);
        updateView();
        fab = mView.findViewById(R.id.fab);
        fab.hide();
        fab.setOnClickListener(onClickListener());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show();
            }
        }, getResources().getInteger(R.integer.fab_show_delay));

    }

    public void updateView(){
        // null check since updateView() should only be used when the view is active.
        if(mView != null){
            accounts = mView.findViewById(R.id.accounts);
            if(user.getAccounts() != null){
                AccountAdapter accountAdapter = new AccountAdapter(mView.getContext(), user.getAccounts());
                accounts.setAdapter(accountAdapter);
                accountAdapter.setOnItemClickListener(AccountBalanceFragment.this);
                accounts.setLayoutManager(new LinearLayoutManager(getContext()));

                int resId = R.anim.layout_animation_rise_up;
                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
                accounts.setLayoutAnimation(animation);

            } else {
                Log.w(TAG, "Failed to init the accountAdapter since \"user.getAccounts\" was null.");
            }
        }

        accountFragment.updateView();

    }

    @Override
    public void onItemClick(int position) {
        fab.hide();
        Account account = user.getAccounts().get(position);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.stay_put, R.anim.fade_in, R.anim.slide_out_bottom);
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.ACCOUNT_KEY), account);
        accountFragment.setArguments(bundle);
        ft.replace(R.id.fragment_container , accountFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private View.OnClickListener onClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.fab:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom).setTitle(R.string.create_account_title);
                        final View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.create_account_form, (ViewGroup) getView(), false);
                        builder.setView(viewInflated);

                        // populating the spinner
                        final AutoCompleteTextView textViewAccountType = viewInflated.findViewById(R.id.create_account_type);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.account_types));
                        if(textViewAccountType != null){
                            textViewAccountType.setAdapter(adapter);
                        }

                        final TextInputEditText inputAccountName =  viewInflated.findViewById(R.id.create_account_name);

                        // to dismiss the keyboard, when we click somewhere else.
                        inputAccountName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(inputAccountName.getWindowToken(), 0);
                            }
                        });


                        builder.setMessage(R.string.create_account_description)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // checking if inputs are empty before calling the repo to store the account
                                        if(!inputAccountName.getText().toString().equals("") &&
                                                !textViewAccountType.getText().toString().equals("")){
                                            ((MainActivity)getActivity()).getFireStoreRepo().saveAccount(
                                                    user.getId(),
                                                    inputAccountName.getText().toString(),
                                                    textViewAccountType.getText().toString().toLowerCase(),
                                                    0);
                                        }

                                    }
                                })
                                .show();
                        break;
                }
            }
        };
    }
}
