package com.example.evshop.domain.models;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem() {
    }
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;

}

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public long totalPrice() {
        return product.priceVnd * quantity;
    }
}
