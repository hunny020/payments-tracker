package com.merabills.paymentstracker.model;

import com.google.gson.annotations.SerializedName;

public class Payment {

    @SerializedName("type")
    private PaymentType type;

    @SerializedName("amount")
    private Double amount;

    @SerializedName("provider") //optional - such as ICICI/Citibank, etc.
    private String provider;

    @SerializedName("transactionRef") //optional
    private String transactionRef;

    public Payment(PaymentType type, Double amount, String provider, String transactionRef) {
        this.type = type;
        this.amount = amount;
        this.provider = provider;
        this.transactionRef = transactionRef;
    }

    public Payment() {}

    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }
}

