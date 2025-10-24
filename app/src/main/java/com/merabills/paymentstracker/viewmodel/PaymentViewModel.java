package com.merabills.paymentstracker.viewmodel;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.merabills.paymentstracker.Utils;
import com.merabills.paymentstracker.data.PaymentsStore;
import com.merabills.paymentstracker.helper.GenericCallback;
import com.merabills.paymentstracker.model.Payment;
import com.merabills.paymentstracker.model.PaymentData;
import com.merabills.paymentstracker.model.PaymentType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentViewModel extends ViewModel {

    private final String TAG = "PaymentViewModel";
    private final PaymentsStore store;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Payment>> _paymentsLD = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Payment>> paymentsLD = _paymentsLD;

    public PaymentViewModel(PaymentsStore store) {
        this.store = store;
    }

    public double getTotalAmount() {
        List<Payment> list = paymentsLD.getValue();
        if (list == null || list.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Payment p : list) {
            if (p != null && p.getAmount() != null) {
                sum += p.getAmount();
            }
        }
        return sum;
    }

    /**
     * Adds a new {@link Payment} entry to the current list of payments.
     * After successfully adding a payment, the updated list is set on {@code _paymentsLD}
     */
    public void addPayment(final Payment p) {
        if (p == null || p.getType() == null) return;

        List<Payment> currentPayments = _paymentsLD.getValue();
        if (currentPayments == null) currentPayments = new ArrayList<>();

        // allow each payment only once
        for (Payment payment : currentPayments) {
            if (payment != null && payment.getType() == p.getType()) {
                return;
            }
        }
        
        final List<Payment> copy = new ArrayList<>(currentPayments);
        copy.add(p);
        _paymentsLD.setValue(copy);
    }

    /**
     * Removes an existing {@link Payment} entry from the current list of payments.
     * After successfully removing a payment, the updated list is set on {@code _paymentsLD}
     */
    public void removePayment(final Payment p) {
        if (p == null || p.getType() == null) return;
        
        List<Payment> currentPayments = _paymentsLD.getValue();
        if (currentPayments == null || currentPayments.isEmpty()) return;
        
        List<Payment> copy = new ArrayList<>(currentPayments);
        boolean removed = false;
        for(Payment payment : currentPayments) {
            if (payment != null && payment.getType() == p.getType()) {
                copy.remove(payment);
                removed = true;
                break; // Only remove the first match
            }
        }
        if (removed) _paymentsLD.setValue(copy);
    }

    /**
     * Save current payments to storage on background thread.
     */
    public void savePayment(final GenericCallback callback) {
        final List<Payment> currPaymentsList = _paymentsLD.getValue() == null ? new ArrayList<>() : new ArrayList<>(_paymentsLD.getValue());
        executor.execute(() -> {
            try {
                double totalAmount = 0.0;
                for (Payment payment : currPaymentsList) {
                    if (payment != null && payment.getAmount() != null) {
                        totalAmount += payment.getAmount();
                    }
                }
                PaymentData data = new PaymentData(totalAmount, currPaymentsList);
                store.savePaymentData(data);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Failed to save payments", e);
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }
        });
    }

    /**
     * Load payments from storage on background thread.
     */
    public void loadPayments() {
        executor.execute(() -> {
            try {
                PaymentData paymentData = store.loadPaymentData();
                final List<Payment> paymentsList = paymentData == null || paymentData.getPayments() == null ? new ArrayList<>() : new ArrayList<>(paymentData.getPayments());
                Utils.changeValueLD(_paymentsLD, paymentsList);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load payments", e);
                Utils.changeValueLD(_paymentsLD, new ArrayList<>());
            }
        });
    }

    public List<PaymentType> getAvailablePaymentTypes() {
        List<Payment> currentPayments = paymentsLD.getValue();
        EnumSet<PaymentType> currentPaymentTypes = EnumSet.noneOf(PaymentType.class);

        if (currentPayments != null) {
            for (Payment p : currentPayments) {
                if (p != null && p.getType() != null) {
                    currentPaymentTypes.add(p.getType());
                }
            }
        }

        List<PaymentType> availablePaymentTypes = new ArrayList<>();
        for (PaymentType type : PaymentType.values()) {
            if (type != null && !currentPaymentTypes.contains(type)) {
                availablePaymentTypes.add(type);
            }
        }
        return availablePaymentTypes;
    }

    public void clearPayments() {
        _paymentsLD.setValue(new ArrayList<>());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
