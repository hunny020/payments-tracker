package com.merabills.paymentstracker.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final WeakReference<MainActivity> activityRef = new WeakReference<>(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        if (getSupportActionBar() != null) getSupportActionBar().hide();
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
                    uiHandler.post(() -> {
                        MainActivity activity = activityRef.get();
                        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                            activity.enableDisableSaveButton(true);
                            Toast.makeText(activity, R.string.payment_saved, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(String message) {
                    uiHandler.post(() -> {
                        MainActivity activity = activityRef.get();
                        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                            activity.enableDisableSaveButton(true);
                            String errorMessage = (message != null && !message.isEmpty()) ? message : getString(R.string.something_went_wrong);
                            Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });

       Utils.setDebouncedClickListener(binding.reset, view -> {
           viewModel.clearPayments();
       });
    }

    private void enableDisableSaveButton(boolean isEnable) {
        if (isEnable) {
            binding.ctaSave.setEnabled(true);
            binding.ctaSave.setText(R.string.save);
            binding.savePb.setVisibility(View.GONE);
        } else {
            binding.ctaSave.setEnabled(false);
            binding.ctaSave.setText(null);
            binding.savePb.setVisibility(View.VISIBLE);
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
        if (binding == null) return;
        
        binding.chipGroupPayments.removeAllViews();
        if (payments == null || payments.isEmpty()) {
            Utils.setViewsVisible(binding.noPaymentsTitle, binding.addPayment);
            Utils.setViewsGone(binding.titlePayments, binding.chipGroupPayments, binding.ctaSave, binding.reset);
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
            chip.setText(Utils.getPaymentChipText(payment));
            ChipGroup.LayoutParams lp = new ChipGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            chip.setLayoutParams(lp);
            chip.setCloseIconVisible(true);
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setOnCloseIconClickListener(v -> {
                viewModel.removePayment(payment);
                Toast.makeText(this, R.string.payment_removed, Toast.LENGTH_SHORT).show();
            });
            
            String typeName = payment.getType() != null ? payment.getType().name() : "Unknown";
            String amount = payment.getAmount() != null ? payment.getAmount().toString() : "0";
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
    public void onPaymentCreated(Payment payment) {
        viewModel.addPayment(payment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
