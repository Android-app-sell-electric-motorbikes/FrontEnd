package com.example.evshop.ui.vehicle;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.evshop.R;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.ui.cart.CartManager;
import com.example.evshop.util.Formatters;
import com.example.evshop.util.NotificationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VehicleDetailActivity extends AppCompatActivity {

    private VehicleDetailViewModel viewModel;
    private TemplateVehicle currentVehicle;
    private int quantity = 1; // ** BIẾN LƯU SỐ LƯỢNG **

    private MaterialToolbar toolbar;
    private ImageView imgVehicleDetail;
    private TextView txtVersionNameDetail, txtColorDetail, txtPriceDetail, txtVersionDescription, tvQuantity;
    private ProgressBar progressBar;
    private View scrollView, bottomControls;
    private Button btnAddToCart, btnMinus, btnPlus;

    private View specMotorPower, specBattery, specRange, specTopSpeed, specWeight, specHeight, specYear, specModelName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        viewModel = new ViewModelProvider(this).get(VehicleDetailViewModel.class);
        initViews();
        setupClickListeners();
        observeViewModel();

        String vehicleId = getIntent().getStringExtra("VEHICLE_ID");
        if (!TextUtils.isEmpty(vehicleId)) {
            viewModel.loadVehicleDetails(vehicleId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin xe.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        scrollView = findViewById(R.id.scrollView);
        bottomControls = findViewById(R.id.bottom_controls);
        imgVehicleDetail = findViewById(R.id.imgVehicleDetail);
        txtVersionNameDetail = findViewById(R.id.txtVersionNameDetail);
        txtColorDetail = findViewById(R.id.txtColorDetail);
        txtPriceDetail = findViewById(R.id.txtPriceDetail);
        progressBar = findViewById(R.id.progressBar);
        txtVersionDescription = findViewById(R.id.txtVersionDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        tvQuantity = findViewById(R.id.tv_quantity);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);

        specMotorPower = findViewById(R.id.specMotorPower);
        specBattery = findViewById(R.id.specBattery);
        specRange = findViewById(R.id.specRange);
        specTopSpeed = findViewById(R.id.specTopSpeed);
        specWeight = findViewById(R.id.specWeight);
        specHeight = findViewById(R.id.specHeight);
        specYear = findViewById(R.id.specYear);
        specModelName = findViewById(R.id.specModelName);
    }

    private void setupClickListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        btnAddToCart.setOnClickListener(v -> addToCart());

        // ** SỰ KIỆN TĂNG/GIẢM SỐ LƯỢNG **
        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });
    }

    private void observeViewModel() {
        viewModel.loading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            scrollView.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
            bottomControls.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });

        viewModel.error.observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.vehicleDetails.observe(this, vehicle -> {
            if (vehicle == null) return;
            this.currentVehicle = vehicle;

            if (vehicle.getImgUrl() != null && !vehicle.getImgUrl().isEmpty()) {
                Glide.with(this).load(vehicle.getImgUrl().get(0)).placeholder(R.drawable.placeholder_vehicle).into(imgVehicleDetail);
            } else {
                imgVehicleDetail.setImageResource(R.drawable.placeholder_vehicle);
            }

            if (vehicle.getColor() != null) {
                txtColorDetail.setText(vehicle.getColor().getColorName());
            }

            txtPriceDetail.setText(Formatters.currency(vehicle.getPrice()));
            txtVersionDescription.setText(vehicle.getDescription());

            if (vehicle.getVersion() != null) {
                toolbar.setTitle(vehicle.getVersion().getVersionName());
                txtVersionNameDetail.setText(vehicle.getVersion().getVersionName());
                // Các thông số kỹ thuật khác...
            }
        });
    }

    private void setSpec(String label, String value, View specView) {
        // ... (Hàm này giữ nguyên)
    }

    private void addToCart() {
        if (currentVehicle != null) {
            // ** THÊM ĐÚNG SỐ LƯỢNG ĐÃ CHỌN **
            CartManager.getInstance().addToCart(currentVehicle, quantity);
            Snackbar.make(findViewById(android.R.id.content), "Đã thêm " + quantity + " sản phẩm vào giỏ hàng", Snackbar.LENGTH_LONG).show();
            NotificationHelper notificationHelper = new NotificationHelper(this);
            int cartItemCount = CartManager.getInstance().getTotalItemCount();
            notificationHelper.updateAppBadge(cartItemCount);
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Không thể thêm vào giỏ hàng, vui lòng thử lại.", Snackbar.LENGTH_LONG).show();
        }
    }
}
