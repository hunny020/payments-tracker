package com.merabills.paymentstracker.model;

import android.content.Context;
import androidx.annotation.StringRes;
import com.merabills.paymentstracker.R;

public enum PaymentType {
    CASH(R.string.payment_type_cash),
    BANK_TRANSFER(R.string.payment_type_bank_transfer),
    CREDIT_CARD(R.string.payment_type_credit_card);

    @StringRes
    private final int paymentResId;

    PaymentType(@StringRes int paymentName) {
        this.paymentResId = paymentName;
    }

    public String getPaymentName(Context context) {
        return context.getString(paymentResId);
    }

    public static PaymentType getTypeFromPaymentName(Context context, String name) {
        for (PaymentType type : PaymentType.values()) {
            if (type.getPaymentName(context).equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

}
