package com.example.evshop.ui.cart;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.util.Formatters;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private CartAdapter cartAdapter;
    private final CartManager cartManager = CartManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        setupRecyclerView();
        updateTotalPrice();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartManager.getCartItems(), this::updateTotalPrice);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void updateTotalPrice() {
        tvTotalPrice.setText("Tổng tiền: " + Formatters.currency(cartManager.getTotalPrice()));
    }
}
