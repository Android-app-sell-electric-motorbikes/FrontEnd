// File: Version.java
package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

public class Version {
    // Thêm các trường khác của Version nếu có từ JSON (ví dụ: versionId)
    @SerializedName("id")
    private String id;

    @SerializedName("versionName")
    private String versionName;

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getVersionName() {
        return versionName;
    }

    @Override
    public String toString() {
        return versionName; // Trả về tên để hiển thị
    }
}
