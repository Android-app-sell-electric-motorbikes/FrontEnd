// File: ApiService.java
package com.example.evshop.data;

import com.example.evshop.data.network.requests.CreateTemplateVehicleRequest;
// *** THÊM IMPORT MỚI ***
import com.example.evshop.data.network.requests.GetUploadUrlRequest;
import com.example.evshop.data.network.response.UploadUrlResponse;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.Color;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.Version;
import com.example.evshop.domain.models.VersionDetails;
import com.example.evshop.domain.models.InventoryItem;

import java.util.List;

// *** THÊM IMPORT MỚI ***
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
// *** THÊM IMPORT MỚI ***
import retrofit2.http.PUT;
import retrofit2.http.Path;
// *** THÊM IMPORT MỚI ***
import retrofit2.http.Url;


public interface ApiService {
        @Headers("Accept: application/json")
        @POST("api/Auth/login-user")
        Call<ApiEnvelope<LoginResult>> login(@Body LoginRequest body);


        //---Vehicle----
        @GET("api/EVTemplate/Get-all-template-vehicles")
        Call<ApiEnvelope<List<TemplateVehicle>>> getAllTemplateVehicles();

        @GET("api/EVTemplate/get-template-by-id/{id}")
        Call<ApiEnvelope<TemplateVehicle>> getVehicleById(@Path("id") String vehicleId);


        // =========================================================
        // ***           CÁC API MỚI CHO TRANG ADMIN             ***
        // =========================================================

        @GET("api/ElectricVehicleVersion/get-all-versions")
        Call<ApiEnvelope<List<Version>>> getVersions();

        @GET("api/ElectricVehicleColor/get-all-colors")
        Call<ApiEnvelope<List<Color>>> getColors();


        // =========================================================================
        // *** STEP 1: API ĐỂ XIN PRESIGNED URL TỪ BACKEND CỦA BẠN               ***
        // =========================================================================
        /**
         * Yêu cầu backend tạo ra một URL để upload file lên.
         * API này KHÔNG gửi file, chỉ xin URL.
         */
        @Headers("Content-Type: application/json")
        @POST("api/ElectricVehicle/upload-file-url-electric-vehicle")
        Call<UploadUrlResponse> getUploadUrl(@Body GetUploadUrlRequest request);

        // =========================================================================
        // *** STEP 2: API ĐỂ UPLOAD FILE LÊN URL CỦA AMAZON S3                  ***
        // =========================================================================

        @Headers("Accept: application/json") // Giữ lại header này cho an toàn
        @PUT
        Call<ResponseBody> uploadImageToS3(
                @Url String url,
                @Body RequestBody imageBody
                // Header Content-Type sẽ được thêm vào trong Repository
        );

        @GET("api/ElectricVehicle/get-evc-inventory")
        Call<ApiEnvelope<List<InventoryItem>>> getInventory();

        // =========================================================================
        // *** STEP 3: API ĐỂ TẠO TEMPLATE (Đã có, giữ nguyên)                     ***
        // =========================================================================
        /**
         * Gửi yêu cầu tạo một mẫu xe mới.
         * Request body bây giờ sẽ chứa `objectKey` thay vì `attachmentId`.
         */
        @POST("api/EVTemplate/create-template-vehicles")
        Call<ApiEnvelope<Boolean>> createTemplateVehicle(@Body CreateTemplateVehicleRequest request);


        // --- API cũ để lấy chi tiết version (giữ lại nếu cần) ---
        @GET("api/ElectricVehicleVersion/get-version-by-id/{versionId}")
        Call<ApiEnvelope<VersionDetails>> getVersionDetails(
                @Path("versionId") String versionId
        );

        // --- HÀM UPLOAD CŨ ĐÃ BỊ XÓA VÌ KHÔNG CÒN SỬ DỤNG ---
}
