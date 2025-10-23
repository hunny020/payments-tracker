package com.merabills.paymentstracker.helper;

public interface GenericCallback {
    void onSuccess();
    void onFailure(String message);
}
