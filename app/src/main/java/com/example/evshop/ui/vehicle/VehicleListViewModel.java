package com.example.evshop.ui.vehicle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateResult;
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

        // <<< BƯỚC 2: SỬA KIỂU DỮ LIỆU CỦA CALL VÀ CALLBACK >>>
        apiService.getAllTemplateVehicles().enqueue(new Callback<ApiEnvelope<TemplateResult>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Response<ApiEnvelope<TemplateResult>> response) {
                _loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiEnvelope<TemplateResult> apiResponse = response.body();

                    // <<< BƯỚC 3: SỬA LOGIC LẤY DỮ LIỆU >>>
                    // Dữ liệu bây giờ nằm trong result.getData()
                    if (apiResponse.isSuccess && apiResponse.result != null && apiResponse.result.getData() != null) {
                        _vehicles.setValue(apiResponse.result.getData());
                    } else {
                        // Xử lý lỗi từ API
                        String message = apiResponse.message != null ? apiResponse.message : "Không có dữ liệu";
                        _error.setValue("Lỗi tải dữ liệu: " + message);
                    }
                } else {
                    // Xử lý lỗi HTTP
                    _error.setValue("Lỗi tải dữ liệu: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Throwable t) {
                _loading.setValue(false);
                _error.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}
