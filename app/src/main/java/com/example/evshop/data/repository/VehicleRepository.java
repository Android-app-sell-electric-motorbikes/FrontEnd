package com.example.evshop.data.repository;

import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateResult;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.VersionDetails;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class VehicleRepository {

    private final ApiService apiService;

    @Inject
    public VehicleRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // ** SỬA LẠI: SỬ DỤNG TemplateResult VÀ THÊM CÁC THAM SỐ LỌC **
    public Call<ApiEnvelope<TemplateResult>> getAllTemplateVehicles(
            int page, int pageSize, String search, Long minPrice, Long maxPrice, Boolean sortByPriceAsc) {
        return apiService.getAllTemplateVehicles(page, pageSize, search, minPrice, maxPrice, sortByPriceAsc);
    }

    public Call<ApiEnvelope<TemplateVehicle>> getVehicleById(String vehicleId) {
        return apiService.getVehicleById(vehicleId);
    }

    public Call<ApiEnvelope<VersionDetails>> getVersionDetails(String versionId) {
        return apiService.getVersionDetails(versionId);
    }
}
