package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // **THÊM IMPORT**
import java.util.List;
import java.util.Objects;

public class TemplateVehicle implements Serializable { // **IMPLEMENTS Serializable**

    @SerializedName("id")
    private String id;

    @SerializedName("price")
    private long price;

    @SerializedName("imgUrl")
    private List<String> imgUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("version")
    private Version version;

    @SerializedName("color")
    private Color color;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public List<String> getImgUrl() { return imgUrl; }
    public void setImgUrl(List<String> imgUrl) { this.imgUrl = imgUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Version getVersion() { return version; }
    public void setVersion(Version version) { this.version = version; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    // Lớp con Version
    public static class Version implements Serializable { // **IMPLEMENTS Serializable**
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

        // Getters and Setters for Version
        public String getVersionId() { return versionId; }
        public void setVersionId(String versionId) { this.versionId = versionId; }
        public String getVersionName() { return versionName; }
        public void setVersionName(String versionName) { this.versionName = versionName; }
        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        public int getMotorPower() { return motorPower; }
        public void setMotorPower(int motorPower) { this.motorPower = motorPower; }
        public int getBatteryCapacity() { return batteryCapacity; }
        public void setBatteryCapacity(int batteryCapacity) { this.batteryCapacity = batteryCapacity; }
        public int getRangePerCharge() { return rangePerCharge; }
        public void setRangePerCharge(int rangePerCharge) { this.rangePerCharge = rangePerCharge; }
        public int getTopSpeed() { return topSpeed; }
        public void setTopSpeed(int topSpeed) { this.topSpeed = topSpeed; }
        public int getWeight() { return weight; }
        public void setWeight(int weight) { this.weight = weight; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public int getProductionYear() { return productionYear; }
        public void setProductionYear(int productionYear) { this.productionYear = productionYear; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // Lớp con Color
    public static class Color implements Serializable { // **IMPLEMENTS Serializable**
        @SerializedName("colorId")
        private String colorId;
        @SerializedName("colorName")
        private String colorName;

        // Getters and Setters for Color
        public String getColorId() { return colorId; }
        public void setColorId(String colorId) { this.colorId = colorId; }
        public String getColorName() { return colorName; }
        public void setColorName(String colorName) { this.colorName = colorName; }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateVehicle that = (TemplateVehicle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
