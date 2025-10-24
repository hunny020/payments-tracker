package com.merabills.paymentstracker.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.merabills.paymentstracker.AppConstants;
import com.merabills.paymentstracker.R;
import com.merabills.paymentstracker.Utils;
import com.merabills.paymentstracker.databinding.DialogAddPaymentBinding;
import com.merabills.paymentstracker.helper.OnPaymentCreatedListener;
import com.merabills.paymentstracker.model.Payment;
import com.merabills.paymentstracker.model.PaymentType;

import java.util.ArrayList;


public class AddPaymentDialog extends DialogFragment {

    public static final String TAG = "AddPaymentDialog";
    private OnPaymentCreatedListener listener;
    private DialogAddPaymentBinding binding;
    private ArrayList<PaymentType> availablePaymentTypes;

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
        if (getArguments() != null) {
            availablePaymentTypes = (ArrayList<PaymentType>) getArguments().getSerializable(AppConstants.ARG_AVAILABLE_TYPES);
        } else {
            availablePaymentTypes = new ArrayList<>();
        }
        setupUi();
        setupClickListeners();
        addTextChangedListeners();
        return binding.getRoot();
    }

    private void setupUi() {
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
    }

    private void setupClickListeners() {
        binding.ctaOk.setOnClickListener(v -> {
            String amountStr = binding.etAmount.getText().toString().trim();
            if (TextUtils.isEmpty(amountStr)) {
                binding.etAmount.setError(getString(R.string.enter_amount));
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                binding.etAmount.setError(getString(R.string.invalid_number));
                return;
            }

            Object selectedItem = binding.spinnerPaymentType.getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            PaymentType type = PaymentType.getTypeFromPaymentName(selectedItem.toString());
            if (type == null) {
                return;
            }
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
                    Utils.setViewsVisible(binding.etProvider, binding.etTransactionRef);
                } else {
                    Utils.setViewsGone(binding.etProvider, binding.etTransactionRef);
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

                if (input.isEmpty() || input.equals(".")) {
                    binding.etAmount.setError(null);
                    return;
                }

                try {
                    double value = Double.parseDouble(input);

                    // Limit to 2 decimal digits
                    if (input.contains(".")) {
                        int index = input.indexOf(".");
                        if (input.length() - index - 1 > AppConstants.MAX_DECIMAL_PLACES) {
                            restorePreviousAmountValue(binding.etAmount, previousValue, this);
                            return;
                        }
                    }

                    // Limit max value
                    if (value > AppConstants.MAX_AMOUNT_VALUE) {
                        binding.etAmount.setError(getString(R.string.max_amount_error, AppConstants.MAX_AMOUNT_VALUE));
                        restorePreviousAmountValue(binding.etAmount, previousValue, this);
                    } else {
                        binding.etAmount.setError(null);
                    }
                } catch (NumberFormatException e) {
                    // Invalid number (e.g. multiple dots)
                    restorePreviousAmountValue(binding.etAmount, previousValue, this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void restorePreviousAmountValue(EditText et, String previousValue, TextWatcher textWatcher) {
        et.removeTextChangedListener(textWatcher);
        et.setText(previousValue);
        et.setSelection(previousValue.length());
        et.addTextChangedListener(textWatcher);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void dismiss() {
        dismissAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        listener = null;
    }
}

