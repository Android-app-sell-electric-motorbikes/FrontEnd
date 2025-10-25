// Thay thế toàn bộ nội dung file TemplateVehicleListActivity.java bằng code này
package com.example.evshop.ui.vehicle;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateVehicle;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TemplateVehicleListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    // Sửa lại tên Adapter cho thống nhất
    private TemplateVehicleAdapter adapter;

    @Inject
    ApiService apiService; // Hilt sẽ tự động cung cấp đối tượng ApiService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list);

        // Ánh xạ các View từ layout
        recyclerView = findViewById(R.id.recyclerVehicles);
        // Giả sử bạn có một ProgressBar trong file layout activity_vehicle_list.xml
        // Nếu không có, hãy thêm nó vào.
        progressBar = findViewById(R.id.progressBar);

        // Thiết lập RecyclerView và Adapter một lần duy nhất
        setupRecyclerView();

        // Bắt đầu gọi API để tải dữ liệu
        loadVehicles();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Khởi tạo Adapter với một danh sách rỗng. Adapter sẽ được tái sử dụng.
        adapter = new TemplateVehicleAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void loadVehicles() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE); // Hiển thị vòng xoay loading
        }

        // Gọi hàm API đúng từ ApiService đã được inject
        apiService.getAllTemplateVehicles().enqueue(new Callback<ApiEnvelope<List<TemplateVehicle>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<TemplateVehicle>>> call, Response<ApiEnvelope<List<TemplateVehicle>>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE); // Ẩn vòng xoay loading
                }

                // Kiểm tra xem gọi API có thành công và có dữ liệu trả về không
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    // Sửa lại để lấy dữ liệu từ trường "result" như trong JSON của bạn
                    List<TemplateVehicle> vehicles = response.body().result;

                    if (vehicles != null) {
                        // Cập nhật dữ liệu cho Adapter đã có, không tạo mới
                        adapter.updateVehicles(vehicles);
                    } else {
                        Toast.makeText(TemplateVehicleListActivity.this, "Không có sản phẩm nào.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Thông báo lỗi nếu API trả về lỗi (ví dụ: 404, 500)
                    String errorMessage = response.body() != null ? response.body().message : "Không tải được dữ liệu";
                    Toast.makeText(TemplateVehicleListActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<TemplateVehicle>>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE); // Ẩn loading khi có lỗi mạng
                }
                // Thông báo lỗi khi không có kết nối mạng hoặc lỗi từ Retrofit
                Toast.makeText(TemplateVehicleListActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
