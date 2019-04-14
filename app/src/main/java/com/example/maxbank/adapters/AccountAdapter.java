package com.example.maxbank.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.maxbank.R;
import com.example.maxbank.objects.Account;

import java.math.RoundingMode;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {
    private Context context;
    private List<Account> accounts;
    private OnItemClickListener mListener;

    public AccountAdapter(Context context, List<Account> accounts) {
        this.context = context;
        this.accounts = accounts;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    @NonNull
    @Override
    public AccountAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_account, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull AccountAdapter.ViewHolder holder, int position) {
            Account account = accounts.get(position);
            holder.accountName.setText(account.getName());
            String accountBalanceString = account.getBalance().setScale(2, RoundingMode.HALF_UP).toPlainString() + " kr.";
            holder.accountBalance.setText(accountBalanceString);
    }

    @Override
    public int getItemCount() {
        return this.accounts.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView accountName;
        private TextView accountBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.accountName);
            accountBalance = itemView.findViewById(R.id.accountBalance);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
