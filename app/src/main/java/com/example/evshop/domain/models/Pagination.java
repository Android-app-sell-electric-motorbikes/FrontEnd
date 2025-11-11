package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

public class Pagination {
    @SerializedName("pageNumber")
    private int pageNumber;

    @SerializedName("pageSize")
    private int pageSize;

    @SerializedName("totalItems")
    private int totalItems;

    @SerializedName("totalPages")
    private int totalPages;

    // Getters
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public int getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
}
