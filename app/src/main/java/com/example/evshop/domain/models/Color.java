// File: Color.java
package com.example.evshop.domain.models;

import androidx.annotation.NonNull; // Import để dùng @NonNull

import com.google.gson.annotations.SerializedName;

public class Color {    // Sửa lại tên các trường cho khớp với JSON
    @SerializedName("id")
    private String id;
    private String colorName;
    private String colorCode;
    private double extraCost;

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getColorName() {
        return colorName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getExtraCost() {
        return extraCost;
    }

    /**
     * Hàm này rất quan trọng. ArrayAdapter sẽ gọi nó để biết
     * phải hiển thị chuỗi gì trong Spinner.
     */
    @NonNull
    @Override
    public String toString() {
        // Trả về colorName để hiển thị.
        return colorName != null ? colorName : "Màu không xác định";
    }
}
