// File: VehicleListViewModel.java
package com.example.evshop.ui.vehicle;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.VersionDetails;

import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class VehicleListViewModel extends ViewModel {

    private final ApiService apiService;

    private final MutableLiveData<List<TemplateVehicle>> _vehicles = new MutableLiveData<>();
    public LiveData<List<TemplateVehicle>> getVehicles() { return _vehicles; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<VehicleDetailResult> _vehicleDetails = new MutableLiveData<>();
    public LiveData<VehicleDetailResult> getVehicleDetails() { return _vehicleDetails; }

    @Inject
    public VehicleListViewModel(ApiService apiService) {
        this.apiService = apiService;
        loadInitialVehicles();
    }

    public void loadInitialVehicles() {
        _isLoading.setValue(true);
        apiService.getAllTemplateVehicles().enqueue(new Callback<ApiEnvelope<List<TemplateVehicle>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<TemplateVehicle>>> call, Response<ApiEnvelope<List<TemplateVehicle>>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    _vehicles.postValue(response.body().result);
                } else {
                    Log.e("VEHICLE_DEBUG", "ViewModel: Lỗi khi tải danh sách xe. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<TemplateVehicle>>> call, Throwable t) {
                _isLoading.setValue(false);
                Log.e("VEHICLE_DEBUG", "ViewModel: Lỗi mạng khi tải danh sách xe. Message: " + t.getMessage());
            }
        });
    }

    public void fetchVehicleDetails(int position, String versionId) {
        Log.d("VEHICLE_DEBUG", "ViewModel: Bắt đầu gọi API getVersionDetails với versionId: " + versionId);

        apiService.getVersionDetails(versionId).enqueue(new Callback<ApiEnvelope<VersionDetails>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<VersionDetails>> call, Response<ApiEnvelope<VersionDetails>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    Log.d("VEHICLE_DEBUG", "ViewModel: API chi tiết trả về thành công. Posting kết quả cho LiveData.");
                    _vehicleDetails.postValue(new VehicleDetailResult(position, response.body().result));
                } else {
                    String errorMsg = response.message();
                    int errorCode = response.code();
                    Log.e("VEHICLE_DEBUG", "ViewModel: API chi tiết trả về lỗi. Code: " + errorCode + ", Message: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<VersionDetails>> call, Throwable t) {
                Log.e("VEHICLE_DEBUG", "ViewModel: Lỗi mạng hoặc Retrofit khi gọi API chi tiết. Message: " + t.getMessage());
            }
        });
    }

    // Lớp nội bộ để gói kết quả trả về cho LiveData
    public static class VehicleDetailResult {
        private final int position;
        private final VersionDetails details;

        public VehicleDetailResult(int position, VersionDetails details) {
            this.position = position;
            this.details = details;
        }

        public int getPosition() { return position; }
        public VersionDetails getDetails() { return details; }
    }
}
