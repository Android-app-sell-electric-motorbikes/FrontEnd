package com.example.evshop.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.ui.CartAdapter;
import com.example.evshop.util.CartManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerOrderItems;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private TextInputEditText etName, etPhone, etAddress;
    private RadioGroup radioGroupPayment;
    private MaterialButton btnPlaceOrder;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupRecyclerView();
        calculateTotal();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvTotal = findViewById(R.id.tvTotal);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
    }

    private void setupRecyclerView() {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        
        // Sử dụng CartAdapter với callback null vì không cần update total từ adapter
        adapter = new CartAdapter(cartItems, null);
        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrderItems.setAdapter(adapter);
    }

    private void calculateTotal() {
        double subtotal = CartManager.getInstance().getTotalPrice();
        double shipping = subtotal > 0 ? 30000 : 0; // Phí ship 30k
        double total = subtotal + shipping;

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        tvSubtotal.setText(formatter.format(subtotal));
        tvShipping.setText(formatter.format(shipping));
        tvTotal.setText(formatter.format(total));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        // Validate thông tin
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập tên");
            etName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Vui lòng nhập địa chỉ");
            etAddress.requestFocus();
            return;
        }

        // Lấy phương thức thanh toán
        int selectedPaymentId = radioGroupPayment.getCheckedRadioButtonId();
        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedPayment = findViewById(selectedPaymentId);
        String paymentMethod = selectedPayment.getText().toString();

        // Xử lý đặt hàng
        processOrder(name, phone, address, paymentMethod);
    }

    private void processOrder(String name, String phone, String address, String paymentMethod) {
        // TODO: Gọi API để tạo đơn hàng
        // Hiện tại chỉ giả lập thành công
        
        double total = CartManager.getInstance().getTotalPrice() + 30000;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        String message = "✅ Đặt hàng thành công!\n\n" +
                "Tên: " + name + "\n" +
                "SĐT: " + phone + "\n" +
                "Địa chỉ: " + address + "\n" +
                "Thanh toán: " + paymentMethod + "\n" +
                "Tổng tiền: " + formatter.format(total);

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Xóa giỏ hàng sau khi đặt hàng thành công
        CartManager.getInstance().clearCart();

        // Đóng màn hình và quay về
        finish();
    }
}

