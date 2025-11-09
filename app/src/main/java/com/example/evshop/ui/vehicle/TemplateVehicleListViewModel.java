// File: ui/vehicle/TemplateVehicleListViewModel.java

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
import java.util.List;
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

    @Inject
    public TemplateVehicleListViewModel(VehicleRepository repository) {
        this.repository = repository;
        loadVehicles(null); // Tải toàn bộ danh sách lần đầu
    }

    // === PHƯƠNG THỨC GỌI API ĐÃ ĐƯỢC CẬP NHẬT ===
    public void loadVehicles(String searchTerm) {
        _loading.postValue(true);

        repository.getAllTemplateVehicles(1, 100, searchTerm) // Page 1, size 100, với từ khóa tìm kiếm
                .enqueue(new Callback<ApiEnvelope<TemplateResult>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Response<ApiEnvelope<TemplateResult>> response) {
                        _loading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            ApiEnvelope<TemplateResult> apiResponse = response.body();
                            if (apiResponse.isSuccess && apiResponse.result != null && apiResponse.result.getData() != null) {
                                _vehicles.postValue(apiResponse.result.getData());
                            } else {
                                _vehicles.postValue(new ArrayList<>()); // Trả về danh sách rỗng nếu có lỗi
                            }
                        } else {
                            _vehicles.postValue(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Throwable t) {
                        _loading.postValue(false);
                        _vehicles.postValue(new ArrayList<>()); // Trả về danh sách rỗng khi lỗi mạng
                    }
                });
    }

    // === PHƯƠNG THỨC MỚI ĐỂ VIEW GỌI ĐẾN ===
    // Được gọi khi người dùng nhập xong và nhấn tìm kiếm
    public void searchVehicles(String query) {
        // Nếu query rỗng hoặc null, tải lại toàn bộ danh sách
        // Nếu có query, chỉ tải kết quả tìm kiếm
        loadVehicles(query);
    }
}
