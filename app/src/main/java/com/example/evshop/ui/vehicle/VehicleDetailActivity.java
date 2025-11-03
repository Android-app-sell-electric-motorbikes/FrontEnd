package com.example.evshop.ui.vehicle;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // SỬA: Import đúng lớp Log
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException; // THÊM IMPORT
import com.bumptech.glide.request.RequestListener;   // THÊM IMPORT
import com.bumptech.glide.request.target.Target;      // THÊM IMPORT
import com.example.evshop.R;
// Chỉ cần import model TemplateVehicle vì nó đã chứa tất cả thông tin
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.util.Formatters;
import com.google.android.material.appbar.MaterialToolbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VehicleDetailActivity extends AppCompatActivity {

    private VehicleDetailViewModel viewModel;

    // Khai báo tất cả các View
    private MaterialToolbar toolbar;
    private ImageView imgVehicleDetail;
    private TextView txtVersionNameDetail, txtColorDetail, txtPriceDetail, txtVersionDescription;
    private ProgressBar progressBar;
    private View scrollView;

    // Các View cho từng dòng thông số
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
            // SỬA: Gọi hàm mới trong ViewModel
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

        // Ánh xạ các layout include
        specMotorPower = findViewById(R.id.specMotorPower);
        specBattery = findViewById(R.id.specBattery);
        specRange = findViewById(R.id.specRange);
        specTopSpeed = findViewById(R.id.specTopSpeed);
        specWeight = findViewById(R.id.specWeight);
        specHeight = findViewById(R.id.specHeight);
        specYear = findViewById(R.id.specYear);
        specModelName = findViewById(R.id.specModelName);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // =================================================================
    // HÀM QUAN TRỌNG NHẤT: ĐÃ ĐƯỢC THÊM CODE DEBUG
    // =================================================================
    private void observeViewModel() {
        viewModel.loading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Ẩn nội dung khi đang tải để tránh hiển thị dữ liệu cũ
            scrollView.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        });

        viewModel.error.observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        // Chỉ cần MỘT observer duy nhất cho tất cả dữ liệu
        viewModel.vehicleDetails.observe(this, vehicle -> {
            // SỬA: Sửa lại logic if/else để log hoạt động đúng
            if (vehicle == null) {
                Log.d("VehicleDetailDebug", "Observer triggered, but vehicle is NULL");
                return;
            }

            // --- Cập nhật Ảnh, Giá, Màu (Từ lớp cha TemplateVehicle) ---
            if (vehicle.getImgUrl() != null && !vehicle.getImgUrl().isEmpty()) {
                String imageUrl = vehicle.getImgUrl().get(0);

                // DEBUG: In ra URL để kiểm tra
                Log.d("VehicleDetailDebug", "Image URL Found: " + imageUrl);

                // DEBUG: Thêm Listener để bắt lỗi của Glide
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_vehicle)
                        .error(R.drawable.ic_placeholder)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                // Lỗi đã xảy ra. In ra log để xem chi tiết.
                                Log.e("VehicleDetailDebug", "Glide Load Failed. URL: " + model, e);
                                return false; // Quan trọng: return false để ảnh .error() vẫn được hiển thị.
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                // Ảnh đã tải thành công.
                                Log.d("VehicleDetailDebug", "Glide Load Succeeded");
                                return false; // return false để Glide tiếp tục xử lý và hiển thị ảnh.
                            }
                        })
                        .into(imgVehicleDetail);
            } else {
                // DEBUG: In ra log khi không có ảnh
                Log.d("VehicleDetailDebug", "getImgUrl() is NULL or EMPTY");
                imgVehicleDetail.setImageResource(R.drawable.placeholder_vehicle);
            }

            if (vehicle.getColor() != null) {
                txtColorDetail.setText(vehicle.getColor().getColorName());
            } else {
                txtColorDetail.setText("N/A");
            }
            txtPriceDetail.setText(Formatters.currency(vehicle.getPrice()));

            // --- Cập nhật Thông số kỹ thuật (Từ lớp con Version) ---
            TemplateVehicle.Version version = vehicle.getVersion();
            if (version != null) {
                toolbar.setTitle(version.getVersionName());
                txtVersionNameDetail.setText(version.getVersionName());
                txtVersionDescription.setText(version.getDescription());

                // Dùng hàm setSpec để gán các thông số kỹ thuật
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

    /**
     * Hàm helper để gán label và value cho một dòng thông số
     */
    private void setSpec(String label, String value, View specView) {
        if (specView == null || value == null) return;
        TextView txtLabel = specView.findViewById(R.id.txtSpecLabel);
        TextView txtValue = specView.findViewById(R.id.txtSpecValue);
        txtLabel.setText(label);
        txtValue.setText(value);
    }
}
