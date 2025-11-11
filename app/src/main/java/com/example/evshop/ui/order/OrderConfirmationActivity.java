package com.example.evshop.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.ui.main.MainActivity;
import com.example.evshop.util.Formatters;

import java.util.ArrayList;

public class OrderConfirmationActivity extends AppCompatActivity {

    public static final String EXTRA_CART_ITEMS = "EXTRA_CART_ITEMS";
    public static final String EXTRA_TOTAL_PRICE = "EXTRA_TOTAL_PRICE";
    public static final String EXTRA_FULL_NAME = "FULL_NAME";
    public static final String EXTRA_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String EXTRA_ADDRESS = "ADDRESS";

    private LinearLayout llSummaryItems;
    private TextView tvConfirmationTotal, tvShippingName, tvShippingAddress, tvShippingPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        llSummaryItems = findViewById(R.id.llSummaryItems);
        tvConfirmationTotal = findViewById(R.id.tvConfirmationTotal);
        tvShippingName = findViewById(R.id.tvShippingName);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        tvShippingPhone = findViewById(R.id.tvShippingPhone);

        Intent intent = getIntent();
        ArrayList<CartItem> cartItems = (ArrayList<CartItem>) intent.getSerializableExtra(EXTRA_CART_ITEMS);
        double totalPrice = intent.getDoubleExtra(EXTRA_TOTAL_PRICE, 0);
        String fullName = intent.getStringExtra(EXTRA_FULL_NAME);
        String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
        String address = intent.getStringExtra(EXTRA_ADDRESS);

        displayOrderSummary(cartItems, totalPrice);
        displayShippingInfo(fullName, phoneNumber, address);

        findViewById(R.id.btnBackToHome).setOnClickListener(v -> {
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
    }

    private void displayOrderSummary(ArrayList<CartItem> cartItems, double totalPrice) {
        if (cartItems == null) return;

        llSummaryItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CartItem item : cartItems) {
            View itemView = inflater.inflate(R.layout.item_cart_summary, llSummaryItems, false);
            TextView tvQuantity = itemView.findViewById(R.id.tvSummaryQuantity);
            TextView tvName = itemView.findViewById(R.id.tvSummaryName);
            TextView tvPrice = itemView.findViewById(R.id.tvSummaryPrice);

            tvQuantity.setText(item.getQuantity() + "x");
            tvName.setText(item.getVehicle().getVersion().getVersionName());
            tvPrice.setText(Formatters.currency(item.getTotalPrice()));

            llSummaryItems.addView(itemView);
        }

        tvConfirmationTotal.setText(Formatters.currency(totalPrice));
    }

    private void displayShippingInfo(String name, String phone, String address) {
        tvShippingName.setText(name);
        tvShippingPhone.setText(phone);
        tvShippingAddress.setText(address);
    }
}
