package com.merabills.paymentstracker.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.merabills.paymentstracker.AppConstants;
import com.merabills.paymentstracker.R;
import com.merabills.paymentstracker.databinding.DialogAddPaymentBinding;
import com.merabills.paymentstracker.helper.OnPaymentCreatedListener;
import com.merabills.paymentstracker.model.Payment;
import com.merabills.paymentstracker.model.PaymentType;

import java.util.ArrayList;


public class AddPaymentDialog extends DialogFragment {

    private OnPaymentCreatedListener listener;
    public static final String TAG = "AddPaymentDialog";
    private DialogAddPaymentBinding binding;
    private ArrayList<PaymentType> availablePaymentTypes;
    private final Double amountMaxValue = 999999999.99;

    public static AddPaymentDialog newInstance(ArrayList<PaymentType> availableTypes) {
        AddPaymentDialog dialog = new AddPaymentDialog();
        Bundle args = new Bundle();
        args.putSerializable(AppConstants.ARG_AVAILABLE_TYPES, availableTypes);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnPaymentCreatedListener) {
            listener = (OnPaymentCreatedListener) context;
        } else if (getParentFragment() instanceof OnPaymentCreatedListener) {
            listener = (OnPaymentCreatedListener) getParentFragment();
        } else {
            throw new IllegalStateException("Host must implement OnPaymentCreatedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddPaymentBinding.inflate(getLayoutInflater());

        // Setup spinner
        if (getArguments() != null) {
            availablePaymentTypes = (ArrayList<PaymentType>) getArguments().getSerializable(AppConstants.ARG_AVAILABLE_TYPES);
        } else {
            availablePaymentTypes = new ArrayList<>();
        }

        ArrayList<String> optionNames = new ArrayList<>();
        for(PaymentType type: availablePaymentTypes) {
            optionNames.add(type.getPaymentName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                optionNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPaymentType.setAdapter(adapter);

        setupClickListeners();
        addTextChangedListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        binding.ctaOk.setOnClickListener(v -> {
            String amountStr = binding.etAmount.getText().toString().trim();
            if (TextUtils.isEmpty(amountStr)) {
                binding.etAmount.setError("Enter amount");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                binding.etAmount.setError("Invalid number");
                return;
            }

            PaymentType type = PaymentType.getTypeFromPaymentName(binding.spinnerPaymentType.getSelectedItem().toString());
            String provider = binding.etProvider.getText().toString().trim();
            String reference = binding.etTransactionRef.getText().toString().trim();

            Payment payment = new Payment(type, amount, provider, reference);

            if (listener != null) {
                listener.onPaymentCreated(payment);
            }
            dismiss();
        });

        binding.ctaCancel.setOnClickListener(v -> dismiss());

        binding.spinnerPaymentType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                PaymentType selected = availablePaymentTypes.get(position);
                if (selected == PaymentType.BANK_TRANSFER || selected == PaymentType.CREDIT_CARD) {
                    binding.etProvider.setVisibility(View.VISIBLE);
                    binding.etTransactionRef.setVisibility(View.VISIBLE);
                } else {
                    binding.etProvider.setVisibility(View.GONE);
                    binding.etTransactionRef.setVisibility(View.GONE);
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void addTextChangedListeners() {
        binding.etAmount.addTextChangedListener(new TextWatcher() {

            private String previousValue = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousValue = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                // Remove the error as soon as user types something
                if (!TextUtils.isEmpty(s)) {
                    binding.etAmount.setError(null);
                }
                //allow upto 2 decimal places

                if (input.isEmpty() || input.equals(".")) {
                    binding.etAmount.setError(null);
                    return;
                }

                try {
                    double value = Double.parseDouble(input);

                    // Limit to 2 decimal digits
                    if (input.contains(".")) {
                        int index = input.indexOf(".");
                        if (input.length() - index - 1 > 2) {
                            // Restore previous value if too many decimals
                            binding.etAmount.removeTextChangedListener(this);
                            binding.etAmount.setText(previousValue);
                            binding.etAmount.setSelection(previousValue.length());
                            binding.etAmount.addTextChangedListener(this);
                            return;
                        }
                    }

                    // Limit max value
                    if (value > 999999999.99) {
                        binding.etAmount.setError("Maximum allowed amount is 999999999.99");

                        // Restore previous value
                        binding.etAmount.removeTextChangedListener(this);
                        binding.etAmount.setText(previousValue);
                        binding.etAmount.setSelection(previousValue.length());
                        binding.etAmount.addTextChangedListener(this);
                        return;
                    } else {
                        binding.etAmount.setError(null);
                    }

                } catch (NumberFormatException e) {
                    // Invalid number (e.g. multiple dots)
                    binding.etAmount.removeTextChangedListener(this);
                    binding.etAmount.setText(previousValue);
                    binding.etAmount.setSelection(previousValue.length());
                    binding.etAmount.addTextChangedListener(this);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Optional: Make dialog width match parent
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow()
                    .setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            d.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            d.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            );
        }
    }

    @Override
    public void dismiss() {
        dismissAllowingStateLoss();
    }
}

