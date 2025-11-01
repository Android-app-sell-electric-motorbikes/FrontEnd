package com.example.evshop.ui.vehicle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateVehicle;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named; // << THÊM IMPORT NÀY
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class VehicleListViewModel extends ViewModel {

    private final ApiService apiService;

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
    public VehicleListViewModel(@Named("AuthApiService") ApiService apiService) { // << SỬA Ở ĐÂY
        this.apiService = apiService;
    }

    public void fetchVehicles() {
        _loading.setValue(true);
        apiService.getAllTemplateVehicles().enqueue(new Callback<ApiEnvelope<List<TemplateVehicle>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<TemplateVehicle>>> call, Response<ApiEnvelope<List<TemplateVehicle>>> response) {
                _loading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    _vehicles.setValue(response.body().result);
                } else {
                    _error.setValue("Lỗi tải dữ liệu: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<TemplateVehicle>>> call, Throwable t) {
                _loading.setValue(false);
                _error.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}
