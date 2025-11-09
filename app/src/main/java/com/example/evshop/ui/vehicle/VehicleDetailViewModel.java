// File: ui/vehicle/VehicleDetailViewModel.java
package com.example.evshop.ui.vehicle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.evshop.data.repository.VehicleRepository;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.VersionDetails; // << THÊM IMPORT MỚI

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class VehicleDetailViewModel extends ViewModel {

    private final VehicleRepository repository;

    public final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public final MutableLiveData<String> error = new MutableLiveData<>();

    // LiveData cho thông tin chung (giá, màu, ảnh...)
    private final MutableLiveData<TemplateVehicle> _templateVehicle = new MutableLiveData<>();
    public LiveData<TemplateVehicle> getTemplateVehicle() { return _templateVehicle; }

    // LiveData cho thông số kỹ thuật chi tiết
    private final MutableLiveData<VersionDetails> _versionDetails = new MutableLiveData<>();
    public LiveData<VersionDetails> getVersionDetails() { return _versionDetails; }

    @Inject
    public VehicleDetailViewModel(VehicleRepository repository) {
        this.repository = repository;
    }

    public void loadVehicleDetails(String vehicleId) {
        loading.setValue(true);
        error.setValue(null); // Reset lỗi cũ

        // BƯỚC 1: Gọi API lấy thông tin chung (TemplateVehicle)
        repository.getVehicleById(vehicleId).enqueue(new Callback<ApiEnvelope<TemplateVehicle>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<TemplateVehicle>> call, @NonNull Response<ApiEnvelope<TemplateVehicle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiEnvelope<TemplateVehicle> apiResponse = response.body();
                    if (apiResponse.isSuccess && apiResponse.message != null) {
                        TemplateVehicle template = apiResponse.result;
                        _templateVehicle.postValue(template);

                        // Lấy versionId từ kết quả của Bước 1
                        if (template.getVersion() != null && template.getVersion().getVersionId() != null) {
                            // BƯỚC 2: Dùng versionId để gọi API lấy thông số kỹ thuật chi tiết
                            loadSpecifications(template.getVersion().getVersionId());
                        } else {
                            error.postValue("Lỗi: Template không có version ID.");
                            loading.postValue(false);
                        }
                    } else {
                        error.postValue("API Lỗi: " + (apiResponse.message != null ? apiResponse.message : "Không có dữ liệu Template"));
                        loading.postValue(false);
                    }
                } else {
                    error.postValue("Lỗi HTTP: " + response.code());
                    loading.postValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<TemplateVehicle>> call, @NonNull Throwable t) {
                error.postValue("Lỗi mạng (Template): " + t.getMessage());
                loading.postValue(false);
            }
        });
    }

    // Hàm riêng để gọi API lấy thông số kỹ thuật
    private void loadSpecifications(String versionId) {
        repository.getVersionDetails(versionId).enqueue(new Callback<ApiEnvelope<VersionDetails>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<VersionDetails>> call, @NonNull Response<ApiEnvelope<VersionDetails>> response) {
                loading.postValue(false); // Kết thúc loading ở đây
                if (response.isSuccessful() && response.body() != null) {
                    ApiEnvelope<VersionDetails> apiResponse = response.body();
                    if (apiResponse.isSuccess && apiResponse.result != null) {
                        // Gửi dữ liệu thông số kỹ thuật cho View
                        _versionDetails.postValue(apiResponse.result);
                    } else {
                        error.postValue("API Lỗi: " + (apiResponse.message != null ? apiResponse.message : "Không có thông số kỹ thuật"));
                    }
                } else {
                    error.postValue("Lỗi HTTP (Thông số): " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<VersionDetails>> call, @NonNull Throwable t) {
                error.postValue("Lỗi mạng (Thông số): " + t.getMessage());
                loading.postValue(false);
            }
        });
    }
}

