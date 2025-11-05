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
import com.example.evshop.util.AppNotificationManager;
import com.example.evshop.util.Formatters;
import com.example.evshop.util.NotificationHelper; // **THÊM IMPORT**
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VehicleDetailActivity extends AppCompatActivity {

    private VehicleDetailViewModel viewModel;
    private TemplateVehicle currentVehicle;

    private MaterialToolbar toolbar;
    private ImageView imgVehicleDetail;
    private TextView txtVersionNameDetail, txtColorDetail, txtPriceDetail, txtVersionDescription;
    private ProgressBar progressBar;
    private View scrollView;
    private Button btnAddToCart;

    private View specMotorPower, specBattery, specRange, specTopSpeed, specWeight, specHeight, specYear, specModelName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        viewModel = new ViewModelProvider(this).get(VehicleDetailViewModel.class);
        initViews();
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
        imgVehicleDetail = findViewById(R.id.imgVehicleDetail);
        txtVersionNameDetail = findViewById(R.id.txtVersionNameDetail);
        txtColorDetail = findViewById(R.id.txtColorDetail);
        txtPriceDetail = findViewById(R.id.txtPriceDetail);
        progressBar = findViewById(R.id.progressBar);
        txtVersionDescription = findViewById(R.id.txtVersionDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        specMotorPower = findViewById(R.id.specMotorPower);
        specBattery = findViewById(R.id.specBattery);
        specRange = findViewById(R.id.specRange);
        specTopSpeed = findViewById(R.id.specTopSpeed);
        specWeight = findViewById(R.id.specWeight);
        specHeight = findViewById(R.id.specHeight);
        specYear = findViewById(R.id.specYear);
        specModelName = findViewById(R.id.specModelName);

        toolbar.setNavigationOnClickListener(v -> finish());

        btnAddToCart.setOnClickListener(v -> {
            if (currentVehicle != null) {
                // 1. Thêm vào giỏ hàng
                CartManager.getInstance().addToCart(currentVehicle);

                // 2. Hiển thị Snackbar
                Snackbar.make(findViewById(android.R.id.content), "Đã thêm vào giỏ hàng", Snackbar.LENGTH_LONG).show();

                // 3. **HIỂN THỊ THÔNG BÁO HỆ THỐNG**
                NotificationHelper notificationHelper = new NotificationHelper(this);
                int cartItemCount = CartManager.getInstance().getTotalItemCount();
                notificationHelper.updateAppBadge(cartItemCount);

            } else {
                Snackbar.make(findViewById(android.R.id.content), "Không thể thêm vào giỏ hàng, vui lòng thử lại.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.loading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            scrollView.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
            btnAddToCart.setVisibility(isLoading ? View.GONE : View.VISIBLE);
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
            } else {
                txtColorDetail.setText("N/A");
            }
            txtPriceDetail.setText(Formatters.currency(vehicle.getPrice()));

            TemplateVehicle.Version version = vehicle.getVersion();
            if (version != null) {
                toolbar.setTitle(version.getVersionName());
                txtVersionNameDetail.setText(version.getVersionName());
                txtVersionDescription.setText(version.getDescription());

                setSpec("Dòng xe", version.getModelName(), specModelName);
                setSpec("Công suất", version.getMotorPower() + " W", specMotorPower);
                setSpec("Quãng đường / 1 lần sạc", version.getRangePerCharge() + " km", specRange);
                setSpec("Tốc độ tối đa", version.getTopSpeed() + " km/h", specTopSpeed);
                setSpec("Năm sản xuất", String.valueOf(version.getProductionYear()), specYear);
                setSpec("Dung lượng pin", version.getBatteryCapacity() + " Ah", specBattery);
                setSpec("Cân nặng", version.getWeight() + " kg", specWeight);
                setSpec("Chiều cao", version.getHeight() + " mm", specHeight);
            }
        });
    }

    private void setSpec(String label, String value, View specView) {
        if (specView == null || value == null) return;
        TextView txtLabel = specView.findViewById(R.id.txtSpecLabel);
        TextView txtValue = specView.findViewById(R.id.txtSpecValue);
        txtLabel.setText(label);
        txtValue.setText(value);
    }
}
