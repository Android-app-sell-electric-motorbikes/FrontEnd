// File: Color.java
package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

public class Color {
    // Thêm các trường khác của Color nếu có từ JSON (ví dụ: colorId)
    @SerializedName("colorId")
    private String colorId;

    @SerializedName("colorName")
    private String colorName;

    // --- Getters ---
    public String getColorId() {
        return colorId;
    }

    public String getColorName() {
        return colorName;
    }

    @Override
    public String toString() {
        return colorName; // Trả về tên để hiển thị
    }
}
