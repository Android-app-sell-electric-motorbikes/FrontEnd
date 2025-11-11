package com.example.evshop.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // << THÊM IMPORT NÀY
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
// *** XÓA IMPORT KHÔNG DÙNG TỚI ***
// import com.example.evshop.domain.models.InventoryItem;
import com.example.evshop.data.ApiService;
import com.example.evshop.data.RetrofitClient;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.InventoryResult; // << THÊM IMPORT CHO MODEL MỚI
import com.example.evshop.ui.adapter.InventoryAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
// *** XÓA IMPORT KHÔNG DÙNG TỚI ***
// import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleInventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InventoryAdapter inventoryAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_inventory);

        // 1. Ánh xạ Toolbar từ layout
        toolbar = findViewById(R.id.toolbar);
        // 2. Đặt Toolbar này làm ActionBar cho Activity
        setSupportActionBar(toolbar);
        // 3. Kích hoạt và hiển thị nút "Up" (mũi tên quay lại)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = findViewById(R.id.rv_inventory);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        setupRecyclerView();
        fetchInventoryData();
    }

    /**
     * Hàm này sẽ được gọi khi người dùng nhấn vào một item trên menu (bao gồm cả nút "Up").
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        // SỬA LỖI QUAN TRỌNG: Cần thêm LayoutManager để RecyclerView biết cách hiển thị item
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        inventoryAdapter = new InventoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(inventoryAdapter);
    }

    private void fetchInventoryData() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getApi(this);

        // ========================================================
        // ***           SỬA LỖI KIỂU DỮ LIỆU Ở ĐÂY (1)         ***
        // ========================================================
        // Kiểu dữ liệu của Call và Callback phải là ApiEnvelope<InventoryResult>
        Call<ApiEnvelope<InventoryResult>> call = apiService.getInventory();

        call.enqueue(new Callback<ApiEnvelope<InventoryResult>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<InventoryResult>> call, @NonNull Response<ApiEnvelope<InventoryResult>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiEnvelope<InventoryResult> apiResponse = response.body();

                    // ========================================================
                    // ***           SỬA LỖI LOGIC LẤY DATA Ở ĐÂY (2)     ***
                    // ========================================================
                    // Bây giờ, danh sách item nằm bên trong đối tượng 'result.data'
                    if (apiResponse.isSuccess && apiResponse.result != null && apiResponse.result.getData() != null && !apiResponse.result.getData().isEmpty()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        // Cập nhật adapter với danh sách lấy từ result.getData()
                        inventoryAdapter.updateData(apiResponse.result.getData());
                    } else {
                        String message = (apiResponse.message != null && !apiResponse.message.isEmpty()) ? apiResponse.message : "Không có xe nào trong kho";
                        handleApiError(message);
                    }
                } else {
                    handleApiError("Lỗi " + response.code() + ": " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<InventoryResult>> call, @NonNull Throwable t) {
                // Thêm Log.e để dễ debug hơn
                Log.e("VehicleInventory", "API Call Failed", t);
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
