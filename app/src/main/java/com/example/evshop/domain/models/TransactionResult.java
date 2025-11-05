package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionResult {
    @SerializedName("data")
    private List<Transaction> data;

    @SerializedName("pagination")
    private Pagination pagination;

    // Getters
    public List<Transaction> getData() {
        return data;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
