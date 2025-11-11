package com.example.evshop.ui.vehicle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.ApiService; // **THAY ĐỔI IMPORT**
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.VersionDetails;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class VehicleDetailViewModel extends ViewModel {

    private final ApiService apiService; // **SỬ DỤNG ApiService TRỰC TIẾP**

    public final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public final MutableLiveData<String> error = new MutableLiveData<>();

    private final MutableLiveData<TemplateVehicle> _vehicleDetails = new MutableLiveData<>();
    public LiveData<TemplateVehicle> vehicleDetails = _vehicleDetails;

    @Inject
    public VehicleDetailViewModel(ApiService apiService) { // **THAY ĐỔI CONSTRUCTOR**
        this.apiService = apiService;
    }

    public void loadVehicleDetails(String vehicleId) {
        loading.setValue(true);
        error.setValue(null);

        apiService.getVehicleById(vehicleId).enqueue(new Callback<ApiEnvelope<TemplateVehicle>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<TemplateVehicle>> call, @NonNull Response<ApiEnvelope<TemplateVehicle>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    _vehicleDetails.postValue(response.body().result);
                } else {
                    String errorMsg = response.body() != null ? response.body().message : "Lỗi tải thông tin xe.";
                    error.postValue(errorMsg);
                }
                loading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<TemplateVehicle>> call, @NonNull Throwable t) {
                error.postValue("Lỗi mạng: " + t.getMessage());
                loading.postValue(false);
            }
        });
    }
}
