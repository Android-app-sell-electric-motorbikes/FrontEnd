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

    // QUAN TRỌNG: Chỉ giữ lại MỘT LiveData cho TemplateVehicle.
    // Dữ liệu từ VersionDetails sẽ được gộp vào đây.
    private final MutableLiveData<TemplateVehicle> _vehicleDetails = new MutableLiveData<>();
    public final LiveData<TemplateVehicle> vehicleDetails = _vehicleDetails;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public final LiveData<Boolean> loading = _loading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    @Inject
    public VehicleDetailViewModel(VehicleRepository repository) {
        this.repository = repository;
    }

    public void loadVehicleDetails(String templateId) {
        _loading.setValue(true);

        // BƯỚC 1: Gọi API để lấy TemplateVehicle (chứa ảnh, giá, màu và versionId)
        repository.getVehicleById(templateId)
                .enqueue(new Callback<ApiEnvelope<TemplateVehicle>>() {
                    @Override
                    public void onResponse(Call<ApiEnvelope<TemplateVehicle>> call, Response<ApiEnvelope<TemplateVehicle>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                            TemplateVehicle template = response.body().result;

                            // Kiểm tra xem template có tồn tại và có versionId không
                            if (template != null && template.getVersion() != null && template.getVersion().getVersionId() != null) {
                                // BƯỚC 2: Gọi API thứ hai để lấy thông số kỹ thuật
                                loadAndMergeVersionDetails(template, template.getVersion().getVersionId());
                            } else {
                                // Nếu không có versionId, vẫn hiển thị thông tin đã có và kết thúc loading
                                _vehicleDetails.setValue(template);
                                _loading.setValue(false);
                                _error.setValue("Lỗi: Không tìm thấy thông tin phiên bản (versionId).");
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
     * Tải thông số kỹ thuật và GỘP nó vào đối tượng TemplateVehicle đã có.
     */
    private void loadAndMergeVersionDetails(final TemplateVehicle template, String versionId) {
        repository.getVersionDetails(versionId)
                .enqueue(new Callback<ApiEnvelope<VersionDetails>>() {
                    @Override
                    public void onResponse(Call<ApiEnvelope<VersionDetails>> call, Response<ApiEnvelope<VersionDetails>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                            VersionDetails details = response.body().result;

                            // Gộp dữ liệu từ VersionDetails vào đối tượng Version bên trong TemplateVehicle
                            // Điều này yêu cầu bạn phải thêm các trường thông số vào lớp TemplateVehicle.Version
                            // (Bạn đã làm điều này ở các bước trước)
                            TemplateVehicle.Version version = template.getVersion();
                            version.setVersionName(details.getVersionName());
                            version.setDescription(details.getDescription());
                            version.setMotorPower(details.getMotorPower());
                            version.setBatteryCapacity(details.getBatteryCapacity());
                            version.setRangePerCharge(details.getRangePerCharge());
                            version.setTopSpeed(details.getTopSpeed());
                            version.setWeight(details.getWeight());
                            version.setHeight(details.getHeight());
                            version.setProductionYear(details.getProductionYear());

                            // Chỉ cập nhật LiveData MỘT LẦN DUY NHẤT sau khi đã có ĐỦ thông tin
                            _vehicleDetails.setValue(template);

                        } else {
                            // Nếu API thứ 2 lỗi, vẫn hiển thị thông tin từ API 1
                            _vehicleDetails.setValue(template);
                            _error.setValue("Lỗi tải thông số kỹ thuật: " + response.message());
                        }
                        // LUÔN LUÔN kết thúc loading sau khi chuỗi API hoàn thành
                        _loading.setValue(false);
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<VersionDetails>> call, Throwable t) {
                        // Nếu API thứ 2 lỗi, vẫn hiển thị thông tin từ API 1
                        _vehicleDetails.setValue(template);
                        _loading.setValue(false);
                        _error.setValue("Lỗi mạng (version details): " + t.getMessage());
                    }
                });
    }
}
