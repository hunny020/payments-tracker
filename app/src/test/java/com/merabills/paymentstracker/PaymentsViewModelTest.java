package com.merabills.paymentstracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.SavedStateHandle;

import com.merabills.paymentstracker.data.PaymentsStore;
import com.merabills.paymentstracker.model.Payment;
import com.merabills.paymentstracker.model.PaymentData;
import com.merabills.paymentstracker.model.PaymentType;
import com.merabills.paymentstracker.viewmodel.PaymentViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class PaymentsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantRule = new InstantTaskExecutorRule();

    private PaymentViewModel viewModel;
    PaymentsStore fakeStore = new PaymentsStore() {
        @Override
        public void savePaymentData(PaymentData paymentData) throws IOException {

        }

        @Override
        public PaymentData loadPaymentData() throws IOException {
            return new PaymentData();
        }
    };

    @Before
    public void setup() {
        viewModel = new PaymentViewModel(new SavedStateHandle(), fakeStore);
    }

    @Test
    public void addPayment_shouldIncreaseListSize() {
        Payment p = new Payment(PaymentType.BANK_TRANSFER, 100.0, "", "");
        viewModel.addPayment(p);
        List<Payment> list = viewModel.paymentsLD.getValue();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(PaymentType.BANK_TRANSFER, list.get(0).getType());
    }

    @Test
    public void total_isCalculatedCorrectly() {
        viewModel.addPayment(new Payment(PaymentType.CASH, 100.0, "", ""));
        viewModel.addPayment(new Payment(PaymentType.CREDIT_CARD, 250.0, "", ""));
        double total = viewModel.getTotalAmount();
        assertEquals(350.0, total, 0.001);
    }

}
