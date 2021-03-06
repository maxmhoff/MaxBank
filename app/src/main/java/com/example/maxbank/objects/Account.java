package com.example.maxbank.objects;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Account implements Parcelable {
    private String id;
    private String name;
    private String type;
    private BigDecimal balance;
    private List<Transaction> transactions;

    public Account(String id, String name, String type, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.balance = balance;
        transactions = new ArrayList<>();
    }

    protected Account(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readString();
        balance = (BigDecimal) in.readSerializable();
        transactions = in.createTypedArrayList(Transaction.CREATOR);
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        sortTransactions();
        updateBalance();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    private void sortTransactions(){
        Collections.sort(transactions, new Comparator<Transaction>() {
            public int compare(Transaction o1, Transaction o2) {
                return o1.getEntryDate().compareTo(o2.getEntryDate());
            }
        });
        Collections.reverse(transactions);
    }

    private void updateBalance(){
        BigDecimal bd = new BigDecimal(0);
        for (Transaction t : this.transactions) {
            bd = bd.add(t.getAmount());
        }
        this.balance = bd;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeSerializable(balance);
        dest.writeTypedList(transactions);
    }
}
