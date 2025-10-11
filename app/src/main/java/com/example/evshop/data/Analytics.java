package com.example.evshop.data;

import android.util.Log;


public class Analytics {
    public void log(String event, String detail) {
        Log.d("Analytics", event + ": " + detail);
    }

    public void viewProduct(String id) { log("view_product", id); }
    public void addToCart(String id) { log("click_add_to_cart", id); }
    public void applyFilter(String payload) { log("apply_filter", payload); }
}