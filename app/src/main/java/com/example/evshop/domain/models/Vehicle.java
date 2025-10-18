package com.example.evshop.domain.models;

import java.util.List;

public class Vehicle {
    public String id;
    public String vin;
    public String status;
    public double costPrice;
    public List<String> imgUrl;
    public Version version;
    public Color color;

    public static class Version {
        public String versionId;
        public String versionName;
        public String modelId;
        public String modelName;
    }

    public static class Color {
        public String colorId;
        public String colorName;
    }
}
