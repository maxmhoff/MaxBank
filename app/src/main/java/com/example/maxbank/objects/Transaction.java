package com.example.maxbank.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Date;


public class Transaction implements Parcelable {
    private String id;
    private BigDecimal amount;
    private Date entryDate;
    private String text;
    private String accountId;

    public Transaction(String id, BigDecimal amount, Date entryDate, String text, String accountId) {
        this.id = id;
        this.amount = amount;
        this.entryDate = entryDate;
        this.text = text;
        this.accountId = accountId;
    }

    protected Transaction(Parcel in) {
        id = in.readString();
        amount = (BigDecimal) in.readSerializable();
        entryDate = (Date) in.readSerializable();
        text = in.readString();
        accountId = in.readString();
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeSerializable(amount);
        dest.writeSerializable(entryDate);
        dest.writeString(text);
        dest.writeString(accountId);
    }

    public String getId() {
        return id;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getText() {
        return text;
    }

    public String getAccountId() {
        return accountId;
    }
}
