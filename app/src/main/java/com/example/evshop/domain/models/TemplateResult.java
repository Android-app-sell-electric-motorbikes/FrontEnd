// File: TemplateResult.java
package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp này đại diện cho toàn bộ đối tượng "result" trong API Get-all-template-vehicles.
 * Nó chứa danh sách các mẫu xe ("data") và thông tin phân trang ("pagination").
 */
public class TemplateResult {

    @SerializedName("data")
    private List<TemplateVehicle> data; // <-- Dùng model TemplateVehicle

    @SerializedName("pagination")
    private Pagination pagination; // <-- Tái sử dụng model Pagination

    // --- Getters ---

    public List<TemplateVehicle> getData() {
        return data;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
