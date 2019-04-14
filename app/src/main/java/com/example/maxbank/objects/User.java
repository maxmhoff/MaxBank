package com.example.maxbank.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class User implements Parcelable {
    private String id;
    private String name;
    private Date dayOfBirth;
    private List<Account> accounts;

    public User(String id, String name, Date dayOfBirth) {
        this.id = id;
        this.name = name;
        this.dayOfBirth = dayOfBirth;
    }

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        dayOfBirth = (Date)in.readSerializable();
        accounts = in.createTypedArrayList(Account.CREATOR);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getId() {
        return id;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeSerializable(dayOfBirth);
        dest.writeList(accounts);
    }
}
