package com.example.evshop.domain.models;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Color implements Serializable {
    @SerializedName("colorId")
    private String colorId;

    @SerializedName("colorName")
    private String colorName;

    // Getters
    public String getColorId() { return colorId; }
    public String getColorName() { return colorName; }

    @NonNull
    @Override
    public String toString() {
        return colorName != null ? colorName : "Màu không xác định";
    }
}
