package com.example.evshop.ui.cart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.ui.order.OrderConfirmationActivity;
import com.example.evshop.ui.payment.PaymentActivity;
import com.example.evshop.util.Formatters;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private CartAdapter cartAdapter;
    private LinearLayout emptyView;
    private View bottomBar;
    private final CartManager cartManager = CartManager.getInstance();
    private PaymentViewModel paymentViewModel; // **1. THÊM LẠI VIEWMODEL**

    // **2. LAUNCHER ĐỂ MỞ VNPAY WEBVIEW**
    private final ActivityResultLauncher<Intent> paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Thanh toán thành công, mở màn hình xác nhận
                    Intent intent = new Intent(this, OrderConfirmationActivity.class);
                    intent.putExtra(OrderConfirmationActivity.EXTRA_CART_ITEMS, new ArrayList<>(cartManager.getCartItems()));
                    intent.putExtra(OrderConfirmationActivity.EXTRA_TOTAL_PRICE, cartManager.getTotalPrice());
                    startActivity(intent);

                    cartManager.clearCart();
                    finish(); // Đóng màn hình giỏ hàng
                } else {
                    Toast.makeText(this, "Thanh toán đã bị hủy hoặc thất bại.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Khởi tạo ViewModel
        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        Button btnCheckout = findViewById(R.id.btnCheckout);
        Button btnClearAll = findViewById(R.id.btnClearAll);
        emptyView = findViewById(R.id.empty_view);
        bottomBar = findViewById(R.id.bottom_bar);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        // **3. SỬA LẠI ONCLICK: GỌI TRỰC TIẾP VIEWMODEL**
        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getTotalItemCount() == 0) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
                return;
            }
            long amount = (long) cartManager.getTotalPrice();
            paymentViewModel.createPayment(amount);
        });

        btnClearAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa giỏ hàng")
                    .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        cartManager.clearCart();
                        updateCartView();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        setupRecyclerView();
        observeViewModel(); // **4. LẮNG NGHE KẾT QUẢ TỪ VIEWMODEL**
    }

    private void observeViewModel() {
        paymentViewModel.paymentUrl.observe(this, url -> {
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra(PaymentActivity.EXTRA_URL, url);
                paymentLauncher.launch(intent);
                paymentViewModel.onPaymentUrlHandled(); // Báo cho ViewModel đã xử lý xong
            }
        });

        paymentViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartView();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(new ArrayList<>(cartManager.getCartItems()), this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void updateCartView() {
        boolean isEmpty = cartManager.getCartItems().isEmpty();
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        bottomBar.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        rvCartItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        
        if (!isEmpty) {
            if (cartAdapter != null) {
                cartAdapter.updateCartItems(cartManager.getCartItems());
            }
            updateTotalPrice();
        }
    }

    private void updateTotalPrice() {
        tvTotalPrice.setText(Formatters.currency(cartManager.getTotalPrice()));
    }

    @Override
    public void onIncreaseQuantity(CartItem item) {
        cartManager.addToCart(item.getVehicle());
        updateCartView();
    }

    @Override
    public void onDecreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            cartManager.updateQuantity(item.getVehicle().getId(), item.getQuantity() - 1);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa sản phẩm")
                    .setMessage("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?")
                    .setPositiveButton("Xóa", (dialog, which) -> onRemoveItem(item))
                    .setNegativeButton("Hủy", null)
                    .show();
        }
        updateCartView();
    }

    @Override
    public void onRemoveItem(CartItem item) {
        cartManager.removeFromCart(item.getVehicle().getId());
        updateCartView();
    }
}
