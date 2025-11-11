// File: D:/PRM391/FrontEnd/app/src/main/java/com/example/evshop/ui/vehicle/TemplateVehicleListViewModel.java

package com.example.evshop.ui.vehicle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.repository.VehicleRepository;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateResult;
import com.example.evshop.domain.models.TemplateVehicle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class TemplateVehicleListViewModel extends ViewModel {

    private final VehicleRepository repository;

    private final MutableLiveData<List<TemplateVehicle>> _vehicles = new MutableLiveData<>();
    public LiveData<List<TemplateVehicle>> getVehicles() { return _vehicles; }

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> getLoading() { return _loading; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    // --- BƯỚC 2.1: THÊM DANH SÁCH GỐC ---
    // Danh sách này sẽ lưu trữ dữ liệu gốc từ API, trước khi lọc/sắp xếp
    private List<TemplateVehicle> originalVehicleList = new ArrayList<>();

    @Inject
    public TemplateVehicleListViewModel(VehicleRepository repository) {
        this.repository = repository;
        // Tải danh sách mặc định khi ViewModel được tạo lần đầu
        fetchInitialData();
    }

    // --- BƯỚC 2.2: TẠO PHƯƠNG THỨC GỌI API BAN ĐẦU ---
    /**
     * Chỉ gọi API một lần để lấy toàn bộ dữ liệu.
     * Các bộ lọc và tìm kiếm sau này sẽ được xử lý ở client.
     */
    public void fetchInitialData() {
        _loading.postValue(true);
        _error.postValue(null);

        // Gọi API mà không có tham số lọc (hoặc chỉ có tham số cơ bản)
        repository.getAllTemplateVehicles(1, 100, null, null, null, null)
                .enqueue(new Callback<ApiEnvelope<TemplateResult>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Response<ApiEnvelope<TemplateResult>> response) {
                        _loading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            ApiEnvelope<TemplateResult> apiResponse = response.body();
                            if (apiResponse.isSuccess && apiResponse.result != null && apiResponse.result.getData() != null) {
                                List<TemplateVehicle> vehiclesFromApi = apiResponse.result.getData();

                                // *** BƯỚC 2.3: GÁN RATING NGẪU NHIÊN ***
                                for (TemplateVehicle vehicle : vehiclesFromApi) {
                                    // Tạo rating ngẫu nhiên từ 3.0 đến 5.0
                                    double randomRating = 3.0 + ThreadLocalRandom.current().nextDouble(0, 2.01);
                                    vehicle.setRating(randomRating);
                                }

                                // Lưu lại danh sách gốc đã có rating
                                originalVehicleList = new ArrayList<>(vehiclesFromApi);
                                // Cập nhật LiveData để hiển thị danh sách ban đầu
                                _vehicles.postValue(new ArrayList<>(originalVehicleList));

                            } else {
                                String message = apiResponse.message != null ? apiResponse.message : "Không có dữ liệu";
                                _error.postValue("Lỗi: " + message);
                            }
                        } else {
                            _error.postValue("Lỗi tải dữ liệu: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Throwable t) {
                        _loading.postValue(false);
                        _error.postValue("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    // --- BƯỚC 2.4: TẠO PHƯƠNG THỨC LỌC & SẮP XẾP Ở CLIENT ---
    /**
     * Xử lý tìm kiếm, lọc và sắp xếp trên danh sách đã có sẵn.
     * KHÔNG GỌI LẠI API.
     */
    public void processClientSideFilter(
            String searchTerm,
            Long minPrice,
            Long maxPrice,
            Boolean sortByPriceAsc,
            Integer minRating // Thêm tham số rating
    ) {
        if (originalVehicleList == null) return;

        // Bắt đầu với luồng dữ liệu (stream) từ danh sách gốc
        List<TemplateVehicle> processedList = originalVehicleList.stream()
                // 1. Lọc theo từ khóa tìm kiếm
                .filter(v -> searchTerm == null || searchTerm.isEmpty() ||
                        (v.getVersion() != null && v.getVersion().getVersionName().toLowerCase().contains(searchTerm.toLowerCase())))
                // 2. Lọc theo giá
                .filter(v -> (minPrice == null || v.getPrice() >= minPrice) &&
                        (maxPrice == null || v.getPrice() <= maxPrice))
                // 3. Lọc theo rating
                .filter(v -> minRating == null || minRating == 0 || v.getRating() >= minRating)
                .collect(Collectors.toList());

        // 4. Sắp xếp danh sách kết quả
        if (sortByPriceAsc != null) {
            if (sortByPriceAsc) {
                // Sắp xếp tăng dần
                processedList.sort(Comparator.comparingLong(TemplateVehicle::getPrice));
            } else {
                // Sắp xếp giảm dần
                processedList.sort(Comparator.comparingLong(TemplateVehicle::getPrice).reversed());
            }
        }

        // Cập nhật UI với danh sách đã được xử lý
        _vehicles.setValue(processedList);
    }

    /*
     * Bỏ phương thức fetchVehicles cũ đi hoặc sửa nó thành processClientSideFilter
     * public void fetchVehicles(...) { ... } // XÓA HOẶC SỬA LẠI
     */
}

