// File: ApiService.java
package com.example.evshop.data;

import com.example.evshop.domain.models.Version;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;
import com.example.evshop.domain.models.TemplateVehicle;
// *** BƯỚC 1: THÊM IMPORT CHO LỚP VersionDetails MÀ BẠN ĐÃ TẠO ***
import com.example.evshop.domain.models.VersionDetails;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
        @Headers("Accept: application/json")
        @POST("api/Auth/login-user")
        Call<ApiEnvelope<LoginResult>> login(@Body LoginRequest body);

        //---Vehicle----
        @GET("api/EVTemplate/Get-all-template-vehicles")
        Call<ApiEnvelope<List<TemplateVehicle>>> getAllTemplateVehicles();

        // API để lấy thông tin chi tiết của một phiên bản xe bằng ID
        // *** BƯỚC 2: SỬ DỤNG LỚP VersionDetails ĐÃ ĐƯỢC IMPORT ***
        @GET("api/ElectricVehicleVersion/get-version-by-id/{versionId}")
        Call<ApiEnvelope<VersionDetails>> getVersionDetails(
                @Path("versionId") String versionId
        );
}
