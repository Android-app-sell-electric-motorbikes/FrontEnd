// File: D:/PRM391/FrontEnd/app/src/main/java/com/example/evshop/ui/vehicle/TemplateVehicleListActivity.java
package com.example.evshop.ui.vehicle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
import com.example.evshop.databinding.ActivityVehicleListBinding;
// *** BƯỚC 1: SỬA LẠI IMPORT ĐỂ DÙNG ĐÚNG ADAPTER ***
import com.example.evshop.domain.models.TemplateVehicle;

import dagger.hilt.android.AndroidEntryPoint;

// *** BƯỚC 2: IMPLEMENT INTERFACE MỚI CỦA ADAPTER ***
@AndroidEntryPoint
public class TemplateVehicleListActivity extends AppCompatActivity
        implements FilterSortSheet.FilterListener, TemplateVehicleAdapter.OnItemClickListener {

    private ActivityVehicleListBinding b;
    private TemplateVehicleListViewModel vm;
    // Sửa lại tên lớp Adapter
    private TemplateVehicleAdapter adapter;

    // Biến để lưu trạng thái lọc hiện tại (không đổi)
    private String currentSearchTerm = null;
    private Long currentMinPrice = null;
    private Long currentMaxPrice = null;
    private Boolean currentSortByPriceAsc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVehicleListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        vm = new ViewModelProvider(this).get(TemplateVehicleListViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearchView();
        observeViewModel();

        // Gọi fetch lần đầu tiên khi Activity được tạo
        vm.fetchVehicles(currentSearchTerm, currentMinPrice, currentMaxPrice, currentSortByPriceAsc);
    }

    // setupToolbar() và setupSearchView() không thay đổi
    private void setupToolbar() {
        b.toolbar.setNavigationOnClickListener(v -> finish());
        b.toolbar.inflateMenu(R.menu.menu_vehicle_list);
        b.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_filter) {
                FilterSortSheet bottomSheet = FilterSortSheet.newInstance();
                bottomSheet.setFilterListener(this);
                bottomSheet.show(getSupportFragmentManager(), "FilterSortSheet");
                return true;
            }
            return false;
        });
    }

    // *** BƯỚC 3: SỬA LẠI CÁCH KHỞI TẠO ADAPTER ***
    private void setupRecyclerView() {
        // Khởi tạo adapter và truyền "this" (chính Activity này) làm listener
        adapter = new TemplateVehicleAdapter(this);
        b.rvAllVehicles.setLayoutManager(new GridLayoutManager(this, 2));
        b.rvAllVehicles.setAdapter(adapter);
    }

    private void setupSearchView() {
        b.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchTerm = query;
                vm.fetchVehicles(currentSearchTerm, currentMinPrice, currentMaxPrice, currentSortByPriceAsc);
                b.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty() && currentSearchTerm != null) {
                    currentSearchTerm = null;
                    vm.fetchVehicles(currentSearchTerm, currentMinPrice, currentMaxPrice, currentSortByPriceAsc);
                }
                return true;
            }
        });
    }

    // *** BƯỚC 4: SỬA LẠI CÁCH CẬP NHẬT DỮ LIỆU CHO ADAPTER ***
    private void observeViewModel() {
        vm.getVehicles().observe(this, vehicles -> {
            if (vehicles != null) {
                // Sử dụng phương thức updateVehicles của adapter mới
                adapter.updateVehicles(vehicles);

                // Cuộn lên đầu danh sách
                if (!vehicles.isEmpty()) {
                    b.rvAllVehicles.scrollToPosition(0);
                }

                // Cập nhật giao diện cho trường hợp danh sách rỗng (không đổi)
                boolean isListEmpty = vehicles.isEmpty();
                b.tvEmptyMessage.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
                b.rvAllVehicles.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
            }
        });

        // Phần lắng nghe getLoading() và getError() không thay đổi
        vm.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                b.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    b.rvAllVehicles.setVisibility(View.GONE);
                    b.tvEmptyMessage.setVisibility(View.GONE);
                }
            }
        });

        vm.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Phương thức nhận kết quả từ BottomSheet (không đổi)
    @Override
    public void onFilterApplied(Long minPrice, Long maxPrice, Boolean sortByPriceAsc) {
        this.currentMinPrice = minPrice;
        this.currentMaxPrice = maxPrice;
        this.currentSortByPriceAsc = sortByPriceAsc;
        vm.fetchVehicles(currentSearchTerm, currentMinPrice, currentMaxPrice, currentSortByPriceAsc);
        Toast.makeText(this, "Đã áp dụng bộ lọc!", Toast.LENGTH_SHORT).show();
    }

    // *** BƯỚC 5: IMPLEMENT PHƯƠNG THỨC CỦA OnItemClickListener ***
    @Override
    public void onItemClick(TemplateVehicle vehicle) {
        // Đây là nơi xử lý sự kiện khi một item trong RecyclerView được click
        Intent intent = new Intent(this, VehicleDetailActivity.class);
        // Truyền ID hoặc toàn bộ object vehicle sang màn hình chi tiết
        // Truyền ID sẽ an toàn hơn và giúp màn hình chi tiết luôn lấy dữ liệu mới nhất
        intent.putExtra("VEHICLE_ID", vehicle.getId());
        startActivity(intent);
    }
}
