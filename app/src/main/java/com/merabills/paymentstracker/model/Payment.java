package com.merabills.paymentstracker.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Payment implements Parcelable {

    @SerializedName("type")
    private PaymentType type;

    @SerializedName("amount")
    private Double amount;

    @SerializedName("provider") // optional – such as ICICI/Citibank
    private String provider;

    @SerializedName("transactionRef") // optional – reference ID
    private String transactionRef;

    public Payment(PaymentType type, Double amount, String provider, String transactionRef) {
        this.type = type;
        this.amount = amount;
        this.provider = provider;
        this.transactionRef = transactionRef;
    }

    public Payment() {}

    // ---------- Getters & Setters ----------
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

    // ---------- Parcelable implementation ----------
    protected Payment(Parcel in) {
        String typeName = in.readString();
        if (typeName != null) {
            this.type = PaymentType.valueOf(typeName);
        }

        if (in.readByte() == 0) {
            this.amount = null;
        } else {
            this.amount = in.readDouble();
        }

        this.provider = in.readString();
        this.transactionRef = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type != null ? type.name() : null);

        if (amount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(amount);
        }

        dest.writeString(provider);
        dest.writeString(transactionRef);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Payment> CREATOR = new Creator<>() {
        @Override
        public Payment createFromParcel(Parcel in) {
            return new Payment(in);
        }

        @Override
        public Payment[] newArray(int size) {
            return new Payment[size];
        }
    };
}

