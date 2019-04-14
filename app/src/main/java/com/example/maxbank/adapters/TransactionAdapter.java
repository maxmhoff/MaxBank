package com.example.maxbank.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.maxbank.R;
import com.example.maxbank.objects.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private Context context;
    private List<Transaction> transactions;
    private List<BigDecimal> accountBalanceList;

    public TransactionAdapter(Context context, List<Transaction> transactions, ArrayList<BigDecimal> accountBalanceList) {
        this.context = context;
        this.transactions = transactions;
        this.accountBalanceList = accountBalanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.transactionText.setText(transaction.getText());

        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
        holder.transactionEntryDate.setText(DATE_FORMAT.format(transaction.getEntryDate()));

        String transactionAmountString = transaction.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString() + " kr.";
        holder.transactionAmount.setText(transactionAmountString);
        if(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0){
            holder.transactionAmount.setTextColor(context.getColor(R.color.colorIncome));
        } else if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0){
            holder.transactionAmount.setTextColor(context.getColor(R.color.colorExpense));
        }

        String transactionAccountBalanceString = accountBalanceList.get(position).setScale(2, RoundingMode.HALF_UP).toPlainString() + " kr.";
        holder.transactionAccountBalance.setText(transactionAccountBalanceString);

    }

    @Override
    public int getItemCount() {
        return this.transactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView transactionText;
        private TextView transactionAmount;
        private TextView transactionEntryDate;
        private TextView transactionAccountBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionText = itemView.findViewById(R.id.transactionText);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionEntryDate = itemView.findViewById(R.id.transactionEntryDate);
            transactionAccountBalance = itemView.findViewById(R.id.transactionAccountBalance);
        }
    }
}