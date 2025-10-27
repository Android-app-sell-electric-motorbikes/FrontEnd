// File: VersionDetails.java
// Đường dẫn: app/src/main/java/com/example/evshop/domain/models/VersionDetails.java
package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;

// Lớp này CHỈ DÙNG để chứa kết quả từ API get-version-by-id
public class VersionDetails {

    @SerializedName("id") // <--- API này dùng "id"
    private String id;

    @SerializedName("versionName")
    private String versionName;

    @SerializedName("motorPower")
    private int motorPower;

    @SerializedName("rangePerCharge")
    private int rangePerCharge;

    @SerializedName("topSpeed")
    private int topSpeed;

    @SerializedName("productionYear")
    private int productionYear;

    @SerializedName("description")
    private String description;
    @SerializedName("batteryCapacity")
    private int batteryCapacity;

    @SerializedName("weight")
    private int weight;

    @SerializedName("height")
    private int height;

    // --- Getters ---
    public String getId() { return id; }
    public String getVersionName() { return versionName; }
    public int getMotorPower() { return motorPower; }
    public int getRangePerCharge() { return rangePerCharge; }
    public int getTopSpeed() { return topSpeed; }
    public int getProductionYear() { return productionYear; }
    public String getDescription() { return description; }
    public int getBatteryCapacity() { return batteryCapacity; }
    public int getWeight() { return weight; }

    public int getHeight() { return height; }
}
