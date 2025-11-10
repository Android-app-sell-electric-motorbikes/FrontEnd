// File: ui/vehicle/VehicleListViewModel.java

package com.example.evshop.ui.vehicle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateResult;
import com.example.evshop.domain.models.TemplateVehicle;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class VehicleListViewModel extends ViewModel {

    private final ApiService apiService;

    private final MutableLiveData<List<TemplateVehicle>> _vehicles = new MutableLiveData<>();
    public LiveData<List<TemplateVehicle>> getVehicles() { return _vehicles; }

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> getLoading() { return _loading; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    @Inject
    public VehicleListViewModel(@Named("AuthApiService") ApiService apiService) {
        this.apiService = apiService;
    }

    // ========================================================
    // ***           SỬA LẠI TOÀN BỘ PHƯƠNG THỨC NÀY        ***
    // ========================================================
    public void fetchVehicles(
            String searchTerm,
            Long minPrice,
            Long maxPrice,
            Boolean sortByPriceAsc
    ) {
        _loading.setValue(true);

        // Gọi phương thức API đã được cập nhật với đầy đủ tham số
        apiService.getAllTemplateVehicles(1, 100, searchTerm, minPrice, maxPrice, sortByPriceAsc)
                .enqueue(new Callback<ApiEnvelope<TemplateResult>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Response<ApiEnvelope<TemplateResult>> response) {
                        _loading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            ApiEnvelope<TemplateResult> apiResponse = response.body();

                            // *** SỬA LẠI ĐỂ DÙNG GETTER ***
                            if (apiResponse.isSuccess && apiResponse.result != null && apiResponse.result.getData() != null) {
                                _vehicles.setValue(apiResponse.result.getData());
                            } else {
                                // Xử lý lỗi từ API, hiển thị danh sách rỗng
                                _vehicles.setValue(new ArrayList<>());
                                String message = apiResponse.message != null ? apiResponse.message : "Không có dữ liệu";
                                _error.setValue("Lỗi: " + message);
                            }
                        } else {
                            // Xử lý lỗi HTTP
                            _vehicles.setValue(new ArrayList<>());
                            _error.setValue("Lỗi tải dữ liệu: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Throwable t) {
                        _loading.setValue(false);
                        _vehicles.setValue(new ArrayList<>());
                        _error.setValue("Lỗi mạng: " + t.getMessage());
                    }
                });
    }
}
