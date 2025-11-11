package com.example.evshop.domain.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class TemplateVehicle implements Serializable {

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

    private transient double rating;

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
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    // KHÔNG CÒN CÁC LỚP LỒNG BÊN TRONG

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
