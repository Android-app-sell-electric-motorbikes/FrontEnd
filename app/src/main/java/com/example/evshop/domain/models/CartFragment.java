package com.example.evshop.domain.models;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.evshop.databinding.FragmentCartBinding;
import com.example.evshop.ui.CartAdapter;
import com.example.evshop.util.CartManager;
import com.example.evshop.util.Formatters;

import java.util.List; // ** THÊM IMPORT CÒN THIẾU **

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private final CartManager cartManager = CartManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.btnClearCart.setOnClickListener(v -> {
            cartManager.clearCart();
            updateCartView(); // Cập nhật lại view sau khi xóa
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartView();
    }

    private void updateCartView() {
        if (binding == null) return;

        List<CartItem> cartItems = cartManager.getCartItems();
        
        CartAdapter adapter = new CartAdapter(cartItems, this::updateTotal);
        binding.recyclerCart.setAdapter(adapter);
        
        updateTotal();
    }

    private void updateTotal() {
        if (binding == null) return;
        double total = cartManager.getTotalPrice();
        binding.tvTotal.setText("Tổng cộng: " + Formatters.currency(total));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
}
