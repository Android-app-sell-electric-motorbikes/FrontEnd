package com.example.evshop.data.repository;

import com.example.evshop.data.ApiService;
import com.example.evshop.data.network.requests.CreateTemplateVehicleRequest;
import com.example.evshop.data.network.requests.GetUploadUrlRequest;
import com.example.evshop.data.network.response.UploadUrlResponse;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.Color;
import com.example.evshop.domain.models.InventoryResult; // **THÊM IMPORT**
import com.example.evshop.domain.models.Version;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

@Singleton
public class VehicleAdminRepository {

    private final ApiService apiService;

    @Inject
    public VehicleAdminRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // ** SỬA LẠI KIỂU DỮ LIỆU TRẢ VỀ **
    public Call<ApiEnvelope<InventoryResult>> getInventory() {
        return apiService.getInventory();
    }

    public Call<ApiEnvelope<List<Version>>> getVersions() {
        return apiService.getVersions();
    }

    public Call<ApiEnvelope<List<Color>>> getColors() {
        return apiService.getColors();
    }

    public Call<UploadUrlResponse> getUploadUrl(GetUploadUrlRequest request) {
        return apiService.getUploadUrl(request);
    }

    public Call<ResponseBody> uploadImageToS3(String uploadUrl, RequestBody imageBody) {
        return apiService.uploadImageToS3(uploadUrl, imageBody);
    }

    public Call<ApiEnvelope<Boolean>> createTemplateVehicle(CreateTemplateVehicleRequest request) {
        return apiService.createTemplateVehicle(request);
    }
}
