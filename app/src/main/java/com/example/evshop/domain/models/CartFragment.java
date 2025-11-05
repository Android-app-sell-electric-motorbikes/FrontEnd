package com.example.evshop.domain.models;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.ui.CartAdapter;
import com.example.evshop.ui.CheckoutActivity;
import com.example.evshop.util.CartManager;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvTotal;
    private MaterialButton btnClear, btnCheckout;
    private LinearLayout emptyCartLayout;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recyclerCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnClear = view.findViewById(R.id.btnClearCart);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        emptyCartLayout = view.findViewById(R.id.emptyCartLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(cartItems, this::updateTotal);
        recyclerView.setAdapter(adapter);

        updateTotal();

        btnClear.setOnClickListener(v -> {
            CartManager.getInstance().clearCart();
            refreshCartData();
        });

        btnCheckout.setOnClickListener(v -> {
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            } else {
                // Mở màn hình thanh toán
                Intent intent = new Intent(getContext(), CheckoutActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh dữ liệu mỗi khi người dùng quay lại fragment này
        refreshCartData();
    }

    private void refreshCartData() {
        if (recyclerView == null) return; // Đảm bảo view đã được khởi tạo
        
        List<CartItem> updatedCartItems = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(updatedCartItems, this::updateTotal);
        recyclerView.setAdapter(adapter);
        updateTotal();
        updateEmptyState();
    }

    private void updateTotal() {
        long total = CartManager.getInstance().getTotalPrice();
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotal.setText(formatter.format(total) + "₫");
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (emptyCartLayout == null) return;
        
        boolean isEmpty = CartManager.getInstance().getCartItems().isEmpty();
        emptyCartLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }


}
