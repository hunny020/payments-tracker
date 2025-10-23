package com.merabills.paymentstracker;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.merabills.paymentstracker.model.Payment;
import com.merabills.paymentstracker.model.PaymentType;

import org.junit.Test;

public class PaymentTest {

    @Test
    public void paymentSerialization_shouldPreserveFields() {
        Payment payment = new Payment(PaymentType.BANK_TRANSFER, 120.50, "ICICI", "Ref123");
        Gson gson = new Gson();

        String json = gson.toJson(payment);
        Payment fromJson = gson.fromJson(json, Payment.class);

        assertEquals(payment.getType(), fromJson.getType());
        assertEquals(payment.getAmount(), fromJson.getAmount(), 0.001);
        assertEquals(payment.getProvider(), fromJson.getProvider());
        assertEquals(payment.getTransactionRef(), fromJson.getTransactionRef());
    }

}
