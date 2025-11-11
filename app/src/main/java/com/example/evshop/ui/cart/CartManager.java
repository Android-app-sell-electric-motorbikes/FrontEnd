package com.example.evshop.ui.cart;

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

    // ** SỬA LẠI: Thêm số lượng sản phẩm **
    public void addToCart(TemplateVehicle vehicle, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getVehicle().getId().equals(vehicle.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        cartItems.add(new CartItem(vehicle, quantity));
    }

    // Giữ lại hàm cũ để tương thích với các nơi khác
    public void addToCart(TemplateVehicle vehicle) {
        addToCart(vehicle, 1);
    }

    public void removeFromCart(String vehicleId) {
        cartItems.removeIf(item -> item.getVehicle().getId().equals(vehicleId));
    }

    public void updateQuantity(String vehicleId, int newQuantity) {
        for (CartItem item : cartItems) {
            if (item.getVehicle().getId().equals(vehicleId)) {
                if (newQuantity > 0) {
                    item.setQuantity(newQuantity);
                }
                return;
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
