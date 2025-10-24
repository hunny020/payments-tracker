package com.merabills.paymentstracker;

public class AppConstants {

    // File names
    public static final String PAYMENTS_FILE_NAME = "LastPayment.txt";
    
    // Bundle arguments
    public static final String ARG_AVAILABLE_TYPES = "available_types";
    
    // UI Constants
    public static final long DEFAULT_DEBOUNCE_TIME_MS = 400L;
    public static final double MAX_AMOUNT_VALUE = 999999999.99;
    public static final int MAX_DECIMAL_PLACES = 2;
    
    private AppConstants() {}
}
