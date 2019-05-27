package com.example.maxbank.interfaces;

import com.example.maxbank.objects.Account;

public interface GetAccountListener {
    void onSuccess(Account account);
    void onError();
}
