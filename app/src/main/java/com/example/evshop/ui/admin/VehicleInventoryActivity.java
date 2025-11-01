package com.example.evshop.ui.admin;

import android.os.Bundle;
import android.view.MenuItem; // << THÊM IMPORT NÀY
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull; // << THÊM IMPORT NÀY
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.ApiService;
import com.example.evshop.data.RetrofitClient;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.InventoryItem;
import com.example.evshop.ui.adapter.InventoryAdapter;
import com.google.android.material.appbar.MaterialToolbar; // << THÊM IMPORT NÀY

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleInventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InventoryAdapter inventoryAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MaterialToolbar toolbar; // << KHAI BÁO BIẾN CHO TOOLBAR

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_inventory);

        // ========================================================
        // === BẮT ĐẦU PHẦN CODE MỚI ĐỂ THÊM NÚT BACK ===
        // ========================================================

        // 1. Ánh xạ Toolbar từ layout
        toolbar = findViewById(R.id.toolbar);

        // 2. Đặt Toolbar này làm ActionBar cho Activity
        setSupportActionBar(toolbar);

        // 3. Kích hoạt và hiển thị nút "Up" (mũi tên quay lại)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // ========================================================
        // === KẾT THÚC PHẦN CODE MỚI ===
        // ========================================================

        // Ánh xạ các view khác (giữ nguyên)
        recyclerView = findViewById(R.id.rv_inventory);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        // Thiết lập RecyclerView và Adapter (giữ nguyên)
        setupRecyclerView();

        // Tải dữ liệu từ API (giữ nguyên)
        fetchInventoryData();
    }

    // ========================================================
    // === BƯỚC 4: THÊM HÀM XỬ LÝ SỰ KIỆN NHẤN NÚT BACK ===
    // ========================================================
    /**
     * Hàm này sẽ được gọi khi người dùng nhấn vào một item trên menu (bao gồm cả nút "Up").
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Kiểm tra xem nút được nhấn có phải là nút "Up" (ID là android.R.id.home) không
        if (item.getItemId() == android.R.id.home) {
            // Kết thúc Activity hiện tại và quay về màn hình trước đó (AdminActivity)
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- CÁC HÀM CÒN LẠI GIỮ NGUYÊN (KHÔNG THAY ĐỔI) ---

    private void setupRecyclerView() {
        inventoryAdapter = new InventoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(inventoryAdapter);
    }

    private void fetchInventoryData() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getApi(this);
        Call<ApiEnvelope<List<InventoryItem>>> call = apiService.getInventory();

        call.enqueue(new Callback<ApiEnvelope<List<InventoryItem>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<InventoryItem>>> call, Response<ApiEnvelope<List<InventoryItem>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiEnvelope<List<InventoryItem>> apiResponse = response.body();
                    if (apiResponse.isSuccess && apiResponse.result != null && !apiResponse.result.isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        inventoryAdapter.updateData(apiResponse.result);
                    } else {
                        String message = (apiResponse.message != null && !apiResponse.message.isEmpty()) ? apiResponse.message : "Không có xe nào trong kho";
                        handleApiError(message);
                    }
                } else {
                    handleApiError("Lỗi " + response.code() + ": " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<InventoryItem>>> call, Throwable t) {
                handleApiError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void handleApiError(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText(message);
    }
}
