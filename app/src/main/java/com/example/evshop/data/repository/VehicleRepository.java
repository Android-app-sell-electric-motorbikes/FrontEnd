// File: D:/PRM391/FrontEnd/app/src/main/java/com/example/evshop/data/repository/VehicleRepository.java

package com.example.evshop.data.repository;

import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateResult;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.VersionDetails;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class VehicleRepository {

    private final ApiService apiService;

    @Inject
    public VehicleRepository(@Named("AuthApiService") ApiService apiService) {
        this.apiService = apiService;
    }

    // ========================================================
    // ***           SỬA LẠI CHỮ KÝ PHƯƠNG THỨC Ở ĐÂY       ***
    // ========================================================
    /**
     * Phương thức này bây giờ nhận đầy đủ các tham số để truyền xuống ApiService.
     */
    public Call<ApiEnvelope<TemplateResult>> getAllTemplateVehicles(
            int pageNumber,
            int pageSize,
            String searchTerm,
            Long minPrice,
            Long maxPrice,
            Boolean sortByPriceAsc
    ) {
        // Bây giờ, tất cả tham số đều hợp lệ và được truyền xuống lớp service
        return apiService.getAllTemplateVehicles(
                pageNumber, pageSize, searchTerm, minPrice, maxPrice, sortByPriceAsc
        );
    }

    public Call<ApiEnvelope<TemplateVehicle>> getVehicleById(String vehicleId) {
        return apiService.getVehicleById(vehicleId);
    }

    public Call<ApiEnvelope<VersionDetails>> getVersionDetails(String versionId) {
        return apiService.getVersionDetails(versionId);
    }
}
