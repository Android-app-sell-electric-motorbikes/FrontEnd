package com.example.evshop.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.repository.VehicleRepository;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateResult; // <<< BƯỚC 1: THÊM IMPORT MỚI
import com.example.evshop.domain.models.TemplateVehicle;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final VehicleRepository vehicleRepository;

    private final MutableLiveData<List<TemplateVehicle>> _featuredVehicles = new MutableLiveData<>();
    public LiveData<List<TemplateVehicle>> getFeaturedVehicles() {
        return _featuredVehicles;
    }

    private static final int FEATURED_VEHICLES_COUNT = 4;

    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> error = new MutableLiveData<>(false);

    @Inject
    public HomeViewModel(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public void refresh() {
        loading.setValue(true);
        error.setValue(false);

        // BƯỚC 2: SỬA KIỂU DỮ LIỆU CỦA CALL VÀ CALLBACK
        // Kiểu dữ liệu bây giờ là ApiEnvelope<TemplateResult>
        vehicleRepository.getAllTemplateVehicles(1,10,null,null, null,true).enqueue(new Callback<ApiEnvelope<TemplateResult>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Response<ApiEnvelope<TemplateResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiEnvelope<TemplateResult> apiResponse = response.body();

                    // BƯỚC 3: SỬA LOGIC LẤY DỮ LIỆU
                    // Dữ liệu thực sự nằm trong apiResponse.getResult().getData()
                    if (apiResponse.isSuccess && apiResponse.result != null && apiResponse.result.getData() != null) {

                        // Lấy danh sách đầy đủ từ API
                        List<TemplateVehicle> allVehicles = apiResponse.result.getData();

                        // Chỉ lấy 4 xe đầu tiên để làm "Xe nổi bật"
                        List<TemplateVehicle> featuredList = new ArrayList<>();
                        for (int i = 0; i < Math.min(allVehicles.size(), FEATURED_VEHICLES_COUNT); i++) {
                            featuredList.add(allVehicles.get(i));
                        }
                        _featuredVehicles.postValue(featuredList);

                    } else {
                        // Xử lý trường hợp API trả về isSuccess = false hoặc result rỗng
                        handleError();
                    }
                } else {
                    // Xử lý lỗi HTTP (ví dụ 404, 500)
                    handleError();
                }
                loading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Throwable t) {
                // Xử lý lỗi kết nối mạng
                handleError();
                loading.postValue(false);
            }
        });
    }

    private void handleError() {
        _featuredVehicles.postValue(new ArrayList<>());
        error.postValue(true);
    }
}