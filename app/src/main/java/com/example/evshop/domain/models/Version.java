package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Version implements Serializable {

    @SerializedName("versionId")
    private String versionId;
    @SerializedName("versionName")
    private String versionName;
    @SerializedName("modelId")
    private String modelId;
    @SerializedName("modelName")
    private String modelName;
    @SerializedName("motorPower")
    private int motorPower;
    @SerializedName("batteryCapacity")
    private int batteryCapacity;
    @SerializedName("rangePerCharge")
    private int rangePerCharge;
    @SerializedName("topSpeed")
    private int topSpeed;
    @SerializedName("weight")
    private int weight;
    @SerializedName("height")
    private int height;
    @SerializedName("productionYear")
    private int productionYear;
    @SerializedName("description")
    private String description;

    // Getters
    public String getVersionId() { return versionId; }
    public String getVersionName() { return versionName; }
    public String getModelId() { return modelId; }
    public String getModelName() { return modelName; }
    public int getMotorPower() { return motorPower; }
    public int getBatteryCapacity() { return batteryCapacity; }
    public int getRangePerCharge() { return rangePerCharge; }
    public int getTopSpeed() { return topSpeed; }
    public int getWeight() { return weight; }
    public int getHeight() { return height; }
    public int getProductionYear() { return productionYear; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return versionName != null ? versionName : "Phiên bản không xác định";
    }
}
