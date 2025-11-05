package com.example.evshop.data.repository;

import com.example.evshop.data.ApiService;
import com.example.evshop.data.network.requests.CreateTemplateVehicleRequest;
// *** SỬA LẠI: Import đúng các lớp Request và Response đang được sử dụng ***
import com.example.evshop.data.network.requests.GetUploadUrlRequest;
import com.example.evshop.data.network.responses.UploadUrlResponse;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.Color;
import com.example.evshop.domain.models.Version;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

@Singleton
public class VehicleAdminRepository {

    private final ApiService authApiService;
    private final ApiService publicApiService;

    @Inject
    public VehicleAdminRepository(
            @Named("AuthApiService") ApiService authApiService,
            @Named("PublicApiService") ApiService publicApiService
    ) {
        this.authApiService = authApiService;
        this.publicApiService = publicApiService;
    }

    // Các hàm getVersions, getColors đã đúng (giữ nguyên)
    public Call<ApiEnvelope<List<Version>>> getVersions() {
        return authApiService.getVersions();
    }

    public Call<ApiEnvelope<List<Color>>> getColors() {
        return authApiService.getColors();
    }


    // =================================================================
    // ***        QUY TRÌNH UPLOAD 3 BƯỚC - ĐÃ ĐỒNG BỘ               ***
    // =================================================================

    /**
     * BƯỚC 1: Lấy URL. Hàm này bây giờ sẽ khớp với ApiService và ViewModel.
     */
    public Call<UploadUrlResponse> getUploadUrl(GetUploadUrlRequest request) {
        // Gọi bằng authApiService
        return authApiService.getUploadUrl(request);
    }

    /**
     * BƯỚC 2: Upload lên S3. Hàm này đã đúng (giữ nguyên).
     */
    public Call<ResponseBody> uploadImageToS3(String uploadUrl, RequestBody imageBody) {
        // Gọi bằng publicApiService để không dính token
        return publicApiService.uploadImageToS3(uploadUrl, imageBody);
    }

    /**
     * BƯỚC 3: Tạo template. Hàm này đã đúng (giữ nguyên).
     */
    public Call<ApiEnvelope<Boolean>> createTemplateVehicle(CreateTemplateVehicleRequest request) {
        // Gọi bằng authApiService
        return authApiService.createTemplateVehicle(request);
    }
}
