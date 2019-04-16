package com.example.maxbank.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.maxbank.R;
import com.example.maxbank.adapters.TransactionAdapter;
import com.example.maxbank.objects.Account;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAccountInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class AccountFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Account account;

    private static final String TAG = "AccountFragment";

    private View mView;
    private TextView headline;
    private RecyclerView transactions;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnAccountInteractionListener mListener;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        account = getArguments().getParcelable(getString(R.string.ACCOUNT_KEY));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
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
        if (context instanceof OnAccountInteractionListener) {
            mListener = (OnAccountInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAccountInteractionListener");
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
    public interface OnAccountInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void initViews(){
        headline = mView.findViewById(R.id.headline);
        headline.setText(account.getName());
        transactions = mView.findViewById(R.id.transactions);
        updateView();
    }

    public void updateView(){
        // updateView() should only be used when the view is active.
        if(mView != null){
            // should probably not be done within the fragment
            ArrayList<BigDecimal> accountBalanceList = new ArrayList<>();
            accountBalanceList.add(account.getBalance());
            for (int i = 0; i < account.getTransactions().size()-1; i++) {
                accountBalanceList.add(accountBalanceList.get(i).subtract(account.getTransactions().get(i).getAmount()));
            }

            TransactionAdapter transactionAdapter = new TransactionAdapter(mView.getContext(), account.getTransactions(), accountBalanceList);
            transactions.setAdapter(transactionAdapter);
            transactions.setLayoutManager(new LinearLayoutManager(getContext()));
            try {
                DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
                transactions.addItemDecoration(itemDecorator);
            } catch (NullPointerException nPEX){
                Log.w(TAG, "Item decorator could not be instantiated.");
            }
        }

    }

}
