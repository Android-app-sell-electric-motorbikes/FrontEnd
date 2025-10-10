package com.example.evshop.domain.models;

public class Store {
    public final String id;
    public final String name;
    public final String address;
    public final double lat;
    public final double lng;

    public Store(String id, String name, String address, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }
}