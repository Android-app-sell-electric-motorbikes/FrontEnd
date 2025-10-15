package com.example.evshop.domain.models;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.util.CartManager;

import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvTotal;
    private Button btnClear;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recyclerCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnClear = view.findViewById(R.id.btnClearCart);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        adapter = new CartAdapter(cartItems, this::updateTotal);
        recyclerView.setAdapter(adapter);

        updateTotal();

        btnClear.setOnClickListener(v -> {
            CartManager.getInstance().clearCart();
            adapter.notifyDataSetChanged();
            updateTotal();
        });

        return view;
    }

    private void updateTotal() {
        long total = CartManager.getInstance().getTotalPrice();
        tvTotal.setText("Tổng cộng: " + total + "₫");
    }
}
