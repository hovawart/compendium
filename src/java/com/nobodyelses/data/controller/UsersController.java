package com.nobodyelses.data.controller;

import java.util.List;

import com.nobodyelses.data.model.User;

public class UsersController extends UserGenericController {
    @Override
    protected String toJson(List list) {
        for (Object obj : list) {
            User user = (User) obj;
            String accountNumber = user.getKey().getId().toString();
            user.setAccountNumber(accountNumber);
        }
        return super.toJson(list);
    }

    @Override
    protected String toJson(Object obj) {
        User user = (User) obj;
        String accountNumber = user.getKey().getId().toString();
        user.setAccountNumber(accountNumber);
        return super.toJson(user);
    }
}
