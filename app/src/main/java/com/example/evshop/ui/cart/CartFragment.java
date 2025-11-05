package com.example.evshop.ui.cart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.evshop.databinding.FragmentCartBinding;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.ui.payment.PaymentActivity;
import com.example.evshop.util.Formatters;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private final CartManager cartManager = CartManager.getInstance();
    private PaymentViewModel paymentViewModel;
    private CartAdapter cartAdapter;
    private final List<CartItem> currentCartItems = new ArrayList<>();

    private final ActivityResultLauncher<Intent> paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getContext(), "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                    cartManager.clearCart();
                    updateCartView();
                } else {
                    Toast.makeText(getContext(), "Thanh toán đã bị hủy hoặc thất bại.", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        setupRecyclerView();
        observeViewModel();

        binding.btnClearCart.setOnClickListener(v -> {
            cartManager.clearCart();
            updateCartView();
        });

        binding.btnVnpayCheckout.setOnClickListener(v -> {
            double totalPrice = cartManager.getTotalPrice();
            if (totalPrice == 0) {
                Toast.makeText(getContext(), "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
                return;
            }
            // ** SỬA LỖI: KHÔNG NHÂN VỚI 100 **
            // Gửi đi số tiền thực tế, backend sẽ tự xử lý.
            long amount = (long) totalPrice;
            paymentViewModel.createPayment(amount);
        });
    }

    private void observeViewModel() {
        paymentViewModel.paymentUrl.observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(getActivity(), PaymentActivity.class);
                intent.putExtra(PaymentActivity.EXTRA_URL, url);
                paymentLauncher.launch(intent);
            }
        });

        paymentViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartView();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(currentCartItems, this::updateTotal);
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerCart.setAdapter(cartAdapter);
    }

    private void updateCartView() {
        if (binding == null) return;
        
        List<CartItem> newCartItems = cartManager.getCartItems();
        currentCartItems.clear();
        currentCartItems.addAll(newCartItems);
        if (cartAdapter != null) {
             cartAdapter.notifyDataSetChanged();
        }
        
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
        binding = null;
    }
}
