package com.merabills.paymentstracker.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.merabills.paymentstracker.AppConstants;
import com.merabills.paymentstracker.model.PaymentData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FilePaymentsStore implements PaymentsStore {

    private final Context context;
    private final Gson gson = new Gson();

    public FilePaymentsStore(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public synchronized void savePaymentData(PaymentData paymentData) throws IOException {
        String json = gson.toJson(paymentData);
        try(FileOutputStream fos = context.openFileOutput(AppConstants.PAYMENTS_FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public synchronized PaymentData loadPaymentData() throws IOException {
        File file = new File(context.getFilesDir(), AppConstants.PAYMENTS_FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try(FileInputStream fis = context.openFileInput(AppConstants.PAYMENTS_FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr)) {
            String currLine;
            while ((currLine = br.readLine()) != null) {
                sb.append(currLine);
            }
        }
        try {
            return gson.fromJson(sb.toString(), PaymentData.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("Json exception in reading payment file");
        }
    }
}
