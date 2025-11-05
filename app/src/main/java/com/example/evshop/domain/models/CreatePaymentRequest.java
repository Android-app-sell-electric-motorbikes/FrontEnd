package com.example.evshop.domain.models;

public class CreatePaymentRequest {
    private final String orderInfo;
    private final double amount;

    public CreatePaymentRequest(String orderInfo, double amount) {
        this.orderInfo = orderInfo;
        this.amount = amount;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public double getAmount() {
        return amount;
    }
}
