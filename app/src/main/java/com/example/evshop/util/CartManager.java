package com.example.evshop.util;

import com.example.evshop.domain.models.CartItem;
import com.example.evshop.domain.models.TemplateVehicle;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(TemplateVehicle vehicle) {
        for (CartItem item : cartItems) {
            if (item.getVehicle().getId().equals(vehicle.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cartItems.add(new CartItem(vehicle, 1));
    }

    public void removeFromCart(String vehicleId) {
        cartItems.removeIf(item -> item.getVehicle().getId().equals(vehicleId));
    }

    public void updateQuantity(String vehicleId, int newQuantity) {
        for (CartItem item : cartItems) {
            if (item.getVehicle().getId().equals(vehicleId)) {
                item.setQuantity(newQuantity);
                break;
            }
        }
    }

    public void clearCart() {
        cartItems.clear();
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
}
