package com.example.evshop.ui.vehicle;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.evshop.R;
// QUAN TRỌNG: Phải import cả 2 model
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.VersionDetails;
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
            // SỬA LẠI TÊN HÀM CHO ĐÚNG
            viewModel.loadVehicleTemplate(vehicleId);
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

    // HÀM QUAN TRỌNG NHẤT: LẮNG NGHE CẢ 2 LIVEDATA
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

        // 1. Lắng nghe dữ liệu Template (Ảnh, Giá, Màu)
        viewModel.vehicleTemplate.observe(this, template -> {
            if (template == null) return;

            // Cập nhật các thông tin chung từ `template`
            if (template.getImgUrl() != null && !template.getImgUrl().isEmpty()) {
                Glide.with(this)
                        .load(template.getImgUrl().get(0))
                        .placeholder(R.drawable.placeholder_vehicle)
                        .error(R.drawable.ic_placeholder)
                        .into(imgVehicleDetail);
            } else {
                imgVehicleDetail.setImageResource(R.drawable.placeholder_vehicle);
            }

            String colorName = (template.getColor() != null) ? template.getColor().getColorName() : "N/A";
            txtColorDetail.setText(colorName);
            txtPriceDetail.setText(Formatters.currency(template.getPrice()));
        });

        // 2. Lắng nghe dữ liệu VersionDetails (Thông số kỹ thuật)
        viewModel.versionDetails.observe(this, version -> {
            if (version == null) return;

            // Cập nhật các thông tin chi tiết từ `version`
            // `version.getModelName()` có thể null nếu API không trả về, nên cần kiểm tra
            String modelName = version.getVersionName() != null ? version.getVersionName() : "Chi tiết xe";
            toolbar.setTitle(modelName);
            txtVersionNameDetail.setText(version.getVersionName());
            txtVersionDescription.setText(version.getDescription());

            // Dùng hàm setSpec để gán các thông số kỹ thuật
            setSpec("Dòng xe", modelName, specModelName);
            setSpec("Công suất", version.getMotorPower() + " W", specMotorPower);
            setSpec("Quãng đường / 1 lần sạc", version.getRangePerCharge() + " km", specRange);
            setSpec("Tốc độ tối đa", version.getTopSpeed() + " km/h", specTopSpeed);
            setSpec("Năm sản xuất", String.valueOf(version.getProductionYear()), specYear);
            setSpec("Dung lượng pin", version.getBatteryCapacity() + " Ah", specBattery);
            setSpec("Cân nặng", version.getWeight() + " kg", specWeight);
            setSpec("Chiều cao", version.getHeight() + " mm", specHeight);

            // Các thông số còn lại có thể bạn chưa thêm vào model VersionDetails
            // Ví dụ: setSpec("Cân nặng", version.getWeight() + " kg", specWeight);
        });
    }

    /**
     * Hàm helper để gán label và value cho một dòng thông số
     */
    private void setSpec(String label, String value, View specView) {
        if (specView == null) return;
        TextView txtLabel = specView.findViewById(R.id.txtSpecLabel);
        TextView txtValue = specView.findViewById(R.id.txtSpecValue);
        txtLabel.setText(label);
        txtValue.setText(value);
    }
}
