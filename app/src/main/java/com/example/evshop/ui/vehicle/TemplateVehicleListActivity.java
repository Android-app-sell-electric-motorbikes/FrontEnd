package com.example.evshop.ui.vehicle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager; // **THÊM IMPORT**

import com.example.evshop.databinding.ActivityVehicleListBinding;
import com.example.evshop.ui.adapter.VehicleAdapter; // **DÙNG ADAPTER CHUNG**

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TemplateVehicleListActivity extends AppCompatActivity {

    private ActivityVehicleListBinding b;
    private TemplateVehicleListViewModel vm;
    private VehicleAdapter adapter; // **SỬA LẠI THÀNH VehicleAdapter**

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVehicleListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // Khởi tạo ViewModel
        vm = new ViewModelProvider(this).get(TemplateVehicleListViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearchView(); // << SỬA LOGIC TRONG HÀM NÀY
        observeViewModel();
    }

    private void setupToolbar() {
        b.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        // Sử dụng adapter chung, có thể tái sử dụng từ HomeFragment nếu cấu trúc item giống hệt
        adapter = new VehicleAdapter(template -> {
            Intent intent = new Intent(this, VehicleDetailActivity.class);
            intent.putExtra("VEHICLE_ID", template.getId());
            startActivity(intent);
        });
        // Thiết lập layout manager để hiển thị dạng lưới
        b.rvAllVehicles.setLayoutManager(new GridLayoutManager(this, 2));
        b.rvAllVehicles.setAdapter(adapter);
    }

    // ========================================================
    // ***           SỬA LẠI LOGIC TÌM KIẾM Ở ĐÂY          ***
    // ========================================================
    private void setupSearchView() {
        b.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Được gọi khi người dùng nhấn nút tìm kiếm trên bàn phím.
             * Đây là thời điểm tốt nhất để gọi API.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                vm.searchVehicles(query);
                b.searchView.clearFocus(); // Ẩn bàn phím đi cho gọn
                return true; // Báo cho hệ thống là đã xử lý sự kiện
            }

            /**
             * Được gọi mỗi khi text thay đổi. Không nên gọi API ở đây để tránh quá tải.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                // Nếu người dùng xóa hết chữ trong ô tìm kiếm, tự động tải lại danh sách đầy đủ
                if (newText.isEmpty()) {
                    vm.searchVehicles(null);
                }
                return true;
            }
        });
    }

    private void observeViewModel() {
        // Lắng nghe danh sách xe từ ViewModel
        vm.getVehicles().observe(this, vehicles -> {
            if (vehicles != null) {
                adapter.submitList(vehicles);
            }
        });

        // Lắng nghe trạng thái loading để hiện/ẩn ProgressBar
        vm.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                b.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                // Ẩn RecyclerView khi đang tải để người dùng thấy ProgressBar
                b.rvAllVehicles.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            }
        });
    }
}
