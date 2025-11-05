package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp này đại diện cho một mục trong kho hàng,
 * tương ứng với một đối tượng trong mảng "result" từ API get-evc-inventory.
 */
public class InventoryItem {

    @SerializedName("modelName")
    public String modelName;
    @SerializedName("versionName")
    public String versionName;
    @SerializedName("colorName")
    public String colorName;
    @SerializedName("quantity")
    public int quantity;
    @SerializedName("vehicles")
    // *** SỬA LỖI Ở ĐÂY: Sử dụng lớp Vehicle của chính bạn, không phải của Google Maps ***
    public List<Vehicle> vehicles;


}
