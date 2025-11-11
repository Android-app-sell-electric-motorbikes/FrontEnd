package com.example.evshop.data;

import com.example.evshop.data.network.requests.CreateTemplateVehicleRequest;
import com.example.evshop.data.network.requests.GetUploadUrlRequest;
import com.example.evshop.data.network.responses.UploadUrlResponse;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.Color;
import com.example.evshop.domain.models.InventoryItem;
import com.example.evshop.domain.models.InventoryResult; // **THÊM IMPORT**
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;
import com.example.evshop.domain.models.RegisterRequest;
import com.example.evshop.domain.models.TemplateResult;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.TransactionResult;
import com.example.evshop.domain.models.Version;
import com.example.evshop.domain.models.VersionDetails;
import com.example.evshop.domain.models.VnpayResponse;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {
    @POST("api/Auth/login-mobile")
    Call<ApiEnvelope<LoginResult>> login(@Body LoginRequest loginRequest);

    @POST("api/Auth/register-mobile")
    Call<ApiEnvelope<String>> register(@Body RegisterRequest registerRequest);

    @POST("api/Payment/create-vnpay-mobile/{amount}")
    Call<VnpayResponse> createVnpayPayment(@Path("amount") long amount);

    @GET("api/Payment/get-all-transactions-mobile")
    Call<ApiEnvelope<TransactionResult>> getAllTransactions(
        @Query("pageNumber") int pageNumber,
        @Query("pageSize") int pageSize
    );

    @GET("api/EVTemplate/Get-all-template-vehicles")
    Call<ApiEnvelope<TemplateResult>> getAllTemplateVehicles(
            @Query("pageNumber") int pageNumber,
            @Query("pageSize") int pageSize,
            @Query("search") String searchQuery,
            @Query("minPrice") Long minPrice,
            @Query("maxPrice") Long maxPrice,
            @Query("sortByPriceAsc") Boolean sortByPriceAsc
    );

    @GET("api/EVTemplate/get-template-by-id/{id}") // << THAY ĐỔI TẠI ĐÂY
    Call<ApiEnvelope<TemplateVehicle>> getVehicleById(@Path("id") String vehicleId);

    @GET("api/ElectricVehicleVersion/get-version-by-id/{versionId}")
    Call<ApiEnvelope<VersionDetails>> getVersionDetails(@Path("versionId") String versionId); // << THAY TỪ VersionDetails THÀNH Version

    @GET("api/ElectricVehicleVersion/get-all-versions")
    Call<ApiEnvelope<List<Version>>> getVersions();

    @GET("api/ElectricVehicleColor/get-all-colors")
    Call<ApiEnvelope<List<Color>>> getColors();

    @Headers("Content-Type: application/json")
    @POST("api/ElectricVehicle/upload-file-url-electric-vehicle") // << THAY ĐỔI TẠI ĐÂY
    Call<UploadUrlResponse> getUploadUrl(@Body GetUploadUrlRequest request);

    @PUT
    Call<ResponseBody> uploadImageToS3(
            @Url String url,
            @Body RequestBody imageBody
    );
    @POST("api/EVTemplate/create-template-vehicles")
    Call<ApiEnvelope<Boolean>> createTemplateVehicle(@Body CreateTemplateVehicleRequest request);

    // ** SỬA LẠI KIỂU DỮ LIỆU TRẢ VỀ **
    @GET("api/ElectricVehicle/get-evc-inventory")
    Call<ApiEnvelope<InventoryResult>> getInventory();
}
