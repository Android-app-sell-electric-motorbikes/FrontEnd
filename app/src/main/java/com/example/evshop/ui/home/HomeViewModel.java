package com.example.evshop.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.repository.VehicleRepository;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateResult;
import com.example.evshop.domain.models.TemplateVehicle;

import java.util.ArrayList;
import java.util.List;
// *** BƯỚC 1: THÊM IMPORT ĐỂ TẠO SỐ NGẪU NHIÊN ***
import java.util.concurrent.ThreadLocalRandom;

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
        // Gọi refresh() ngay khi ViewModel được tạo để tải dữ liệu
        refresh();
    }

    public void refresh() {
        loading.setValue(true);
        error.setValue(false);

        vehicleRepository.getAllTemplateVehicles(1, 10, null, null, null, true)
                .enqueue(new Callback<ApiEnvelope<TemplateResult>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Response<ApiEnvelope<TemplateResult>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiEnvelope<TemplateResult> apiResponse = response.body();

                            if (apiResponse.isSuccess && apiResponse.result != null && apiResponse.result.getData() != null) {

                                List<TemplateVehicle> allVehicles = apiResponse.result.getData();

                                // *** BƯỚC 2: THÊM LOGIC GÁN RATING VÀO ĐÂY ***
                                for (TemplateVehicle vehicle : allVehicles) {
                                    // Tạo rating ngẫu nhiên từ 3.0 đến 5.0
                                    double randomRating = 3.0 + ThreadLocalRandom.current().nextDouble(0, 2.01);
                                    vehicle.setRating(randomRating);
                                }
                                // *** KẾT THÚC PHẦN THÊM MỚI ***

                                // Lấy 4 xe đầu tiên để làm "Xe nổi bật" (logic này không đổi)
                                List<TemplateVehicle> featuredList = new ArrayList<>();
                                for (int i = 0; i < Math.min(allVehicles.size(), FEATURED_VEHICLES_COUNT); i++) {
                                    featuredList.add(allVehicles.get(i));
                                }
                                _featuredVehicles.postValue(featuredList);

                            } else {
                                handleError();
                            }
                        } else {
                            handleError();
                        }
                        loading.postValue(false);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiEnvelope<TemplateResult>> call, @NonNull Throwable t) {
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
