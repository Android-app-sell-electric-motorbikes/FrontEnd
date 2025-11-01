package com.example.evshop.domain.models; // Phải cùng package với InventoryItem

import com.google.gson.annotations.SerializedName;

/**
 * Lớp này đại diện cho một chiếc xe cụ thể trong danh sách "vehicles" của InventoryItem.
 */
public class Vehicle {

    /**
     * Tên của kho hàng nơi chiếc xe này đang được lưu trữ.
     * Ví dụ: "kho cua hang 1"
     */
    @SerializedName("warehouseName")
    public String warehouseName;

    // Các trường khác như "vin", "warehouseId" có thể được thêm vào đây nếu cần
    // @SerializedName("vin")
    // public String vin;

}
