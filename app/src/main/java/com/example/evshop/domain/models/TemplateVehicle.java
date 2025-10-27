// File: TemplateVehicle.java
// Đường dẫn: app/src/main/java/com/example/evshop/domain/models/TemplateVehicle.java

package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TemplateVehicle {

    // ... các trường khác như id, color, price...
    @SerializedName("id")
    private String id;

    // ========================================================
    // *** THAY ĐỔI QUAN TRỌNG NHẤT TẠI ĐÂY ***
    // Thay thế VersionInfo bằng Version để có đầy đủ thông tin, bao gồm cả ID
    @SerializedName("version")
    private Version version; // <--- SỬA TỪ VersionInfo THÀNH Version

    @SerializedName("color")
    private Color color;

    @SerializedName("price")
    private double price;

    @SerializedName("imgUrl")
    private List<String> imgUrl;
    // ========================================================


    // --- Getters ---
    public String getId() {
        return id;
    }

    // Sửa cả phương thức getter tương ứng
    public Version getVersion() { // <--- SỬA TỪ VersionInfo THÀNH Version
        return version;
    }

    public Color getColor() {
        return color;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getImgUrl() {
        return imgUrl;
    }

    // Bạn có thể không cần lớp Color và VersionInfo nữa nếu không dùng ở đâu khác
    // Tuy nhiên, cứ để lại chúng để tránh phát sinh lỗi ở những chỗ khác nếu có.

    // Lớp nội bộ Color (nếu bạn có)
    public static class Color {
        @SerializedName("colorName")
        private String colorName;

        public String getColorName() {
            return colorName;
        }
    }

    // Lớp VersionInfo có thể không còn cần thiết
    // public static class VersionInfo {
    //    @SerializedName("versionName")
    //    private String versionName;
    //
    //    public String getVersionName() {
    //        return versionName;
    //    }
    // }
}

