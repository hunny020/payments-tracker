package com.merabills.paymentstracker.data;

import com.merabills.paymentstracker.model.PaymentData;

import java.io.IOException;

public interface PaymentsStore {

    void savePaymentData(PaymentData paymentData) throws IOException;

    PaymentData loadPaymentData() throws IOException;

}
