package com.example.evshop.data.repository;

import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateVehicle;
// *** THÊM IMPORT CHO TEMPLATERESULT ***
import com.example.evshop.domain.models.TemplateResult;
import com.example.evshop.domain.models.VersionDetails;

import java.util.List;

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
    // ***           SỬA LẠI KIỂU TRẢ VỀ Ở ĐÂY             ***
    // ========================================================
    /**
     * Phương thức này bây giờ trả về Call<ApiEnvelope<TemplateResult>>
     * để đồng bộ với ApiService.
     */
    public Call<ApiEnvelope<TemplateResult>> getAllTemplateVehicles(int pageNumber, int pageSize, String searchTerm) {
        // Bây giờ kiểu dữ liệu đã khớp
        return apiService.getAllTemplateVehicles(pageNumber, pageSize, searchTerm);
    }

    public Call<ApiEnvelope<TemplateVehicle>> getVehicleById(String vehicleId) {
        return apiService.getVehicleById(vehicleId);
    }

    public Call<ApiEnvelope<VersionDetails>> getVersionDetails(String versionId) {
        return apiService.getVersionDetails(versionId);
    }
}
