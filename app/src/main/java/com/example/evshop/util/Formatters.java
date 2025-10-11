package com.example.evshop.util;

import java.text.NumberFormat;
import java.util.Locale;


public class Formatters {
    private static final Locale VI = new Locale("vi", "VN");
    public static String currency(long vnd) {
        return NumberFormat.getCurrencyInstance(VI).format(vnd);
    }
}