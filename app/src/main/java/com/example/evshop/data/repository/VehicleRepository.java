package com.example.evshop.data.repository;

import com.example.evshop.data.ApiService;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.VersionDetails;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;
import retrofit2.Callback;

@Singleton
public class VehicleRepository {
    private final ApiService apiService;

    @Inject
    public VehicleRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Hàm này gọi API để lấy danh sách tất cả các xe.
     */
    public void getAllTemplateVehicles(Callback<ApiEnvelope<List<TemplateVehicle>>> callback) {
        apiService.getAllTemplateVehicles().enqueue(callback);
    }

    /**
     * =======================================================
     * HÀM MỚI ĐƯỢC THÊM VÀO ĐỂ LẤY CHI TIẾT XE
     * =======================================================
     * @param vehicleId ID của xe cần lấy chi tiết.
     * @param callback Callback để xử lý kết quả trả về từ API.
     */
    public void getVehicleById(String vehicleId, Callback<ApiEnvelope<TemplateVehicle>> callback) {
        // Lời gọi đến ApiService đã đúng, chỉ cần truyền callback vào là được
        apiService.getVehicleById(vehicleId).enqueue(callback);
    }
    public void getVersionDetails(String versionId, Callback<ApiEnvelope<VersionDetails>> callback) {
        // Hàm này sẽ gọi đến hàm "getVersionDetails" bạn vừa xác nhận trong ApiService
        apiService.getVersionDetails(versionId).enqueue(callback);
    }
}
