package com.merabills.paymentstracker.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.merabills.paymentstracker.data.PaymentsStore;

public class PaymentViewModelFactory implements ViewModelProvider.Factory {

    private final PaymentsStore store;

    public PaymentViewModelFactory(PaymentsStore store) {
        this.store = store;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(PaymentViewModel.class)) {
            return (T) new PaymentViewModel(store);
        }
        throw new IllegalArgumentException("Exception in creating viewmodel instance");
    }
}
