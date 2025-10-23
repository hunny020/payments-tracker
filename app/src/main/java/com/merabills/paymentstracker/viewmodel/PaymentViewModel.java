package com.merabills.paymentstracker.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    private final PaymentsStore store;

    private final MutableLiveData<List<Payment>> _paymentsLD = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Payment>> paymentsLD = _paymentsLD;

    private final MutableLiveData<Boolean> _dataLoadingLD = new MutableLiveData<>(false);
    public LiveData<Boolean> dataLoadingLD = _dataLoadingLD;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public PaymentViewModel(PaymentsStore store) {
        this.store = store;
    }

    public double getTotalAmount() {
        List<Payment> list = paymentsLD.getValue();
        if (list == null) return 0.0;
        double sum = 0.0;
        for (Payment p : list) {
            sum += p.getAmount();
        }
        return sum;
    }

    public void addPayment(final Payment p) {
        // enforce one-of-each in UI layer; double-check here to be defensive
        List<Payment> currentPayments = _paymentsLD.getValue();
        if (currentPayments == null) currentPayments = new ArrayList<>();
        for (Payment payment : currentPayments) {
            if (payment.getType() == p.getType()) return; // allow each payment only once
        }
        final List<Payment> copy = new ArrayList<>(currentPayments);
        copy.add(p);
        _paymentsLD.setValue(copy);
    }

    public void removePayment(final Payment p) {
        List<Payment> currentPayments = _paymentsLD.getValue();
        if (currentPayments == null || currentPayments.isEmpty()) return;
        List<Payment> copy = new ArrayList<>(currentPayments);
        boolean removed = false;
        for(Payment payment : copy) {
            if (payment.getType() == p.getType()) {
                copy.remove(payment);
                removed = true;
            }
        }
        if (removed) _paymentsLD.setValue(copy);
    }

    /**
     * Save current payments to storage on background thread. Callback runs on main thread:
     * callback.accept(null) on success, callback.accept(exception) on failure.
     */
    public void savePayment(final GenericCallback callback) {
        final List<Payment> currPaymentsList = _paymentsLD.getValue() == null ? new ArrayList<>() : new ArrayList<>(_paymentsLD.getValue());
        _dataLoadingLD.setValue(true);
        executor.execute(() -> {
            try {
                double totalAmount = 0.0;
                for (Payment payment : currPaymentsList) {
                    totalAmount += payment.getAmount();
                }
                PaymentData data = new PaymentData(totalAmount, currPaymentsList);
                store.savePaymentData(data);
                uiHandler.post(() -> {
                    _dataLoadingLD.setValue(false);
                    if (callback != null) callback.onSuccess();
                });
            } catch (Exception e) {
                uiHandler.post(() -> {
                    _dataLoadingLD.setValue(false);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
            }
        });
    }

    /**
     * Load payments from storage on background thread. Callback runs on main thread: callback.accept(null) on success,
     * callback.accept(exception) on failure.
     */
    public void loadPayments() {
        _dataLoadingLD.setValue(true);
        executor.execute(() -> {
            try {
                PaymentData paymentData = store.loadPaymentData();
                final List<Payment> paymentsList = paymentData == null || paymentData.getPayments() == null ? new ArrayList<>() : new ArrayList<>(paymentData.getPayments());
                uiHandler.post(() -> {
                    _paymentsLD.setValue(paymentsList);
                    _dataLoadingLD.setValue(false);
                });
            } catch (Exception e) {
                uiHandler.post(() -> {
                    // on error: prefer to leave empty list rather than crashing
                    _paymentsLD.setValue(new ArrayList<>());
                    _dataLoadingLD.setValue(false);
                });
            }
        });
    }

    public List<PaymentType> getAvailablePaymentTypes() {
        List<Payment> currentPayments = paymentsLD.getValue();
        EnumSet<PaymentType> currentPaymentTypes = EnumSet.noneOf(PaymentType.class);

        if (currentPayments != null) {
            for (Payment p : currentPayments) {
                currentPaymentTypes.add(p.getType());
            }
        }

        List<PaymentType> availablePaymentTypes = new ArrayList<>();
        for (PaymentType type : PaymentType.values()) {
            if (!currentPaymentTypes.contains(type)) {
                availablePaymentTypes.add(type);
            }
        }
        return availablePaymentTypes;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }

}
