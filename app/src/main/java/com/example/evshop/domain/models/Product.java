package com.example.evshop.domain.models;
public class Product {

        private String id;
        private String name;
        private String brand;
        private int imageUrl;
        private long priceVnd;
        private float rating;
        private String category;

        public Product() {
        }

        public Product(String id, String name, String brand, int imageUrl, long priceVnd, float rating, String category) {
            this.id = id;
            this.name = name;
            this.brand = brand;
            this.imageUrl = imageUrl;
            this.priceVnd = priceVnd;
            this.rating = rating;
            this.category = category;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public int getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(int imageUrl) {
            this.imageUrl = imageUrl;
        }

        public long getPriceVnd() {
            return priceVnd;
        }

        public void setPriceVnd(long priceVnd) {
            this.priceVnd = priceVnd;
        }

        public float getRating() {
            return rating;
        }

        public void setRating(float rating) {
            this.rating = rating;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getFormattedPrice() {
            return String.format("%,d₫", priceVnd);
        }
    }
