package com.example.evshop.data;

import com.example.evshop.data.network.requests.CreateTemplateVehicleRequest;
import com.example.evshop.data.network.requests.GetUploadUrlRequest;
import com.example.evshop.data.network.response.UploadUrlResponse;
import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.Color;
import com.example.evshop.domain.models.InventoryResult;
import com.example.evshop.domain.models.LoginRequest;
import com.example.evshop.domain.models.LoginResult;
// SỬA 1: Thêm import cho RegisterRequest và UserData
import com.example.evshop.domain.models.RegisterRequest;
import com.example.evshop.domain.models.TemplateResult;
import com.example.evshop.domain.models.TemplateVehicle;
import com.example.evshop.domain.models.TransactionResult;
import com.example.evshop.domain.models.UserData;
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
    @Headers("Accept: application/json")
    @POST("api/Auth/login-user")
    Call<ApiEnvelope<LoginResult>> login(@Body LoginRequest body);

    // =========================================================
    // SỬA 2: Thêm phương thức `register` vào đây
    // =========================================================
    /**
     * Gửi yêu cầu đăng ký một tài khoản người dùng mới.
     * @param request Đối tượng chứa thông tin đăng ký (username, password, email, ...).
     * @return Một đối tượng ApiEnvelope chứa thông tin người dùng vừa được tạo.
     */
    @Headers("Content-Type: application/json")
    @POST("api/Auth/register") // **Lưu ý:** Endpoint này là giả định, bạn cần thay bằng endpoint đúng của API
    Call<ApiEnvelope<UserData>> register(@Body RegisterRequest request);
    // =========================================================


    //---Vehicle----
    @GET("api/EVTemplate/Get-all-template-vehicles")
    Call<ApiEnvelope<TemplateResult>> getAllTemplateVehicles(
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("search") String search,
            @Query("minPrice") Long minPrice,
            @Query("maxPrice") Long maxPrice,
            @Query("sortByPriceAsc") Boolean sortByPriceAsc
    );

    @GET("api/EVTemplate/get-template-by-id/{id}")
    Call<ApiEnvelope<TemplateVehicle>> getVehicleById(@Path("id") String vehicleId);


    // =========================================================
    // ***           CÁC API MỚI CHO TRANG ADMIN             ***
    // =========================================================

    @GET("api/ElectricVehicleVersion/get-all-versions")
    Call<ApiEnvelope<List<Version>>> getVersions();

    @GET("api/ElectricVehicleColor/get-all-colors")
    Call<ApiEnvelope<List<Color>>> getColors();

    @Headers("Content-Type: application/json")
    @POST("api/ElectricVehicle/upload-file-url-electric-vehicle")
    Call<UploadUrlResponse> getUploadUrl(@Body GetUploadUrlRequest request);

    @Headers("Accept: application/json")
    @PUT
    Call<ResponseBody> uploadImageToS3(
            @Url String url,
            @Body RequestBody imageBody
    );

    @GET("api/ElectricVehicle/get-evc-inventory")
    Call<ApiEnvelope<InventoryResult>> getInventory();

    @POST("api/EVTemplate/create-template-vehicles")
    Call<ApiEnvelope<Boolean>> createTemplateVehicle(@Body CreateTemplateVehicleRequest request);

    @GET("api/ElectricVehicleVersion/get-version-by-id/{versionId}")
    Call<ApiEnvelope<VersionDetails>> getVersionDetails(
            @Path("versionId") String versionId
    );


    // =========================================================
    // ***           API MỚI CHO THANH TOÁN VÀ GIAO DỊCH       ***
    // =========================================================
    @POST("api/Payment/create-vnpay-payment")
    Call<VnpayResponse> createVnpayPayment(@Query("amount") long amount);

    @GET("api/Payment/get-all-transactions")
    Call<ApiEnvelope<TransactionResult>> getAllTransactions(
            @Query("page") int page,
            @Query("pageSize") int pageSize
    );
    // =========================================================
}
