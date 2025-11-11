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
// RecyclerView không cần import vì đã có trong binding
// import androidx.recyclerview.widget.RecyclerView;

import com.example.evshop.R;
// Giữ nguyên tên Binding gốc của bạn
import com.example.evshop.databinding.ActivityVehicleListBinding;
import com.example.evshop.domain.models.TemplateVehicle;
// import com.example.evshop.ui.detail.VehicleDetailActivity; // Bỏ comment nếu có màn hình này

import dagger.hilt.android.AndroidEntryPoint;

// *** BƯỚC 1: SỬA LẠI PHẦN IMPLEMENTS INTERFACE ***
@AndroidEntryPoint
public class TemplateVehicleListActivity extends AppCompatActivity
        implements FilterSortSheet.FilterListener, TemplateVehicleAdapter.OnItemClickListener {

    private ActivityVehicleListBinding b;
    private TemplateVehicleListViewModel vm;
    private TemplateVehicleAdapter adapter;

    // *** BƯỚC 2: THÊM BIẾN LƯU TRẠNG THÁI RATING ***
    private String currentSearchTerm = null;
    private Long currentMinPrice = null;
    private Long currentMaxPrice = null;
    private Boolean currentSortByPriceAsc = null;
    private Integer currentMinRating = null; // << THÊM DÒNG NÀY

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

        // Không cần gọi fetch ở đây nữa, vì ViewModel đã tự gọi fetchInitialData() trong constructor của nó
    }

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

    private void setupRecyclerView() {
        adapter = new TemplateVehicleAdapter(this);
        // Giữ nguyên ID gốc của bạn: rvAllVehicles
        b.rvAllVehicles.setLayoutManager(new GridLayoutManager(this, 2));
        b.rvAllVehicles.setAdapter(adapter);
    }

    // *** BƯỚC 3: CẬP NHẬT LOGIC TÌM KIẾM ***
    private void setupSearchView() {
        b.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchTerm = query;
                // Gọi phương thức lọc ở client với TẤT CẢ các tham số hiện tại
                vm.processClientSideFilter(currentSearchTerm, currentMinPrice, currentMaxPrice, currentSortByPriceAsc, currentMinRating);
                b.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Tự động lọc khi người dùng thay đổi text
                currentSearchTerm = newText.isEmpty() ? null : newText;
                vm.processClientSideFilter(currentSearchTerm, currentMinPrice, currentMaxPrice, currentSortByPriceAsc, currentMinRating);
                return true;
            }
        });
    }

    private void observeViewModel() {
        vm.getVehicles().observe(this, vehicles -> {
            if (vehicles != null) {
                adapter.updateVehicles(vehicles);

                if (!vehicles.isEmpty()) {
                    b.rvAllVehicles.scrollToPosition(0);
                }

                boolean isListEmpty = vehicles.isEmpty();
                // Giữ nguyên ID gốc của bạn: tvEmptyMessage và rvAllVehicles
                b.tvEmptyMessage.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
                b.rvAllVehicles.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
            }
        });

        vm.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                // Giữ nguyên ID gốc của bạn: progressBar
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

    // *** BƯỚC 4: CẬP NHẬT PHƯƠNG THỨC onFilterApplied ***
    @Override
    public void onFilterApplied(Long minPrice, Long maxPrice, Boolean sortByPriceAsc, Integer minRating) {
        // Cập nhật lại tất cả các biến trạng thái lọc
        this.currentMinPrice = minPrice;
        this.currentMaxPrice = maxPrice;
        this.currentSortByPriceAsc = sortByPriceAsc;
        this.currentMinRating = minRating; // << LƯU LẠI TRẠNG THÁI RATING

        // GỌI PHƯƠNG THỨC LỌC Ở CLIENT, KHÔNG GỌI LẠI API
        vm.processClientSideFilter(currentSearchTerm, currentMinPrice, currentMaxPrice, currentSortByPriceAsc, currentMinRating);

        Toast.makeText(this, "Đã áp dụng bộ lọc!", Toast.LENGTH_SHORT).show();
    }

    // Phương thức onItemClick không thay đổi
    @Override
    public void onItemClick(TemplateVehicle vehicle) {
        Toast.makeText(this, "Clicked: " + vehicle.getVersion().getVersionName(), Toast.LENGTH_SHORT).show();
        // Mở màn hình chi tiết
         Intent intent = new Intent(this, VehicleDetailActivity.class);
         intent.putExtra("VEHICLE_ID", vehicle.getId());
         startActivity(intent);
    }
}
