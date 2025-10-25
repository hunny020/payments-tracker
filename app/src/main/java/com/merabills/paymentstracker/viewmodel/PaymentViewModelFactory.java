package com.merabills.paymentstracker.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import com.merabills.paymentstracker.data.PaymentsStore;

public class PaymentViewModelFactory extends AbstractSavedStateViewModelFactory {

    private final PaymentsStore store;

    public PaymentViewModelFactory(@NonNull SavedStateRegistryOwner owner,
                                   @NonNull PaymentsStore store) {
        super(owner, null);
        this.store = store;
    }

    @NonNull
    @Override
    protected <T extends ViewModel> T create(@NonNull String s, @NonNull Class<T> aClass, @NonNull SavedStateHandle savedStateHandle) {
        if (aClass.isAssignableFrom(PaymentViewModel.class)) {
            return (T) new PaymentViewModel(savedStateHandle, store);
        }
        throw new IllegalArgumentException("Exception in creating viewmodel instance");
    }
}
