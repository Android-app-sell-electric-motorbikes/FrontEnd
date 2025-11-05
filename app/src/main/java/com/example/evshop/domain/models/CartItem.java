package com.example.evshop.domain.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private final TemplateVehicle vehicle;
    private int quantity;

    public CartItem(TemplateVehicle vehicle, int quantity) {
        this.vehicle = vehicle;
        this.quantity = quantity;
    }

    public TemplateVehicle getVehicle() {
        return vehicle;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return vehicle.getPrice() * quantity;
    }
}
