package com.merabills.paymentstracker.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.merabills.paymentstracker.R;
import com.merabills.paymentstracker.Utils;
import com.merabills.paymentstracker.data.FilePaymentsStore;
import com.merabills.paymentstracker.databinding.ActivityMainBinding;
import com.merabills.paymentstracker.helper.GenericCallback;
import com.merabills.paymentstracker.helper.OnPaymentCreatedListener;
import com.merabills.paymentstracker.model.Payment;
import com.merabills.paymentstracker.model.PaymentType;
import com.merabills.paymentstracker.viewmodel.PaymentViewModel;
import com.merabills.paymentstracker.viewmodel.PaymentViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnPaymentCreatedListener {

    private ActivityMainBinding binding;
    private PaymentViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this, new PaymentViewModelFactory(new FilePaymentsStore(getApplicationContext()))).get(PaymentViewModel.class);
        setClickListeners();
        setObservers();
        viewModel.loadPayments();
    }

    private void setClickListeners() {
        Utils.setDebouncedClickListener(binding.addPayment, view -> {
            showAddPaymentDialog();
        });

        Utils.setDebouncedClickListener(binding.ctaSave, view -> {
            enableDisableSaveButton(false);
            viewModel.savePayment(new GenericCallback() {
                @Override
                public void onSuccess() {
                    enableDisableSaveButton(true);
                    Toast.makeText(binding.ctaSave.getContext(), R.string.payment_saved, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String message) {
                    enableDisableSaveButton(true);
                    Toast.makeText(binding.ctaSave.getContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void enableDisableSaveButton(boolean isEnable) {
        if (isEnable) {
            binding.ctaSave.setEnabled(true);
            binding.ctaSave.setText(null);
            binding.savePb.setVisibility(View.VISIBLE);
        } else {
            binding.ctaSave.setEnabled(false);
            binding.ctaSave.setText(R.string.save);
            binding.savePb.setVisibility(View.GONE);
        }
    }

    private void setObservers() {
        viewModel.paymentsLD.observe(this, payments -> {
            updateChipsGroup(payments);
            updateTotalAmount();
        });

    }

    private void showAddPaymentDialog() {
        List<PaymentType> availablePaymentTypes = viewModel.getAvailablePaymentTypes();
        if (availablePaymentTypes.isEmpty()) {
            Toast.makeText(this, R.string.all_payments_added_already, Toast.LENGTH_SHORT).show();
            return;
        }
        AddPaymentDialog dialog = AddPaymentDialog.newInstance(new ArrayList<>(availablePaymentTypes));
        dialog.show(getSupportFragmentManager(), AddPaymentDialog.TAG);
    }

    private void updateChipsGroup(List<Payment> payments) {
        binding.chipGroupPayments.removeAllViews();
        if (payments == null || payments.isEmpty()) {
            binding.noPaymentsTitle.setVisibility(View.VISIBLE);
            binding.titlePayments.setVisibility(View.GONE);
            binding.chipGroupPayments.setVisibility(View.GONE);
            binding.ctaSave.setVisibility(View.GONE);
            return;
        } else {
            binding.noPaymentsTitle.setVisibility(View.GONE);
            binding.chipGroupPayments.setVisibility(View.VISIBLE);
            binding.ctaSave.setVisibility(View.VISIBLE);
            binding.titlePayments.setVisibility(View.VISIBLE);
        }
        for (Payment payment : payments) {
            Chip chip = new Chip(this);
            chip.setText(Utils.getPaymentChipText(payment));
            chip.setCloseIconVisible(true);
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setOnCloseIconClickListener(v -> {
                viewModel.removePayment(payment);
                Toast.makeText(this, R.string.payment_removed, Toast.LENGTH_SHORT).show();
            });
            chip.setContentDescription(getString(R.string.cd_payment_chip, payment.getType().name(), payment.getAmount()));
            binding.chipGroupPayments.addView(chip);
        }
    }

    private void updateTotalAmount() {
        double total = viewModel.getTotalAmount();
        binding.totalAmountValue.setText(Utils.getUserVisibleAmount(total));
    }

    @Override
    public void onPaymentCreated(Payment payment) {
        viewModel.addPayment(payment);
    }
}
