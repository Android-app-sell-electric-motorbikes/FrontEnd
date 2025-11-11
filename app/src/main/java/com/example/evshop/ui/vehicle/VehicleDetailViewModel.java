package com.example.evshop.ui.vehicle;

import android.util.Log; // *** THÊM IMPORT NÀY ***
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.ApiService;
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

    private final ApiService apiService;
    private static final String TAG = "VehicleDetailVM"; // Tag để debug

    public final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    public final MutableLiveData<String> error = new MutableLiveData<>();

    private final MutableLiveData<TemplateVehicle> _vehicleDetails = new MutableLiveData<>();
    public LiveData<TemplateVehicle> vehicleDetails = _vehicleDetails;

    // *** BƯỚC 1: KHAI BÁO LIVE DATA CHO THÔNG SỐ KỸ THUẬT ***
    private final MutableLiveData<VersionDetails> _versionSpecs = new MutableLiveData<>();
    public LiveData<VersionDetails> versionSpecs = _versionSpecs;

    @Inject
    public VehicleDetailViewModel(ApiService apiService) {
        this.apiService = apiService;
    }

    // *** BƯỚC 2: SỬA LẠI TOÀN BỘ HÀM NÀY ĐỂ GỌI 2 API ***
    public void loadVehicleDetails(String vehicleId) {
        loading.setValue(true);
        error.setValue(null);
        Log.d(TAG, "===> Bắt đầu chuỗi API cho vehicleId: " + vehicleId);

        // --- CUỘC GỌI API THỨ NHẤT: LẤY THÔNG TIN CHUNG ---
        apiService.getVehicleById(vehicleId).enqueue(new Callback<ApiEnvelope<TemplateVehicle>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<TemplateVehicle>> call, @NonNull Response<ApiEnvelope<TemplateVehicle>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    TemplateVehicle vehicle = response.body().result;
                    _vehicleDetails.postValue(vehicle);
                    Log.d(TAG, "[API 1] Lấy thông tin chung thành công. Tên xe: " + (vehicle != null ? vehicle.getDescription() : "null"));

                    // KIỂM TRA NẾU CÓ THÔNG TIN VERSION ĐỂ GỌI API THỨ HAI
                    if (vehicle != null && vehicle.getVersion() != null && vehicle.getVersion().getVersionId() != null) {
                        String versionId = vehicle.getVersion().getVersionId();
                        Log.d(TAG, "[API 1] Đã có versionId: " + versionId + ". ==> Chuẩn bị gọi API 2.");
                        // Gọi hàm để lấy thông số kỹ thuật
                        loadVersionSpecs(versionId);
                    } else {
                        Log.e(TAG, "[API 1] LỖI LOGIC: Không tìm thấy 'versionId' trong kết quả trả về. Không thể gọi API 2.");
                        loading.postValue(false); // Kết thúc loading nếu không có versionId
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().message : "Lỗi " + response.code();
                    error.postValue("Lỗi tải thông tin xe: " + errorMsg);
                    Log.e(TAG, "[API 1] Lỗi khi lấy thông tin chung: " + errorMsg);
                    loading.postValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<TemplateVehicle>> call, @NonNull Throwable t) {
                error.postValue("Lỗi mạng (API 1): " + t.getMessage());
                Log.e(TAG, "[API 1] Lỗi mạng khi lấy thông tin chung: ", t);
                loading.postValue(false);
            }
        });
    }

    // *** BƯỚC 3: TẠO HÀM MỚI ĐỂ LẤY THÔNG SỐ KỸ THUẬT ***
    private void loadVersionSpecs(String versionId) {
        Log.d(TAG, "===> Thực hiện gọi API 2: getVersionDetails với ID: " + versionId);
        // --- CUỘC GỌI API THỨ HAI: LẤY THÔNG SỐ KỸ THUẬT ---
        apiService.getVersionDetails(versionId).enqueue(new Callback<ApiEnvelope<VersionDetails>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<VersionDetails>> call, @NonNull Response<ApiEnvelope<VersionDetails>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    _versionSpecs.postValue(response.body().result);
                    Log.d(TAG, "[API 2] Lấy thông số kỹ thuật thành công!");
                } else {
                    String errorMsg = response.body() != null ? response.body().message : "Lỗi " + response.code();
                    error.postValue("Lỗi tải thông số kỹ thuật: " + errorMsg);
                    Log.e(TAG, "[API 2] Lỗi khi lấy thông số kỹ thuật: " + errorMsg);
                }
                // Đặt loading = false sau khi cuộc gọi API cuối cùng hoàn tất
                loading.postValue(false);
                Log.d(TAG, "===> Hoàn tất chuỗi API.");
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<VersionDetails>> call, @NonNull Throwable t) {
                error.postValue("Lỗi mạng (API 2): " + t.getMessage());
                Log.e(TAG, "[API 2] Lỗi mạng khi lấy thông số kỹ thuật: ", t);
                loading.postValue(false);
            }
        });
    }
}
