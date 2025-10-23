package com.merabills.paymentstracker.helper;

import com.merabills.paymentstracker.model.Payment;

public interface OnPaymentCreatedListener {

    void onPaymentCreated(Payment payment);
}
