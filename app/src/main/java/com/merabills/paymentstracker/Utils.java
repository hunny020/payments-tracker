package com.merabills.paymentstracker;

import android.os.SystemClock;
import android.view.View;

import com.merabills.paymentstracker.model.Payment;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.WeakHashMap;

public final class Utils {

    private Utils() {}

    private static final Map<View, Long> lastClickMap = new WeakHashMap<>();

    /**
     * Attach a debounced click listener to the given view.
     *
     * @param view           the view to attach the listener to
     * @param debounceMillis minimum time between accepted clicks in milliseconds
     * @param listener       the real click action to run when not debounced
     */
    public static void setDebouncedClickListener(final View view, final long debounceMillis,
                                                 final View.OnClickListener listener) {
        view.setOnClickListener(v -> {
            long now = SystemClock.uptimeMillis();
            synchronized (lastClickMap) {
                Long last = lastClickMap.get(v);
                if (last == null || (now - last) >= debounceMillis) {
                    // accept this click
                    lastClickMap.put(v, now);
                    listener.onClick(v);
                }
                // else: ignored because of debounce
            }
        });
    }

    /**
     * Convenience overload with a default debounce of 400 ms.
     */
    public static void setDebouncedClickListener(final View view, final View.OnClickListener listener) {
        setDebouncedClickListener(view, 400L, listener);
    }

    public static String getPaymentChipText(Payment payment) {
        return payment.getType().getPaymentName() + ": Rs." + Utils.getUserVisibleAmount(payment.getAmount());
    }

    public static String getUserVisibleAmount(Double amount) {
        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(amount);
    }
}
