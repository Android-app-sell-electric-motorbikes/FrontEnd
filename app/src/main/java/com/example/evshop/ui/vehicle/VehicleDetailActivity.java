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
// *** BƯỚC 1: THÊM IMPORT NÀY ***
import com.example.evshop.domain.models.VersionDetails;
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
    private int quantity = 1;

    private MaterialToolbar toolbar;
    private ImageView imgVehicleDetail;
    private TextView txtVersionNameDetail, txtColorDetail, txtPriceDetail, txtVersionDescription, tvQuantity;
    private ProgressBar progressBar;
    private View scrollView, bottomControls;
    private Button btnAddToCart, btnMinus, btnPlus;

    // Các View cho thông số kỹ thuật
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

    // *** BƯỚC 2: SỬA LẠI HOÀN TOÀN HÀM NÀY ***
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

        // Lắng nghe thông tin chung của xe
        viewModel.vehicleDetails.observe(this, vehicle -> {
            if (vehicle == null) return;
            this.currentVehicle = vehicle;

            // Hiển thị thông tin chung
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
            }
        });

        // *** THÊM MỚI: LẮNG NGHE THÔNG SỐ KỸ THUẬT TỪ API THỨ HAI ***
        viewModel.versionSpecs.observe(this, specs -> {
            View specsLayout = findViewById(R.id.specsLayout);
            if (specs == null) {
                // Nếu không có thông số, ẩn cả khu vực đi
                specsLayout.setVisibility(View.GONE);
                return;
            }
            specsLayout.setVisibility(View.VISIBLE);

            // Hiển thị thông số kỹ thuật lên giao diện
            setSpec("Công suất động cơ", specs.getMotorPower() + " W", specMotorPower);
            setSpec("Dung lượng pin", specs.getBatteryCapacity() + " Ah", specBattery);
            setSpec("Quãng đường tối đa", specs.getRangePerCharge() + " km", specRange);
            setSpec("Tốc độ tối đa", specs.getTopSpeed() + " km/h", specTopSpeed);
            setSpec("Trọng lượng", specs.getWeight() + " kg", specWeight);
            setSpec("Chiều cao yên", specs.getHeight() + " mm", specHeight);
            setSpec("Năm sản xuất", String.valueOf(specs.getProductionYear()), specYear);

            // Lấy modelName từ vehicleDetails vì VersionDetails không có
            if (currentVehicle != null && currentVehicle.getVersion() != null) {
                setSpec("Tên model", currentVehicle.getVersion().getModelName(), specModelName);
            }
        });
    }

    // *** BƯỚC 3: HOÀN THIỆN HÀM NÀY ĐỂ HIỂN THỊ DỮ LIỆU ***
    private void setSpec(String label, String value, View specView) {
        // Kiểm tra nếu giá trị không hợp lệ thì ẩn cả dòng đi
        if (value == null || value.trim().isEmpty() || value.equals("0") || value.toLowerCase().contains("null")) {
            specView.setVisibility(View.GONE);
            return;
        }
        specView.setVisibility(View.VISIBLE);
        TextView tvLabel = specView.findViewById(R.id.txtSpecLabel);
        TextView tvValue = specView.findViewById(R.id.txtSpecValue);
        tvLabel.setText(label);
        tvValue.setText(value);
    }

    private void addToCart() {
        if (currentVehicle != null) {
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
