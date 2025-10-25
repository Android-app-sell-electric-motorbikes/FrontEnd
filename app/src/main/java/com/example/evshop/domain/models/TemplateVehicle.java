// Thay thế toàn bộ nội dung file TemplateVehicle.java bằng code này
package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Lớp chính, đại diện cho mỗi object trong mảng "result"
public class TemplateVehicle {

    @SerializedName("id")
    private String id;

    @SerializedName("version")
    private VersionInfo version; // Đổi tên class con để dễ phân biệt

    @SerializedName("color")
    private ColorInfo color;     // Đổi tên class con để dễ phân biệt

    @SerializedName("price")
    private long price;

    @SerializedName("imgUrl")
    private List<String> imgUrl;

    // --- Getters để truy cập dữ liệu ---
    public String getId() { return id; }
    public VersionInfo getVersion() { return version; }
    public ColorInfo getColor() { return color; }
    public long getPrice() { return price; }
    public List<String> getImgUrl() { return imgUrl; }


    // Lớp con cho đối tượng "version"
    public static class VersionInfo {
        @SerializedName("versionName")
        private String versionName;

        @SerializedName("modelName")
        private String modelName;

        // --- Getters ---
        public String getVersionName() { return versionName; }
        public String getModelName() { return modelName; }
    }


    // Lớp con cho đối tượng "color"
    public static class ColorInfo {
        @SerializedName("colorName")
        private String colorName;

        // --- Getters ---
        public String getColorName() { return colorName; }
    }
}
