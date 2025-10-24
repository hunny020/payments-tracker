package com.merabills.paymentstracker.model;

public enum PaymentType {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    CREDIT_CARD("Credit Card");

    private final String paymentName;

    PaymentType(String paymentName) {
        this.paymentName = paymentName;
    }

    public String getPaymentName() {
        return paymentName;
    }

    public static PaymentType getTypeFromPaymentName(String name) {
        for (PaymentType type : PaymentType.values()) {
            if (type.getPaymentName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

}
