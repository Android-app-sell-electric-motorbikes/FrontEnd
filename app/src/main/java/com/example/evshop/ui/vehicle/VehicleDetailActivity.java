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
    private TemplateVehicle currentVehicle; // Giữ lại để thêm vào giỏ hàng

    private MaterialToolbar toolbar;
    private ImageView imgVehicleDetail;
    private TextView txtVersionNameDetail, txtColorDetail, txtPriceDetail, txtVersionDescription;
    private ProgressBar progressBar;
    private View scrollView;
    private Button btnAddToCart;

    // Các View cho thông số kỹ thuật
    private View specMotorPower, specBattery, specRange, specTopSpeed, specWeight, specHeight, specYear, specModelName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        viewModel = new ViewModelProvider(this).get(VehicleDetailViewModel.class);
        initViews();
        observeViewModel(); // << Sửa logic trong hàm này

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
        imgVehicleDetail = findViewById(R.id.imgVehicleDetail);
        txtVersionNameDetail = findViewById(R.id.txtVersionNameDetail);
        txtColorDetail = findViewById(R.id.txtColorDetail);
        txtPriceDetail = findViewById(R.id.txtPriceDetail);
        progressBar = findViewById(R.id.progressBar);
        txtVersionDescription = findViewById(R.id.txtVersionDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // Lấy view cho từng thông số
        specMotorPower = findViewById(R.id.specMotorPower);
        specBattery = findViewById(R.id.specBattery);
        specRange = findViewById(R.id.specRange);
        specTopSpeed = findViewById(R.id.specTopSpeed);
        specWeight = findViewById(R.id.specWeight);
        specHeight = findViewById(R.id.specHeight);
        specYear = findViewById(R.id.specYear);
        specModelName = findViewById(R.id.specModelName);

        toolbar.setNavigationOnClickListener(v -> finish());
        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    // ========================================================
    // ***           SỬA LỖI LOGIC Ở ĐÂY                    ***
    // ========================================================
    private void observeViewModel() {
        // Lắng nghe trạng thái loading
        viewModel.loading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Chỉ hiện nội dung khi đã load xong
            scrollView.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
            btnAddToCart.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });

        // Lắng nghe lỗi
        viewModel.error.observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        // 1. LẮNG NGHE THÔNG TIN CHUNG (Tên, Giá, Màu, Ảnh...)
        viewModel.getTemplateVehicle().observe(this, template -> {
            if (template == null) return;

            // Lưu lại xe hiện tại để dùng cho chức năng "Thêm vào giỏ"
            this.currentVehicle = template;

            // Hiển thị ảnh
            if (template.getImgUrl() != null && !template.getImgUrl().isEmpty()) {
                Glide.with(this).load(template.getImgUrl().get(0)).placeholder(R.drawable.placeholder_vehicle).into(imgVehicleDetail);
            } else {
                imgVehicleDetail.setImageResource(R.drawable.placeholder_vehicle);
            }

            // Hiển thị thông tin chung
            if (template.getVersion() != null) {
                toolbar.setTitle(template.getVersion().getVersionName());
                txtVersionNameDetail.setText(template.getVersion().getVersionName());
                // Hiển thị dòng xe (modelName) từ template
                setSpec("Dòng xe", template.getVersion().getModelName(), specModelName);
            }
            if (template.getColor() != null) {
                txtColorDetail.setText(template.getColor().getColorName());
            } else {
                txtColorDetail.setText("N/A");
            }
            txtPriceDetail.setText(Formatters.currency(template.getPrice()));
            txtVersionDescription.setText(template.getDescription()); // Mô tả chung từ template
        });

        // 2. LẮNG NGHE THÔNG SỐ KỸ THUẬT CHI TIẾT
        viewModel.getVersionDetails().observe(this, details -> {
            if (details == null) return;

            // Hiển thị các thông số kỹ thuật từ `details`
            setSpec("Công suất", details.getMotorPower() + " W", specMotorPower);
            setSpec("Dung lượng pin", details.getBatteryCapacity() + " Ah", specBattery);
            setSpec("Quãng đường / 1 lần sạc", details.getRangePerCharge() + " km", specRange);
            setSpec("Tốc độ tối đa", details.getTopSpeed() + " km/h", specTopSpeed);
            setSpec("Năm sản xuất", String.valueOf(details.getProductionYear()), specYear);
            setSpec("Cân nặng", details.getWeight() + " kg", specWeight);
            setSpec("Chiều cao", details.getHeight() + " mm", specHeight);
        });
    }

    private void setSpec(String label, String value, View specView) {
        if (specView == null || value == null) return;
        TextView txtLabel = specView.findViewById(R.id.txtSpecLabel);
        TextView txtValue = specView.findViewById(R.id.txtSpecValue);
        if (txtLabel != null && txtValue != null) {
            txtLabel.setText(label);
            txtValue.setText(value);
        }
    }

    private void addToCart() {
        if (currentVehicle != null) {
            CartManager.getInstance().addToCart(currentVehicle);
            Snackbar.make(findViewById(android.R.id.content), "Đã thêm vào giỏ hàng", Snackbar.LENGTH_LONG).show();
            NotificationHelper notificationHelper = new NotificationHelper(this);
            int cartItemCount = CartManager.getInstance().getTotalItemCount();
            notificationHelper.updateAppBadge(cartItemCount);
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Không thể thêm vào giỏ hàng, vui lòng thử lại.", Snackbar.LENGTH_LONG).show();
        }
    }
}
