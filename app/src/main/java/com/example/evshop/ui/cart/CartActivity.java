package com.example.evshop.ui.cart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.ui.order.BillingDetailsActivity;
import com.example.evshop.ui.order.OrderConfirmationActivity;
import com.example.evshop.util.Formatters;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private CartAdapter cartAdapter;
    private final CartManager cartManager = CartManager.getInstance();

    private final ActivityResultLauncher<Intent> billingLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = new Intent(this, OrderConfirmationActivity.class);
                    intent.putExtra(OrderConfirmationActivity.EXTRA_CART_ITEMS, new ArrayList<>(cartManager.getCartItems()));
                    intent.putExtra(OrderConfirmationActivity.EXTRA_TOTAL_PRICE, cartManager.getTotalPrice());
                    
                    // ** LẤY VÀ TRUYỀN TIẾP THÔNG TIN GIAO HÀNG **
                    if (result.getData() != null) {
                        intent.putExtras(result.getData());
                    }
                    
                    startActivity(intent);

                    cartManager.clearCart();
                    finish();
                } else {
                    Toast.makeText(this, "Thanh toán đã bị hủy hoặc thất bại.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        Button btnCheckout = findViewById(R.id.btnCheckout);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getTotalItemCount() == 0) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, BillingDetailsActivity.class);
            intent.putExtra("CART_ITEMS", new ArrayList<>(cartManager.getCartItems()));
            intent.putExtra("TOTAL_PRICE", cartManager.getTotalPrice());
            billingLauncher.launch(intent);
        });

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartView();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartManager.getCartItems(), this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void updateCartView() {
        if (cartAdapter != null) {
            cartAdapter.updateCartItems(cartManager.getCartItems());
        }
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        tvTotalPrice.setText("Tổng tiền: " + Formatters.currency(cartManager.getTotalPrice()));
    }

    @Override
    public void onIncreaseQuantity(CartItem item) {
        cartManager.addToCart(item.getVehicle());
        updateCartView();
    }

    @Override
    public void onDecreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            int newQuantity = item.getQuantity() - 1;
            cartManager.updateQuantity(item.getVehicle().getId(), newQuantity);
            updateCartView();
        }
    }

    @Override
    public void onRemoveItem(CartItem item) {
        cartManager.removeFromCart(item.getVehicle().getId());
        updateCartView();
    }
}
