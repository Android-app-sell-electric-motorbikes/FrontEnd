package com.example.evshop.ui.vehicle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.evshop.data.repository.VehicleRepository;
import com.example.evshop.domain.models.ApiEnvelope;
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
        repository.getAllTemplateVehicles(new Callback<ApiEnvelope<List<TemplateVehicle>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<TemplateVehicle>>> call, Response<ApiEnvelope<List<TemplateVehicle>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    originalVehicleList = response.body().result != null ? response.body().result : new ArrayList<>();
                    _vehicles.postValue(originalVehicleList);
                } else {
                    // Xử lý lỗi, có thể cập nhật một LiveData lỗi khác
                }
                _loading.postValue(false);
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<TemplateVehicle>>> call, Throwable t) {
                _loading.postValue(false);
                // Xử lý lỗi, có thể cập nhật một LiveData lỗi khác
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
            // Đảm bảo không bị NullPointerException
            if (vehicle.getVersion() != null &&
                    vehicle.getVersion().getVersionName() != null &&
                    vehicle.getVersion().getVersionName().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(vehicle);
            }
        }
        _vehicles.setValue(filteredList);
    }
}
