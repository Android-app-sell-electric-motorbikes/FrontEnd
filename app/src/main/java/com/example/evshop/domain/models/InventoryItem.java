package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp này đại diện cho một mục trong kho hàng,
 * tương ứng với một đối tượng trong mảng "data" từ API get-evc-inventory.
 */
public class InventoryItem {

    @SerializedName("modelName")
    private String modelName;

    @SerializedName("versionName")
    private String versionName;

    @SerializedName("colorName")
    private String colorName;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("vehicles")
    // Danh sách này chứa các đối tượng Vehicle (chiếc xe cụ thể).
    private List<Vehicle> vehicles;

    // --- Getters ---

    public String getModelName() {
        return modelName;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getColorName() {
        return colorName;
    }

    public int getQuantity() {
        return quantity;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}
