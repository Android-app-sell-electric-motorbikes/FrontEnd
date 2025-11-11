package com.example.evshop.ui.order;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.evshop.R;
import com.example.evshop.domain.models.CartItem;
import com.example.evshop.ui.cart.PaymentViewModel;
import com.example.evshop.ui.payment.PaymentActivity;
import com.example.evshop.util.Formatters;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BillingDetailsActivity extends AppCompatActivity {

    private PaymentViewModel paymentViewModel;
    private ArrayList<CartItem> cartItems;
    private double totalPrice;

    private TextInputEditText etFullName, etPhoneNumber, etAddress;
    private RadioGroup rgPaymentMethod;

    private final ActivityResultLauncher<Intent> paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Chuyển tiếp kết quả về cho CartActivity
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = new Intent();
                    // Truyền thông tin giao hàng về màn hình xác nhận
                    data.putExtra("FULL_NAME", etFullName.getText().toString());
                    data.putExtra("PHONE_NUMBER", etPhoneNumber.getText().toString());
                    data.putExtra("ADDRESS", etAddress.getText().toString());
                    setResult(Activity.RESULT_OK, data);
                } else {
                    setResult(Activity.RESULT_CANCELED);
                }
                finish();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_details);

        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);

        cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("CART_ITEMS");
        totalPrice = getIntent().getDoubleExtra("TOTAL_PRICE", 0);

        initViews();
        observeViewModel();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAddress = findViewById(R.id.etAddress);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        Button btnContinueToPayment = findViewById(R.id.btnContinueToPayment);

        tvTotalAmount.setText("Tổng cộng: " + Formatters.currency(totalPrice));

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        btnContinueToPayment.setOnClickListener(v -> {
            if (validateInput()) {
                paymentViewModel.createPayment((long) totalPrice);
            }
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etFullName.getText()) || TextUtils.isEmpty(etPhoneNumber.getText()) || TextUtils.isEmpty(etAddress.getText())) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (rgPaymentMethod.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void observeViewModel() {
        paymentViewModel.paymentUrl.observe(this, url -> {
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra(PaymentActivity.EXTRA_URL, url);
                paymentLauncher.launch(intent);
            }
        });

        paymentViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
