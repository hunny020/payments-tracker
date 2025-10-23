package com.merabills.paymentstracker.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentData {

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("payments")
    private List<Payment> payments;

    public PaymentData() {}

    public PaymentData(double totalAmount, List<Payment> payments) {
        this.totalAmount = totalAmount;
        this.payments = payments;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<Payment> getPayments() {
        return payments;
    }

}
