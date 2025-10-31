package com.example.evshop.ui.vehicle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.repository.VehicleRepository;
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

    private final VehicleRepository repository;

    private final MutableLiveData<TemplateVehicle> _vehicleTemplate = new MutableLiveData<>();
    public final LiveData<TemplateVehicle> vehicleTemplate = _vehicleTemplate;

    private final MutableLiveData<VersionDetails> _versionDetails = new MutableLiveData<>();
    public final LiveData<VersionDetails> versionDetails = _versionDetails;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public final LiveData<Boolean> loading = _loading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public VehicleDetailViewModel(VehicleRepository repository) {
        this.repository = repository;
    }

    /**
     * BƯỚC 1: Tải thông tin template chung bằng templateId.
     */
    public void loadVehicleTemplate(String templateId) {
        _loading.setValue(true);

        // *** SỬA LẠI CÁCH GỌI Ở ĐÂY ***
        // Gọi repository.getVehicleById(templateId) trước, sau đó mới .enqueue()
        repository.getVehicleById(templateId)
                .enqueue(new Callback<ApiEnvelope<TemplateVehicle>>() {
                    @Override
                    public void onResponse(Call<ApiEnvelope<TemplateVehicle>> call, Response<ApiEnvelope<TemplateVehicle>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                            TemplateVehicle template = response.body().result;
                            _vehicleTemplate.setValue(template);

                            // Sau khi có template, nếu có versionId, thực hiện BƯỚC 2
                            if (template != null && template.getVersion() != null && template.getVersion().getVersionId() != null) {
                                loadVersionDetails(template.getVersion().getVersionId());
                            } else {
                                _loading.setValue(false);
                                _error.setValue("Lỗi: Template không chứa thông tin phiên bản (versionId).");
                            }
                        } else {
                            _loading.setValue(false);
                            _error.setValue("Lỗi tải thông tin xe: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<TemplateVehicle>> call, Throwable t) {
                        _loading.setValue(false);
                        _error.setValue("Lỗi mạng (template): " + t.getMessage());
                    }
                });
    }

    /**
     * BƯỚC 2: Tải thông số kỹ thuật bằng versionId.
     */
    private void loadVersionDetails(String versionId) {
        // *** SỬA LẠI CÁCH GỌI Ở ĐÂY ***
        repository.getVersionDetails(versionId)
                .enqueue(new Callback<ApiEnvelope<VersionDetails>>() {
                    @Override
                    public void onResponse(Call<ApiEnvelope<VersionDetails>> call, Response<ApiEnvelope<VersionDetails>> response) {
                        _loading.setValue(false); // Kết thúc loading tại đây
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                            _versionDetails.setValue(response.body().result);
                        } else {
                            _error.setValue("Lỗi tải thông số kỹ thuật: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<VersionDetails>> call, Throwable t) {
                        _loading.setValue(false);
                        _error.setValue("Lỗi mạng (version details): " + t.getMessage());
                    }
                });
    }
}
