package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

public class VnpayResponse {
    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("result")
    private String result;

    // Getters
    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResult() {
        return result;
    }
}
