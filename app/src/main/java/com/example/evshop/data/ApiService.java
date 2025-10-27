// File: ApiService.java
package com.example.evshop.data;

import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;
import com.example.evshop.domain.models.TemplateVehicle;
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
        // API lấy TẤT CẢ các xe, dùng cho màn hình chính
        @GET("api/EVTemplate/Get-all-template-vehicles")
        Call<ApiEnvelope<List<TemplateVehicle>>> getAllTemplateVehicles();

        /**
         * =========================================================================
         *  SỬA LẠI API LẤY CHI TIẾT XE THEO ĐÚNG LINK BẠN CUNG CẤP
         * =========================================================================
         * - Đường dẫn chính xác là "api/EVTemplate/get-template-by-id/{id}"
         */
        @GET("api/EVTemplate/get-template-by-id/{id}")
        Call<ApiEnvelope<TemplateVehicle>> getVehicleById(@Path("id") String vehicleId);

        // API để lấy thông tin chi tiết của một phiên bản xe (có thể dùng sau này)
        @GET("api/ElectricVehicleVersion/get-version-by-id/{versionId}")
        Call<ApiEnvelope<VersionDetails>> getVersionDetails(
                @Path("versionId") String versionId
        );
}
