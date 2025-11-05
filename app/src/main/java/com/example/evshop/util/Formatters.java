package com.example.evshop.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Formatters {
    private static final Locale VI = new Locale("vi", "VN");

    public static String currency(double amount) {
        return NumberFormat.getCurrencyInstance(VI).format(amount);
    }

    public static String date(String isoString) {
        if (isoString == null) {
            return "N/A";
        }
        try {
            // Định dạng của API: 2025-11-05T07:30:33.657606Z
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
            apiFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = apiFormat.parse(isoString);

            // Định dạng hiển thị mong muốn: 05-11-2025 07:30
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
            return displayFormat.format(date);
        } catch (ParseException e) {
            // Nếu không phân tích được, trả về chuỗi gốc
            return isoString;
        }
    }
}
