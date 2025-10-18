package com.example.evshop.util;

import com.example.evshop.domain.models.CartItem;
import com.example.evshop.domain.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
    }

    public void removeFromCart(String productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void updateQuantity(String productId, int newQuantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
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

    public long getTotalAmount() {
        long total = 0;
        for (CartItem item : cartItems) {
            total += item.totalPrice();
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

    public long getTotalPrice() {
        long total = 0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPriceVnd() * item.getQuantity();
        }
        return total;
    }

}