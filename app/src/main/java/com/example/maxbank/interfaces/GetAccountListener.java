package com.example.maxbank.interfaces;

import com.example.maxbank.objects.Account;

public interface GetAccountListener {
    void onGetAccountSuccess(Account account);
    void onGetAccountError();
}
