// File: Version.java
package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

public class Version {
    // Thêm các trường khác của Version nếu có từ JSON (ví dụ: versionId)
    @SerializedName("versionId")
    private String versionId;

    @SerializedName("versionName")
    private String versionName;

    // --- Getters ---
    public String getVersionId() {
        return versionId;
    }

    public String getVersionName() {
        return versionName;
    }
}
