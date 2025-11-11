package com.example.evshop.ui.vehicle;

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
public class VehicleListViewModel extends ViewModel {

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

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() {
        return _error;
    }

    @Inject
    public VehicleListViewModel(VehicleRepository repository) { // ** SỬA LẠI: BỎ @Named **
        this.repository = repository;
        loadAllVehicles();
    }

    public void loadAllVehicles() {
        _loading.postValue(true);
        repository.getAllTemplateVehicles(1, 100, null, null, null, null).enqueue(new Callback<ApiEnvelope<TemplateResult>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<TemplateResult>> call, Response<ApiEnvelope<TemplateResult>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    if (response.body().result != null && response.body().result.getData() != null) {
                         originalVehicleList = response.body().result.getData();
                        _vehicles.postValue(originalVehicleList);
                    }
                } else {
                    _error.postValue("Lỗi tải dữ liệu: " + response.code());
                }
                _loading.postValue(false);
            }

            @Override
            public void onFailure(Call<ApiEnvelope<TemplateResult>> call, Throwable t) {
                _error.postValue("Lỗi mạng: " + t.getMessage());
                _loading.postValue(false);
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
