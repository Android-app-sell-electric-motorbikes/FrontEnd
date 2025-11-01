package com.example.evshop.ui.admin; // Đảm bảo đúng package

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// Import các lớp cần thiết (đảm bảo đường dẫn package là đúng)
import com.example.evshop.R;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.InventoryItem;
import com.example.evshop.data.ApiService; // Giả sử bạn có package data.network
import com.example.evshop.data.RetrofitClient; // Giả sử bạn có class này
import com.example.evshop.ui.adapter.InventoryAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleInventoryActivity extends AppCompatActivity {

    private static final String TAG = "VehicleInventory";

    private RecyclerView recyclerView;
    private InventoryAdapter inventoryAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty; // Thêm TextView cho thông báo trống

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_inventory);

        // --- Cấu hình Toolbar ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Bật nút quay lại trên toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Xử lý sự kiện khi nhấn nút quay lại
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // --- Ánh xạ Views ---
        recyclerView = findViewById(R.id.rv_inventory);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        // --- Thiết lập RecyclerView ---
        setupRecyclerView();

        // --- Lấy dữ liệu từ API ---
        fetchInventoryData();
    }

    private void setupRecyclerView() {
        inventoryAdapter = new InventoryAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(inventoryAdapter);
    }

    private void fetchInventoryData() {
        // Hiển thị trạng thái loading
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);        // *** THAY ĐỔI QUAN TRỌNG Ở ĐÂY ***
        // Lấy ApiService bằng cách gọi getApi() và truyền vào Context của Activity
        ApiService apiService = RetrofitClient.getApi(this);
        Call<ApiEnvelope<List<InventoryItem>>> call = apiService.getInventory();
        // Thực hiện gọi API bất đồng bộ
        call.enqueue(new Callback<ApiEnvelope<List<InventoryItem>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<InventoryItem>>> call, Response<ApiEnvelope<List<InventoryItem>>> response) {
                progressBar.setVisibility(View.GONE); // Ẩn loading

                if (response.isSuccessful() && response.body() != null) {
                    ApiEnvelope<List<InventoryItem>> apiResponse = response.body();
                    if (apiResponse.isSuccess && apiResponse.result != null && !apiResponse.result.isEmpty()) {
                        // Nếu có dữ liệu, hiển thị RecyclerView và cập nhật Adapter
                        recyclerView.setVisibility(View.VISIBLE);
                        inventoryAdapter.updateData(apiResponse.result);
                    } else {
                        // Nếu không có dữ liệu hoặc API báo lỗi, hiển thị thông báo
                        String message = (apiResponse.message != null && !apiResponse.message.isEmpty()) ? apiResponse.message : "Không có xe nào trong kho";
                        handleApiError(message);
                    }
                } else {
                    // Xử lý lỗi từ server (ví dụ: 404 Not Found, 500 Internal Server Error)
                    handleApiError("Lỗi " + response.code() + ": " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<InventoryItem>>> call, Throwable t) {
                // Xử lý lỗi kết nối mạng (không thể kết nối tới server)
                handleApiError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }


    // Hàm chung để xử lý và hiển thị lỗi
    private void handleApiError(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        tvEmpty.setText(errorMessage);
        tvEmpty.setVisibility(View.VISIBLE);
        Log.e(TAG, errorMessage); // Ghi log lỗi để debug
        Toast.makeText(VehicleInventoryActivity.this, errorMessage, Toast.LENGTH_LONG).show(); // Hiển thị thông báo ngắn cho người dùng
    }
}
