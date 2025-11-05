package com.example.evshop.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evshop.data.HomeRepository;
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
    
    // Filters và category cho products (nếu cần)
    public HomeRepository.Filters filters = new HomeRepository.Filters();
    private String category = "Tất cả";
    private String query = "";

    // Chỉ cần LiveData cho xe nổi bật
    private final MutableLiveData<List<TemplateVehicle>> _featuredVehicles = new MutableLiveData<>();
    public LiveData<List<TemplateVehicle>> getFeaturedVehicles() { // Đổi tên lại cho rõ nghĩa
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

        // *** SỬA LẠI CÁCH GỌI Ở ĐÂY ***
        // Gọi repository.getAllTemplateVehicles() trước, sau đó mới .enqueue()
        vehicleRepository.getAllTemplateVehicles().enqueue(new Callback<ApiEnvelope<List<TemplateVehicle>>>() {
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
    
    /**
     * Áp dụng filters cho products.
     * Lưu filters để sử dụng khi load products (nếu được implement sau).
     */
    public void applyFilters(HomeRepository.Filters f) {
        this.filters = (f != null) ? f : new HomeRepository.Filters();
        // TODO: Implement product loading with filters when needed
        // refresh(); // Refresh data với filters mới
    }
    
    /**
     * Set category để filter products.
     * Lưu category và refresh data với category mới.
     */
    public void setCategory(String cat) {
        this.category = (cat != null) ? cat : "Tất cả";
        // TODO: Implement product loading with category when needed
        // refresh(); // Refresh data với category mới
    }
    
    /**
     * Set query để search products.
     * Lưu query và refresh data với query mới.
     */
    public void setQuery(String q) {
        this.query = (q != null) ? q : "";
        // TODO: Implement product loading with query when needed
        // refresh(); // Refresh data với query mới
    }
}
