// File: Version.java
package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

// Lớp này DÙNG CHO API DANH SÁCH (Get-all-template-vehicles)
public class Version {

    // *** THAY ĐỔI QUAN TRỌNG NHẤT: PHẢI KHỚP VỚI JSON CỦA API DANH SÁCH ***
    @SerializedName("versionId") // <-- API DANH SÁCH DÙNG "versionId"
    private String id; // <-- Ta vẫn dùng tên biến là "id" cho tiện, nhưng báo cho Gson biết nó tên là "versionId"

    @SerializedName("versionName")
    private String versionName;

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getVersionName() {
        return versionName;
    }

    // Không cần các trường chi tiết khác ở đây vì API danh sách không cung cấp chúng.
    // Giữ cho model này thật gọn nhẹ.
}
