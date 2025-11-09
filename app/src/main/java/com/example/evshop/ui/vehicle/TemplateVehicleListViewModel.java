package com.example.evshop.ui.vehicle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.evshop.data.repository.VehicleRepository;import com.example.evshop.domain.models.ApiEnvelope;
// <<< BƯỚC 1: THÊM IMPORT MỚI >>>
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
    private List<TemplateVehicle> originalVehicleList = new ArrayList<>();

    private final MutableLiveData<List<TemplateVehicle>> _vehicles = new MutableLiveData<>();
    public LiveData<List<TemplateVehicle>> getVehicles() {
        return _vehicles;
    }

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> getLoading() {
        return _loading;
    }

    @Inject
    public TemplateVehicleListViewModel(VehicleRepository repository) {
        this.repository = repository;
        loadAllVehicles();
    }

    public void loadAllVehicles() {
        _loading.postValue(true);
        // <<< BƯỚC 2: SỬA KIỂU DỮ LIỆU CỦA CALL VÀ CALLBACK >>>
        repository.getAllTemplateVehicles().enqueue(new Callback<ApiEnvelope<TemplateResult>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Response<ApiEnvelope<TemplateResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiEnvelope<TemplateResult> apiResponse = response.body();

                    // <<< BƯỚC 3: SỬA LOGIC LẤY DỮ LIỆU >>>
                    // Dữ liệu bây giờ nằm trong result.getData()
                    if (apiResponse.isSuccess && apiResponse.result != null && apiResponse.result.getData() != null) {
                        originalVehicleList = apiResponse.result.getData();
                        _vehicles.postValue(originalVehicleList);
                    } else {
                        // Xử lý lỗi từ API, ví dụ: hiển thị danh sách rỗng
                        originalVehicleList = new ArrayList<>();
                        _vehicles.postValue(originalVehicleList);
                    }
                } else {
                    // Xử lý lỗi HTTP
                    originalVehicleList = new ArrayList<>();
                    _vehicles.postValue(originalVehicleList);
                }
                _loading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Throwable t) {
                _loading.postValue(false);
                // Xử lý lỗi mạng
                originalVehicleList = new ArrayList<>();
                _vehicles.postValue(originalVehicleList);
            }
        });
    }

    public void searchVehicles(String query) {
        if (query == null || query.trim().isEmpty()) {
            _vehicles.setValue(originalVehicleList);
            return;
        }
        List<TemplateVehicle> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim();
        for (TemplateVehicle vehicle : originalVehicleList) {
            if (vehicle.getVersion() != null &&
                    vehicle.getVersion().getVersionName() != null &&
                    vehicle.getVersion().getVersionName().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(vehicle);
            }
        }
        _vehicles.setValue(filteredList);
    }
}
