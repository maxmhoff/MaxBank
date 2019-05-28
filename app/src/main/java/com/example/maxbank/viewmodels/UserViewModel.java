package com.example.maxbank.viewmodels;

import com.example.maxbank.objects.User;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private MutableLiveData<User> user;

    public MutableLiveData<User> getUser(){
        if(user == null){
            user = new MutableLiveData<>();
        }
        return user;
    }
}
