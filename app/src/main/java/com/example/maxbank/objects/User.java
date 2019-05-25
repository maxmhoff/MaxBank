package com.example.maxbank.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class User implements Parcelable {
    private String id;
    private String name;
    private Date dayOfBirth;
    private String branch;
    private List<Account> accounts;

    public User(String id, String name, Date dayOfBirth, String branch) {
        this.id = id;
        this.name = name;
        this.dayOfBirth = dayOfBirth;
        this.branch = branch;
    }

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        dayOfBirth = (Date)in.readSerializable();
        branch = in.readString();
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

    public String getName() {
        return name;
    }

    public String getBranch() {
        return branch;
    }

    public boolean isSeniorCitizen(){
        // Since I'm using Date() I have to do it this clumsy way
        Calendar calDayOfBirth = Calendar.getInstance();
        calDayOfBirth.setTime(dayOfBirth);
        Calendar calNow = Calendar.getInstance();
        calNow.setTime(new Date());
        int age = calDayOfBirth.get(YEAR) - calNow.get(YEAR);
        if (calDayOfBirth.get(MONTH) > calNow.get(MONTH) ||
                (calDayOfBirth.get(MONTH) == calNow.get(MONTH) && calDayOfBirth.get(DATE) > calNow.get(DATE))) {
            age--;
        }
        if(age >= 77){
            return true;
        }
        return false;
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
        dest.writeString(branch);
        dest.writeTypedList(accounts);
    }
}
