package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp này đại diện cho toàn bộ đối tượng "result" trong API get-evc-inventory.
 * Nó chứa danh sách dữ liệu kho xe ("data") và thông tin phân trang ("pagination").
 */
public class InventoryResult {

    @SerializedName("data")
    private List<InventoryItem> data;

    @SerializedName("pagination")
    private Pagination pagination;

    // --- Getters ---

    public List<InventoryItem> getData() {
        return data;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
