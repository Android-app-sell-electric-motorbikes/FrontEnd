package com.example.evshop.ui.home;

// Thêm các import này để các phương thức mới hoạt động
import android.util.Log;
import androidx.annotation.NonNull;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.repository.VehicleRepository;
import com.example.evshop.domain.models.ApiEnvelope;
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
    public final LiveData<List<TemplateVehicle>> featuredVehicles = _featuredVehicles;
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

        vehicleRepository.getAllTemplateVehicles(new Callback<ApiEnvelope<List<TemplateVehicle>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiEnvelope<List<TemplateVehicle>>> call, @NonNull Response<ApiEnvelope<List<TemplateVehicle>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    List<TemplateVehicle> allVehicles = response.body().result;

                    List<TemplateVehicle> featuredList = new ArrayList<>();
                    if (allVehicles != null) {
                        for (int i = 0; i < Math.min(allVehicles.size(), FEATURED_VEHICLES_COUNT); i++) {
                            featuredList.add(allVehicles.get(i));
                        }
                    }
                    _featuredVehicles.postValue(featuredList);
                } else {
                    handleError();
                }
                loading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<ApiEnvelope<List<TemplateVehicle>>> call, @NonNull Throwable t) {
                handleError();
                loading.postValue(false);
            }
        });
    }

    private void handleError() {
        _featuredVehicles.postValue(new ArrayList<>());
        error.postValue(true);
    }

    // =========================================================================
    // HÃY THÊM CÁC PHƯƠNG THỨC CÒN THIẾU VÀO ĐÚNG VỊ TRÍ NÀY
    // =========================================================================

    public void setQuery(String query) {
        // TODO: Triển khai logic tìm kiếm sau.
        Log.d("HomeViewModel", "Đã nhận truy vấn tìm kiếm: " + query);
    }

    public void setCategory(String category) {
        // TODO: Triển khai logic lọc theo danh mục sau.
        Log.d("HomeViewModel", "Đã nhận danh mục: " + category);
    }

    // Đây là lớp giả để code có thể build được, bạn đừng xóa nó
    public static class Filters {
        public enum Sort { POPULAR, PRICE_ASC, PRICE_DESC, RATING }
        public Sort sort = Sort.POPULAR;
        public List<String> brands = new ArrayList<>();
        public long maxPriceVnd = -1;
        public float minRating = 0;
    }

    public void applyFilters(Filters filters) {
        // TODO: Triển khai logic áp dụng bộ lọc sau.
        Log.d("HomeViewModel", "Đang áp dụng bộ lọc...");
    }

} // <-- Dấu ngoặc nhọn đóng của lớp HomeViewModel
