package com.merabills.paymentstracker;

import android.os.Looper;
import android.os.SystemClock;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

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
        setDebouncedClickListener(view, AppConstants.DEFAULT_DEBOUNCE_TIME_MS, listener);
    }

    public static String getPaymentChipText(Payment payment) {
        return payment.getType().getPaymentName() + ": Rs." + Utils.getUserVisibleAmount(payment.getAmount());
    }

    /**
     * Converts a {@link Double} amount into a user-visible string with up to
     * two decimal places.
     * <p>
     */
    public static String getUserVisibleAmount(Double amount) {
        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(amount);
    }

    /**
     * Safely updates the value of a {@link MutableLiveData} from any thread.
     * <p>
     * If the current thread is the main (UI) thread, this method calls
     * {@link MutableLiveData#setValue(Object)} directly. Otherwise, it uses
     * {@link MutableLiveData#postValue(Object)} to post the value change
     * to the main thread asynchronously.
     * </p>
     *
     */
    public static <T> void changeValueLD(MutableLiveData<T> liveData, T value) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            liveData.setValue(value);
        } else {
            liveData.postValue(value);
        }
    }

    /**
     * Sets all provided views' visibility to {@link View#VISIBLE}.
     *
     * @param views one or more {@link View} objects to make visible.
     */
    public static void setViewsVisible(View... views) {
        if (views == null) return;
        for (View v : views) {
            if (v != null) v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets all provided views' visibility to {@link View#GONE}.
     */
    public static void setViewsGone(View... views) {
        if (views == null) return;
        for (View v : views) {
            if (v != null) v.setVisibility(View.GONE);
        }
    }
}
