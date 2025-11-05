package com.example.evshop.ui.vehicle;import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import com.example.evshop.databinding.ActivityVehicleListBinding; // <-- Chú ý: Tên file binding có thể khác
import com.example.evshop.ui.adapter.FeaturedVehicleAdapter; // <-- Tái sử dụng Adapter của HomeFragment

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TemplateVehicleListActivity extends AppCompatActivity {

    private ActivityVehicleListBinding b; // <-- Thay đổi tên này nếu file layout của bạn có tên khác
    private TemplateVehicleListViewModel vm;
    private FeaturedVehicleAdapter adapter; // Tái sử dụng adapter cũ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng ViewBinding
        b = ActivityVehicleListBinding.inflate(getLayoutInflater()); // <-- Sửa ở đây nếu cần
        setContentView(b.getRoot());

        // Khởi tạo ViewModel
        vm = new ViewModelProvider(this).get(TemplateVehicleListViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearchView();
        observeViewModel();
    }

    private void setupToolbar() {
        // Gán sự kiện cho nút quay về
        b.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        // Tái sử dụng adapter từ HomeFragment vì item layout giống nhau
        adapter = new FeaturedVehicleAdapter(template -> {
            // Xử lý khi click vào một xe, mở màn hình chi tiết
            Intent intent = new Intent(this, VehicleDetailActivity.class);
            intent.putExtra("VEHICLE_ID", template.getId());
            startActivity(intent);
        });
        b.rvAllVehicles.setAdapter(adapter); // <-- Đảm bảo ID trong XML là rvAllVehicles
    }

    private void setupSearchView() {
        b.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Thường không cần xử lý ở đây vì onQueryTextChange đã đủ
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Mỗi khi người dùng gõ chữ, gọi ViewModel để lọc
                vm.searchVehicles(newText);
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
                // Ẩn RecyclerView khi đang tải để ProgressBar không bị che
                b.rvAllVehicles.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            }
        });
    }
}
