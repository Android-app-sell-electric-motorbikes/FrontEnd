package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class InventoryResult {

    @SerializedName("data")
    private List<InventoryItem> data;

    // Bạn có thể thêm các trường khác như "pagination" ở đây nếu API có trả về

    public List<InventoryItem> getData() {
        return data;
    }
}
