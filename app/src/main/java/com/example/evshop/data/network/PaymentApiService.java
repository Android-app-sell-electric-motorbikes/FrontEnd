package com.example.evshop.data.network;

import com.example.evshop.domain.models.ApiEnvelope;
import com.example.evshop.domain.models.TransactionResult;
import com.example.evshop.domain.models.VnpayResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaymentApiService {
    // ** SỬA LẠI: CHỈ CẦN AMOUNT TRÊN ĐƯỜNG DẪN **
    @POST("api/Payment/create-vnpay-mobile/{amount}")
    Call<VnpayResponse> createVnpayPayment(@Path("amount") long amount);

    @GET("api/Payment/get-all-transactions-mobile")
    Call<ApiEnvelope<TransactionResult>> getAllTransactions(
        @Query("pageNumber") int pageNumber,
        @Query("pageSize") int pageSize
    );
}
