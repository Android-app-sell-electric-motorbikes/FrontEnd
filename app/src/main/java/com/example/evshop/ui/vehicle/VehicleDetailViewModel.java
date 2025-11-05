package com.example.evshop.ui.vehicle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.repository.VehicleRepository;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateVehicle;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class VehicleDetailViewModel extends ViewModel {

    private final VehicleRepository repository;

    // LiveData để thông báo trạng thái tải
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>();

    // LiveData để thông báo lỗi
    public final MutableLiveData<String> error = new MutableLiveData<>();

    // LiveData để giữ chi tiết xe
    public final MutableLiveData<TemplateVehicle> vehicleDetails = new MutableLiveData<>();

    @Inject
    public VehicleDetailViewModel(VehicleRepository repository) {
        this.repository = repository;
    }

    public void loadVehicleDetails(String vehicleId) {
        loading.setValue(true);
        repository.getVehicleById(vehicleId).enqueue(new Callback<ApiEnvelope<TemplateVehicle>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<TemplateVehicle>> call, Response<ApiEnvelope<TemplateVehicle>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    vehicleDetails.postValue(response.body().result);
                } else {
                    error.postValue("Lỗi tải dữ liệu chi tiết xe.");
                }
                loading.postValue(false);
            }

            @Override
            public void onFailure(Call<ApiEnvelope<TemplateVehicle>> call, Throwable t) {
                error.postValue(t.getMessage());
                loading.postValue(false);
            }
        });
    }
}
