package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("id")
    private String id;

    @SerializedName("provider")
    private String provider;

    @SerializedName("orderRef")
    private String orderRef;

    @SerializedName("amount")
    private double amount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("status")
    private int status; // 1 for success, 0 for failure

    @SerializedName("createdAt")
    private String createdAt;

    // Getters
    public String getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getOrderRef() {
        return orderRef;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
