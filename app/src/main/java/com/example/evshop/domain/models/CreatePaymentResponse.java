package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

public class CreatePaymentResponse {
    @SerializedName("paymentUrl")
    private String paymentUrl;

    public String getPaymentUrl() {
        return paymentUrl;
    }
}
