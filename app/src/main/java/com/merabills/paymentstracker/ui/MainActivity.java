package com.merabills.paymentstracker.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.merabills.paymentstracker.R;
import com.merabills.paymentstracker.Utils;
import com.merabills.paymentstracker.data.FilePaymentsStore;
import com.merabills.paymentstracker.data.PaymentsStore;
import com.merabills.paymentstracker.databinding.ActivityMainBinding;
import com.merabills.paymentstracker.model.Payment;
import com.merabills.paymentstracker.model.PaymentType;
import com.merabills.paymentstracker.viewmodel.PaymentViewModel;
import com.merabills.paymentstracker.viewmodel.PaymentViewModelFactory;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PaymentViewModel viewModel;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private PaymentViewModelFactory paymentViewModelFactory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(PaymentViewModel.class);
        setClickListeners();
        setObservers();
        viewModel.loadPaymentsIfRequired();
    }

    @NonNull
    @Override
    public ViewModelProvider.Factory getDefaultViewModelProviderFactory() {
        if (paymentViewModelFactory == null) {
            PaymentsStore store = new FilePaymentsStore(getApplicationContext());
            paymentViewModelFactory = new PaymentViewModelFactory(this, store);
        }
        return paymentViewModelFactory;
    }

    private void setClickListeners() {
        Utils.setDebouncedClickListener(binding.addPayment, view -> {
            showAddPaymentDialog();
        });

        Utils.setDebouncedClickListener(binding.ctaSave, view -> {
            enableDisableSaveButton(false);
            viewModel.savePayment();
        });

       Utils.setDebouncedClickListener(binding.reset, view -> {
           viewModel.clearPayments();
       });
    }

    private void enableDisableSaveButton(boolean isEnable) {
        if (isEnable) {
            binding.ctaSave.setVisibility(View.VISIBLE);
        } else {
            binding.ctaSave.setVisibility(View.GONE);
        }
    }

    private void setObservers() {
        viewModel.paymentsLD.observe(this, payments -> {
            updateChipsGroup(payments);
            updateTotalAmount();
        });

        viewModel.savePaymentResult.observe(this, result -> {
            if (result == null) return;
            if (result) {
                enableDisableSaveButton(true);
                Toast.makeText(this, R.string.payment_saved, Toast.LENGTH_SHORT).show();
            } else {
                enableDisableSaveButton(true);
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
            viewModel.resetSavePaymentLD();
        });

    }

    private void showAddPaymentDialog() {
        List<PaymentType> availablePaymentTypes = viewModel.getAvailablePaymentTypes();
        if (availablePaymentTypes.isEmpty()) {
            Toast.makeText(this, R.string.all_payments_added_already, Toast.LENGTH_SHORT).show();
            return;
        }
        AddPaymentDialog dialog = AddPaymentDialog.newInstance();
        dialog.show(getSupportFragmentManager(), AddPaymentDialog.TAG);
    }

    private void updateChipsGroup(List<Payment> payments) {
        if (binding == null) return;
        
        binding.chipGroupPayments.removeAllViews();
        if (payments == null || payments.isEmpty()) {
            Utils.setViewsVisible(binding.noPaymentsTitle, binding.addPayment);
            Utils.setViewsGone(binding.titlePayments, binding.chipGroupPayments, binding.reset);
            return;
        } else {
            Utils.setViewsVisible(binding.chipGroupPayments, binding.ctaSave, binding.titlePayments, binding.reset);
            binding.noPaymentsTitle.setVisibility(View.GONE);
            if (payments.size() == PaymentType.values().length) {
                binding.addPayment.setVisibility(View.GONE);
            } else {
                binding.addPayment.setVisibility(View.VISIBLE);
            }
        }
        for (Payment payment : payments) {
            if (payment == null) continue;
            
            Chip chip = new Chip(this);

            String chipText = getString(R.string.chip_text, payment.getType().getPaymentName(this), Utils.getUserVisibleAmount(payment.getAmount()));
            chip.setText(chipText);
            ChipGroup.LayoutParams lp = new ChipGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            chip.setLayoutParams(lp);
            chip.setCloseIconVisible(true);
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setOnCloseIconClickListener(v -> {
                viewModel.removePayment(payment);
                Toast.makeText(this, R.string.payment_removed, Toast.LENGTH_SHORT).show();
            });
            
            String typeName = payment.getType() != null ? payment.getType().name() : getString(R.string.unknown);
            Double amount = payment.getAmount() != null ? payment.getAmount() : 0.0;
            chip.setContentDescription(getString(R.string.cd_payment_chip, typeName, amount));
            binding.chipGroupPayments.addView(chip);

            View spacer = new View(this);
            ChipGroup.LayoutParams spacerLp = new ChipGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1); // dummy spacer
            spacer.setLayoutParams(spacerLp);
            spacer.setVisibility(View.INVISIBLE); // invisible but occupies full width
            binding.chipGroupPayments.addView(spacer);
        }
    }

    private void updateTotalAmount() {
        if (binding == null) return;
        double total = viewModel.getTotalAmount();
        binding.totalAmountValue.setText(Utils.getUserVisibleAmount(total));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
