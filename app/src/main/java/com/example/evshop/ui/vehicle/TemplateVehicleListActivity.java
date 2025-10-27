// File: TemplateVehicleListActivity.java
package com.example.evshop.ui.vehicle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TemplateVehicleListActivity extends AppCompatActivity implements TemplateVehicleAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TemplateVehicleAdapter adapter;
    private VehicleListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Giả sử layout của bạn tên là activity_vehicle_list
        setContentView(R.layout.activity_vehicle_list);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(VehicleListViewModel.class);

        // Ánh xạ View từ layout
        // Giả sử ID của RecyclerView là 'recyclerVehicles' và ProgressBar là 'progressBar'
        recyclerView = findViewById(R.id.recyclerVehicles);
        progressBar = findViewById(R.id.progressBar);

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Khởi tạo Adapter và truyền "this" (Activity) vào làm listener
        adapter = new TemplateVehicleAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Lắng nghe và cập nhật danh sách xe từ ViewModel
        viewModel.getVehicles().observe(this, vehicles -> {
            if (vehicles != null) {
                adapter.updateVehicles(vehicles);
            }
        });

        // Lắng nghe và hiển thị trạng thái loading chung
        viewModel.isLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Lắng nghe kết quả chi tiết xe trả về
        viewModel.getVehicleDetails().observe(this, result -> {
            if (result != null && result.getDetails() != null) {
                Log.d("VEHICLE_DEBUG", "Activity: Nhận được chi tiết cho vị trí "
                        + result.getPosition() + ". Bắt đầu cập nhật Adapter.");
                adapter.onDetailsFetched(result.getPosition(), result.getDetails());
            } else {
                Log.e("VEHICLE_DEBUG", "Activity: Nhận được kết quả chi tiết nhưng bị NULL!");
                Toast.makeText(this, "Không thể tải chi tiết sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm này được gọi từ Adapter khi người dùng nhấn vào item
    @Override
    public void onFetchDetails(int position, String versionId) {
        if (versionId == null || versionId.isEmpty()) {
            Log.e("VEHICLE_DEBUG", "Activity: Yêu cầu fetch chi tiết tại vị trí "
                    + position + " nhưng versionId BỊ NULL. Không thể gọi API.");
            Toast.makeText(this, "Lỗi: Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("VEHICLE_DEBUG", "Activity: Yêu cầu ViewModel gọi API fetch chi tiết cho versionId: " + versionId);
        // Yêu cầu ViewModel gọi API
        viewModel.fetchVehicleDetails(position, versionId);
    }
}
